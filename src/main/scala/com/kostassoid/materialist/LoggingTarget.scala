package com.kostassoid.materialist

import com.typesafe.config.Config

class LoggingTargetFactory extends TargetFactory {
  override def getTarget(config: Config): Target =
    new LoggingTarget
}

class LoggingTarget extends Target with Logging {
  override def start(): Unit = {}

  override def stop(): Unit = {}

  override def push(docs: Iterable[TargetRecord]): Unit = {
    docs.foreach(d ⇒ log.info(d.toString))
  }
}
