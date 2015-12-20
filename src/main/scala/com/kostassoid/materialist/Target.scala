package com.kostassoid.materialist

import com.typesafe.config.Config

trait TargetFactory {
  def getTarget(config: Config, stream: String): Target
}

trait Target {
  def start(): Unit
  def stop(): Unit

  def push(operation: StorageOperation): Unit
  def flush()
}
