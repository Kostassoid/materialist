package com.kostassoid.materialist

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
  override def getTarget(config: Config): Target =  {
    new MongoDbTarget(config.getString("mongodb.connection"), config.getString("mongodb.database"))
  }
}

class MongoDbTarget(connectionString: String, databaseName: String) extends Target with Logging {

  private var client: MongoClient = null
  private var db: MongoDatabase = null
  private var outstanding = mutable.Map.empty[String, Future[BulkWriteResult]]

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

  override def push(group: String, sourceOperations: Iterable[Operation]): Unit = {
    val updateOptions = new UpdateOptions().upsert(true)
    val collection = db.getCollection(group)
    val ops = sourceOperations.map { r ⇒

      r match {
        case Upsert(key, stream, value) ⇒
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
        case Delete(key, stream) ⇒
          DeleteOneModel(Document("_id" → BsonString(key)))
      }

    }
    waitForCompletion(group)
    outstanding += group → applyOps(collection, ops.toSeq, Promise[BulkWriteResult]()).future
  }

  private def waitForCompletion(collection: String) = {
    outstanding.get(collection).foreach { f ⇒
      Await.result(f, Duration.Inf) // todo: better idea
      outstanding -= collection
    }
  }

  //@tailrec
  private def applyOps(collection: MongoCollection[scala.Document], ops: Seq[WriteModel[_ <: Document]], promise: Promise[BulkWriteResult]): Promise[BulkWriteResult] = {
    collection.bulkWrite(ops, BulkWriteOptions().ordered(true)).subscribe(
      (completed: BulkWriteResult) ⇒ {
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
    outstanding.keySet.foreach(waitForCompletion)
  }
}
