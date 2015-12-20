package com.kostassoid.materialist

import java.util.Properties

import com.typesafe.config.Config
import kafka.consumer.{Consumer, ConsumerConfig, ConsumerIterator}
import kafka.javaapi.consumer.ConsumerConnector

import scala.collection.JavaConversions._

class KafkaSourceFactory extends SourceFactory with Logging {
  override def getSource(config: Config, stream: String): Source = {

    val consumerProps = config.getConfig("kafka.consumer")
      .entrySet()
      .map(e ⇒ (e.getKey, e.getValue.unwrapped().toString))
      .foldLeft(new Properties()) { (p, kv) ⇒ p.put(kv._1, kv._2); p }

    consumerProps.put("auto.commit.enable", "false")
    consumerProps.put("auto.offset.reset", "smallest")

    if (!consumerProps.containsKey("group.id")) {
      consumerProps.put("group.id", s"materialist-$stream")
    }

    log.debug(s"Got properties: $consumerProps")

    new KafkaSource(new ConsumerConfig(consumerProps), stream)
  }
}

class KafkaSource(consumerConfig: ConsumerConfig, topic: String) extends Source with Logging {

  private var connector: ConsumerConnector = null
  private var stream: ConsumerIterator[Array[Byte], Array[Byte]] = null

  override def toString = s"Kafka($topic)"

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

  override def pull(): Iterable[StorageOperation] = {
    require(connector != null, "Source isn't started.")

    // todo: add partition info
    if (stream.hasNext()) {
      val next = stream.next()
      val key = new String(next.key(), "utf-8")
      Some(next.message() match {
        case x if x == null ⇒ Delete(key)
        case v ⇒ Upsert(key, new String(v, "utf-8"))
      })
    } else {
      None
    }
  }

  override def commit(): Unit = {
    connector.commitOffsets(true)
  }
}
