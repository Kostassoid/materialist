package com.kostassoid.materialist

import com.typesafe.config.Config
import scala.collection.JavaConversions._

object AppConfig {

  def getGroupings(configs: List[Config]): List[Grouping] = {
    configs.map { g ⇒
      val allow = if (g.hasPath("allow")) g.getStringList("allow").map(_.r).toList else Nil
      val exclude = if (g.hasPath("exclude")) g.getStringList("exclude").map(_.r).toList else Nil

      if (g.hasPath("group.pattern")) {
        Grouping.fromRegex(allow, exclude, g.getString("group.pattern").r)
      } else
      if (g.hasPath("group.name")) {
        Grouping.fromName(allow, exclude, g.getString("group.name"))
      } else {
        throw new Exception("Missing [group.name] or [group.regex] in grouping specification.")
      }
    }
  }

  def apply(config: Config) =
    new AppConfig(
      groupings = getGroupings(config.getConfigList("groupings").toList),
      sourceConfig = config.getConfig("source"),
      sourceFactory = Class.forName(config.getString("source.factory.class")).newInstance().asInstanceOf[SourceFactory],
      targetConfig = config.getConfig("target"),
      targetFactory = Class.forName(config.getString("target.factory.class")).newInstance().asInstanceOf[TargetFactory]
    )
}

case class AppConfig(
  groupings: List[Grouping],
  sourceConfig: Config,
  sourceFactory: SourceFactory,
  targetConfig: Config,
  targetFactory: TargetFactory
)
