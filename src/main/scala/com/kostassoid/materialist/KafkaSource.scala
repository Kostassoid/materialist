package com.kostassoid.materialist

import java.util.Properties

import com.typesafe.config.Config
import kafka.consumer.{Consumer, ConsumerConfig}
import kafka.javaapi.consumer.ConsumerConnector

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

class KafkaSourceFactory extends SourceFactory with Logging {
  override def getSource(config: Config): Source = {

    val consumerProps = config.getConfig("kafka.consumer")
      .entrySet()
      .map(e ⇒ (e.getKey, e.getValue.unwrapped().toString))
      .foldLeft(new Properties()) { (p, kv) ⇒ p.put(kv._1, kv._2); p }

    log.debug(s"Got properties: $consumerProps")

    new KafkaSource(new ConsumerConfig(consumerProps), config.getString("kafka.topic"), config.getLong("batch.size"), config.getLong("batch.wait.ms"))
  }
}

class KafkaSource(consumerConfig: ConsumerConfig, topic: String, batchSize: Long, timeout: Long) extends Source with Logging {

  private var connector: ConsumerConnector = null

  override def start(): Unit = {
    stop()

    connector = Consumer.createJavaConsumerConnector(consumerConfig)
  }

  override def stop(): Unit = {
    if (connector != null) {
      connector.shutdown()
      connector = null
    }
  }

  override def iterator: Iterator[List[SourceRecord]] = {
    // todo: improve performance
    val stream = connector.createMessageStreams(Map(topic → Integer.valueOf(1))).get(topic).get(0).iterator()
    new Iterator[List[SourceRecord]] {

      private val batch = ListBuffer.empty[SourceRecord]
      private var isClosed = false

      override def hasNext: Boolean = !isClosed

      override def next(): List[SourceRecord] = {
        try {
          batch.clear()
          val start = System.currentTimeMillis()
          while (batch.size < batchSize && (System.currentTimeMillis() - start) < timeout) {
            if (stream.hasNext()) {
              val next = stream.next()
              val key = new String(next.key(), "utf-8")
              val message = new String(next.message(), "utf-8")
              batch.append(SourceRecord(key, message, next.partition.toString))
            } else {
              log.trace("Nothing to read. Waiting 1000 ms.")
              Thread.sleep(1000)
            }
          }
          batch.toList
        } catch {
          case e: InterruptedException ⇒
            isClosed = true
            List.empty
        }
      }
    }
  }

  override def commit(): Unit = {
    connector.commitOffsets(true)
  }
}
