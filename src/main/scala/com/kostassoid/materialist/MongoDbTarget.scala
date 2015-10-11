package com.kostassoid.materialist

import com.typesafe.config.Config

class MongoDbTargetFactory extends TargetFactory {
  override def getTarget(config: Config): Target =  {
    new MongoDbTarget(config.getString("mongodb.connection"))
  }
}

class MongoDbTarget(connectionString: String) extends Target {

  override def start(): Unit = {

  }

  override def stop(): Unit = {

  }

  override def push(docs: Iterable[TargetRecord]): Unit = {

  }
}
