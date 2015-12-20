package com.kostassoid.materialist

import com.typesafe.config.Config
import scala.collection.JavaConversions._

object AppConfig {

  implicit class ConfigEx(c: Config) {
    def getString(path: String, default: ⇒ String) = {
      if (c.hasPath(path)) c.getString(path) else default
    }
  }

  def getRoutes(configs: List[Config]): List[RouteConfig] = {
    configs.map { r ⇒
      val from = r.getString("from")
      val matchKey = r.getString("match.key", "_")
      val to = r.getString("to", "_")

      RouteConfig(from, matchKey, to)
    }
  }

  def apply(config: Config) =
    new AppConfig(
      routes = getRoutes(config.getConfigList("routes").toList),
      sourceConfig = config.getConfig("source"),
      sourceFactory = Class.forName(config.getString("source.factory.class")).newInstance().asInstanceOf[SourceFactory],
      targetConfig = config.getConfig("target"),
      targetFactory = Class.forName(config.getString("target.factory.class")).newInstance().asInstanceOf[TargetFactory],
      checkpointInterval = config.getLong("checkpoint.interval")
    )
}

case class AppConfig(
                      routes: List[RouteConfig],
                      sourceConfig: Config,
                      sourceFactory: SourceFactory,
                      targetConfig: Config,
                      targetFactory: TargetFactory,
                      checkpointInterval: Long
)
