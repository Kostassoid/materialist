package com.kostassoid.materialist

import java.util.Properties

import com.typesafe.config.Config
import kafka.consumer.{ConsumerIterator, Consumer, ConsumerConfig}
import kafka.javaapi.consumer.ConsumerConnector

import scala.collection.JavaConversions._
import scala.collection.mutable

class KafkaSourceFactory extends SourceFactory with Logging {
  override def getSource(config: Config): Source = {

    val consumerProps = config.getConfig("kafka.consumer")
      .entrySet()
      .map(e ⇒ (e.getKey, e.getValue.unwrapped().toString))
      .foldLeft(new Properties()) { (p, kv) ⇒ p.put(kv._1, kv._2); p }

    consumerProps.put("auto.commit.enable", "false")
    consumerProps.put("auto.offset.reset", "smallest")

    log.debug(s"Got properties: $consumerProps")

    new KafkaSource(new ConsumerConfig(consumerProps), config.getString("kafka.topic"), config.getLong("batch.size"), config.getLong("batch.wait.ms"))
  }
}

class KafkaSource(consumerConfig: ConsumerConfig, topic: String, batchSize: Long, timeout: Long) extends Source with Logging {

  private var connector: ConsumerConnector = null
  private var stream: ConsumerIterator[Array[Byte], Array[Byte]] = null

  override def start(): Unit = {
    stop()

    connector = Consumer.createJavaConsumerConnector(consumerConfig)
    stream = connector.createMessageStreams(Map(topic → Integer.valueOf(1))).get(topic).get(0).iterator()
  }

  override def stop(): Unit = {
    if (connector != null) {
      connector.shutdown()
      connector = null
    }
  }

  override def pull(): Iterable[Operation] = {
    require(connector != null, "Source isn't started.")

    val batch = mutable.Map.empty[String, Operation]
    val start = System.currentTimeMillis()
    while (batch.size < batchSize && (System.currentTimeMillis() - start) < timeout) {
      if (stream.hasNext()) {
        val next = stream.next()
        val key = new String(next.key(), "utf-8")
        val op = next.message() match {
          case x if x == null ⇒ Delete(key, next.partition.toString)
          case v ⇒ Upsert(key, next.partition.toString, new String(v, "utf-8"))
        }

        batch += key → op
      } else {
        log.trace("Nothing to read. Waiting 1000 ms.")
        Thread.sleep(1000)
      }
    }
    batch.values
  }

  override def commit(): Unit = {
    connector.commitOffsets(true)
  }
}
