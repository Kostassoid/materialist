package com.kostassoid.materialist

import com.typesafe.config.Config
import scala.collection.JavaConversions._

object AppConfig {

  implicit class ConfigEx(c: Config) {
    def getStringOpt(path: String) = {
      if (c.hasPath(path)) Some(c.getString(path)) else None
    }
  }

  def getRoutes(configs: List[Config]): List[RouteConfig] = {
    configs.map { r ⇒
      val from = r.getString("from")
      val matchKey = r.getStringOpt("match.key")
      val to = r.getStringOpt("to")

      RouteConfig(from, matchKey, to)
    }
  }

  def apply(config: Config) =
    new AppConfig(
      raw = config,
      routes = getRoutes(config.getConfigList("routes").toList),
      sourceConfig = config.getConfig("source"),
      sourceFactory = Class.forName(config.getString("source.factory.class")).newInstance().asInstanceOf[SourceFactory],
      targetConfig = config.getConfig("target"),
      targetFactory = Class.forName(config.getString("target.factory.class")).newInstance().asInstanceOf[TargetFactory],
      checkpointInterval = config.getLong("checkpoint.interval")
    )
}

case class AppConfig(
                      raw: Config,
                      routes: List[RouteConfig],
                      sourceConfig: Config,
                      sourceFactory: SourceFactory,
                      targetConfig: Config,
                      targetFactory: TargetFactory,
                      checkpointInterval: Long
)
