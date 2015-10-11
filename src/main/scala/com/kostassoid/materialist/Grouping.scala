package com.kostassoid.materialist

import scala.util.matching.Regex

trait Grouping {
  def resolveGroup(source: SourceRecord): Option[String]
}

class RegexGrouping(pattern: Regex, skipUnmatched: Boolean) extends Grouping {
  override def resolveGroup(source: SourceRecord) = {
    pattern.findFirstIn(source.key).orElse {
      if (!skipUnmatched) {
        throw new Exception(s"Group cannot be extracted from ${source.key}. Failing.")
      } else {
        None
      }
    }
  }
}

class FixedGrouping(value: String) extends Grouping {
  override def resolveGroup(source: SourceRecord) = Some(value)
}
