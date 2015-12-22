package com.kostassoid.materialist

import com.typesafe.config.Config


class NullTargetFactory extends TargetFactory {
  override def getTarget(config: Config, stream: String): Target =
    new NullTarget
}

class NullTarget extends Target {

  override def toString = s"Null"

  override def start(): Unit = {}

  override def stop(): Unit = {}

  override def push(op: StorageOperation): Unit = {}

  override def flush(): Unit = {}
}
