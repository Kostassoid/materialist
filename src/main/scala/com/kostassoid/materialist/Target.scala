package com.kostassoid.materialist

import com.typesafe.config.Config

trait TargetFactory {
  def getTarget(config: Config): Target
}

trait Target {
  def start(): Unit
  def stop(): Unit

  def push(group: String, docs: Iterable[Operation]): Unit
  def flush()
}
