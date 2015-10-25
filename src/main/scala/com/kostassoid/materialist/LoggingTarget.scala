package com.kostassoid.materialist

import com.typesafe.config.Config

class LoggingTargetFactory extends TargetFactory {
  override def getTarget(config: Config): Target =
    new LoggingTarget
}

class LoggingTarget extends Target with Logging {
  override def start(): Unit = {}

  override def stop(): Unit = {}

  override def push(group: String, records: Iterable[Operation]): Unit = {
    records.foreach(d ⇒ log.info(d.toString))
  }

  override def flush(): Unit = {
    log.info("Flushing")
  }
}
