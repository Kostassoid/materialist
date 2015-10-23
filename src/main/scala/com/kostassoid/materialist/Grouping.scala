package com.kostassoid.materialist

import scala.util.matching.Regex

object Grouping {
  def fromName(allow: List[Regex], exclude: List[Regex], groupName: String) =
    new Grouping(allow, exclude, _ ⇒ groupName)

  def fromRegex(allow: List[Regex], exclude: List[Regex], groupPattern: Regex) =
  new Grouping(allow, exclude, k ⇒ groupPattern.findFirstMatchIn(k).get.group(1))
}

class Grouping(allow: List[Regex], exclude: List[Regex], groupResolver: String ⇒ String) {
  def resolveGroup(key: String): Option[String] = {
    if (isAllowed(key) && !isExcluded(key)) Some(groupResolver(key)) else None
  }

  private def isAllowed(key: String) = {
    allow match {
      case Nil ⇒ true
      case l ⇒ l.exists(_.findFirstIn(key).isDefined)
    }
  }

  private def isExcluded(key: String) = exclude.exists(_.findFirstIn(key).isDefined)
}