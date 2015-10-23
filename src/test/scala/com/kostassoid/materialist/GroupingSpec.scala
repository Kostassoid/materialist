package com.kostassoid.materialist

import org.scalatest.{MustMatchers, FreeSpec}

class GroupingSpec extends FreeSpec with MustMatchers {

  "Grouping" - {
    "must include all by default" in {
      val grouping = new Grouping(Nil, List("yyy".r), k ⇒ k)

      grouping.resolveGroup("xxx") mustBe defined
      grouping.resolveGroup("yyy") mustBe empty
    }

    "must include any matched" in {
      val grouping = new Grouping(List("xxx".r, "yyy".r), Nil, k ⇒ k)

      grouping.resolveGroup("xxx") mustBe defined
      grouping.resolveGroup("yyy") mustBe defined
      grouping.resolveGroup("zzz") mustBe empty
      grouping.resolveGroup("xx") mustBe empty
    }

    "must exclude nothing by default" in {
      val grouping = new Grouping(List("yyy".r), Nil, k ⇒ k)

      grouping.resolveGroup("xxx") mustBe empty
      grouping.resolveGroup("yyy") mustBe defined
    }

    "must exclude any matched" in {
      val grouping = new Grouping(Nil, List("xxx".r, "yyy".r), k ⇒ k)

      grouping.resolveGroup("xxx") mustBe empty
      grouping.resolveGroup("yyy") mustBe empty
      grouping.resolveGroup("zzz") mustBe defined
      grouping.resolveGroup("xx") mustBe defined
    }

    "must use group resolver" in {
      val grouping = Grouping.fromRegex(Nil, Nil, "^(\\w+)-.*$".r)

      grouping.resolveGroup("xxx-this") mustBe Some("xxx")
      grouping.resolveGroup("yyy-that") mustBe Some("yyy")
      grouping.resolveGroup("zzz-another-one") mustBe Some("zzz")
    }

    "must throw if resolver failed" in {
      val grouping = Grouping.fromRegex(Nil, Nil, "^(\\w+)-.*$".r)

      intercept[Exception] { grouping.resolveGroup("xxx") }
    }

  }

}
