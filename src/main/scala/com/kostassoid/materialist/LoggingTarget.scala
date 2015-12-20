package com.kostassoid.materialist

import com.typesafe.config.Config

class LoggingTargetFactory extends TargetFactory {
  override def getTarget(config: Config, stream: String): Target =
    new LoggingTarget
}

class LoggingTarget extends Target with Logging {

  override def toString = s"Logging"

  override def start(): Unit = {}

  override def stop(): Unit = {}

  override def push(op: StorageOperation): Unit = {
    log.info(op.toString)
  }

  override def flush(): Unit = {
    log.info("Flushing")
  }
}
