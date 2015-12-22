package com.kostassoid.materialist

import com.typesafe.config.Config

class NullSourceFactory extends SourceFactory {
  override def getSource(config: Config, stream: String): Source = {
    new NullSource
  }
}

class NullSource extends Source {

  override def toString = s"Null"

  override def start(): Unit = {}

  override def stop(): Unit = {}

  override def pull(): Iterable[StorageOperation] = { Nil }

  override def commit(): Unit = {}
}
