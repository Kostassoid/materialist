package com.kostassoid.materialist

import com.typesafe.config.Config

case class SourceRecord(key: String, value: String, stream: String)

trait SourceFactory {
  def getSource(config: Config): Source
}

trait Source {
  def start(): Unit
  def stop(): Unit

  def iterator: Iterator[List[SourceRecord]]
  def commit(): Unit
}
