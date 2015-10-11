package com.kostassoid.materialist

import com.typesafe.config.Config
import scala.collection.JavaConversions._

object AppConfig {

  def getGroupings(config: Config): List[Grouping] = {
    config.root().keySet().map(config.getConfig).map { g ⇒
      g.getString("type") match {
        case "fixed" ⇒ new FixedGrouping(g.getString("value"))
        case "regex" ⇒ new RegexGrouping(g.getString("pattern").r, g.getBoolean("skip-unmatched"))
        case x ⇒ throw new Exception(s"Unknown grouping type: $x")
      }
    } toList
  }

  def apply(config: Config) =
    new AppConfig(
      groupings = getGroupings(config.getConfig("groupings")),
      preventKeyCollision = config.getString("key.prevent-collision").toBoolean,
      sourceConfig = config.getConfig("source"),
      sourceFactory = Class.forName(config.getString("source.factory.class")).newInstance().asInstanceOf[SourceFactory],
      targetConfig = config.getConfig("target"),
      targetFactory = Class.forName(config.getString("target.factory.class")).newInstance().asInstanceOf[TargetFactory]
    )
}

case class AppConfig(
                      groupings: List[Grouping],
                      preventKeyCollision: Boolean,
                      sourceConfig: Config,
                      sourceFactory: SourceFactory,
                      targetConfig: Config,
                      targetFactory: TargetFactory
)
