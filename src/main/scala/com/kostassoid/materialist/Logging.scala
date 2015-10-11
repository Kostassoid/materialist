package com.kostassoid.materialist

import org.slf4j.LoggerFactory

trait Logging {
  protected lazy val log = LoggerFactory.getLogger(this.getClass)
}
