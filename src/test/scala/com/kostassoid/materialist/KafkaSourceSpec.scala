package com.kostassoid.materialist

import org.scalatest.{FreeSpec, MustMatchers}

class KafkaSourceSpec extends FreeSpec with MustMatchers {

  "Kafka source" - {
    "must handle json values" in {
      val prepared = KafkaSource.prepareValue("""{ "this": "xxx", "that": 13}""", "some-topic", 66)

      prepared mustBe """{"this":"xxx","that":13,"_topic":"some-topic","_partition":66}"""
    }

    "must handle string values" in {
      val prepared = KafkaSource.prepareValue("yo", "some-topic", 66)

      prepared mustBe """{"value":"yo","_topic":"some-topic","_partition":66}"""
    }

    "must handle int values" ignore {
      val prepared = KafkaSource.prepareValue("123456", "some-topic", 66)

      prepared mustBe """{"value":123456,"_topic":"some-topic","_partition":66}"""
    }
  }

}
