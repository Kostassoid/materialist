package com.kostassoid.materialist

import java.util.Properties

import com.typesafe.config.Config
import kafka.consumer.{Consumer, ConsumerConfig, ConsumerIterator, ConsumerTimeoutException}
import kafka.javaapi.consumer.ConsumerConnector
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.collection.JavaConversions._
import scala.util.Try

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

    if (!consumerProps.containsKey("consumer.timeout.ms")) {
      consumerProps.put("consumer.timeout.ms", "1000")
    }

    log.debug(s"Got properties: $consumerProps")

    new KafkaSource(new ConsumerConfig(consumerProps), stream)
  }
}

object KafkaSource {

  def prepareValue(in: String, topic: String, partition: Int): String = {

    // todo: support non-string primitives
    val json = Try { parse(in) } recover { case _: ParserUtil.ParseException ⇒ JObject("value" → JString(in)) } get

    compact(render(json merge (("_topic" → topic) ~ ("_partition" → partition))))
  }
}

class KafkaSource(consumerConfig: ConsumerConfig, topic: String) extends Source with Logging with Metrics {

  import KafkaSource._

  private var connector: ConsumerConnector = null
  private var stream: ConsumerIterator[Array[Byte], Array[Byte]] = null

  override def toString = s"Kafka($topic)"

  override def start(): Unit = {
    stop()

    log.info(s"Creating connector for $topic")
    connector = Consumer.createJavaConsumerConnector(consumerConfig)

    log.info(s"Creating message streams for $topic")
    stream = connector.createMessageStreams(Map(topic → Integer.valueOf(1))).get(topic).get(0).iterator()

    log.trace(s"Kafka source started for $topic")
  }

  override def stop(): Unit = {
    if (connector != null) {
      connector.shutdown()
      connector = null
    }
  }

  override def pull(): Iterable[StorageOperation] = {
    require(connector != null, "Source isn't started.")

    try {
      if (stream.hasNext()) {

        metrics.meter("kafka.pull.meter").mark()

        metrics.timer("kafka.pull.timer").time {
          val next = stream.next()
          val key = new String(next.key(), "utf-8")
          Some(next.message() match {
            case x if x == null ⇒ Delete(key)
            case v ⇒ Upsert(key, prepareValue(new String(v, "utf-8"), next.topic, next.partition))
          })
        }
      } else {
        None
      }
    }
    catch {
      case _: ConsumerTimeoutException ⇒ None
    }
  }

  override def commit(): Unit = {
    metrics.timer("kafka.commit.timer").time {
      connector.commitOffsets(true)
    }
  }
}
