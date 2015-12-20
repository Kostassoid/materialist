package com.kostassoid.materialist

import com.typesafe.config.Config

trait SourceFactory {
  def getSource(config: Config, stream: String): Source
}

trait Source {
  def start(): Unit
  def stop(): Unit

  def pull(): Iterable[StorageOperation]
  def commit(): Unit
}
