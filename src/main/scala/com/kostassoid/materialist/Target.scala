package com.kostassoid.materialist

import com.typesafe.config.Config

case class TargetRecord(group: String, key: String, value: String)

trait TargetFactory {
  def getTarget(config: Config): Target
}

trait Target {
  def start(): Unit
  def stop(): Unit

  def push(docs: Iterable[TargetRecord]): Unit
}
