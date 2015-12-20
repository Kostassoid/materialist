package com.kostassoid.materialist

import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.model.{UpdateOptions, WriteModel}
import com.typesafe.config.Config
import org.bson.BsonType
import org.bson.json.JsonReader
import org.mongodb.scala
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.{BulkWriteOptions, DeleteOneModel, ReplaceOneModel}

import _root_.scala.collection.mutable
import _root_.scala.concurrent.duration.Duration
import _root_.scala.concurrent.{Await, Future, Promise}

class MongoDbTargetFactory extends TargetFactory {
  override def getTarget(config: Config, stream: String): Target =  {
    new MongoDbTarget(config.getString("mongodb.connection"), config.getString("mongodb.database"), stream, config.getLong("batch.size"))
  }
}

class MongoDbTarget(connectionString: String, databaseName: String, stream: String, batchSize: Long) extends Target with Logging {

  private var client: MongoClient = null
  private var db: MongoDatabase = null

  private val buffer = mutable.ListBuffer.empty[StorageOperation]
  private var outstanding: Option[Future[BulkWriteResult]] = None

  override def toString = s"MongoDb($databaseName/$stream)"

  override def start(): Unit = {
    log.info(s"Connecting to $connectionString")
    client = MongoClient(connectionString)
    db = client.getDatabase(databaseName)
  }

  override def stop(): Unit = {
    if (client != null) {
      log.info("Stopping MongoDb client")
      client.close()
      client = null
    }
  }

  override def push(operation: StorageOperation): Unit = {
    buffer += operation
    if (buffer.size >= batchSize) {
      saveBuffer()
    }
  }

  private def saveBuffer(): Unit = {
    val updateOptions = new UpdateOptions().upsert(true)
    val collection = db.getCollection(stream)
    val ops = buffer map { r ⇒

      r match {
        case Upsert(key, value) ⇒
          val reader = new JsonReader(value)
          try {
            val doc = reader.readBsonType() match {
              case BsonType.STRING ⇒ Document("value" → reader.readString())
              case BsonType.INT32 ⇒ Document("value" → reader.readInt32())
              case BsonType.INT64 ⇒ Document("value" → reader.readInt64())
              case BsonType.DOUBLE ⇒ Document("value" → reader.readDouble())
              case BsonType.BOOLEAN ⇒ Document("value" → reader.readBoolean())
              case _ ⇒ Document(value)
            }
            ReplaceOneModel(Document("_id" → BsonString(key)),
              doc + ("_id" → BsonString(key), "_stream" → BsonString(stream)),
              updateOptions)
          } finally {
            reader.close()
          }
        case Delete(key) ⇒
          DeleteOneModel(Document("_id" → BsonString(key)))
      }
    }

    if (buffer.nonEmpty) {
      waitForCompletion()
      outstanding = Some(applyOps(collection, ops.toSeq, Promise[BulkWriteResult]()).future)
      buffer.clear()
    }
  }

  private def waitForCompletion() = {
    outstanding foreach { f ⇒
      Await.result(f, Duration.Inf) // todo: better idea
      outstanding = None
    }
  }

  //@tailrec
  private def applyOps(collection: MongoCollection[scala.Document], ops: Seq[WriteModel[_ <: Document]], promise: Promise[BulkWriteResult]): Promise[BulkWriteResult] = {
    collection.bulkWrite(ops, BulkWriteOptions().ordered(true)).subscribe(
      (completed: BulkWriteResult) ⇒ {
        // todo: check result
        promise.success(completed)
      },
      (failed: Throwable) ⇒ {
        log.warn("Unable to apply updates. Retrying.", failed)
        applyOps(collection, ops, promise)
      }
    )
    promise
  }

  def flush() = {
    saveBuffer()
    waitForCompletion()
  }
}
