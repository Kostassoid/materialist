package com.kostassoid.materialist

import com.typesafe.config.ConfigFactory

object MainApp extends App with Logging {

  log.info("Materialist is starting.")

  val config = AppConfig(ConfigFactory.load().getConfig("materialist"))

  val thisThread = Thread.currentThread()
  sys.addShutdownHook {
    log.info("Received shutdown signal. Closing.")
    thisThread.interrupt()
    thisThread.join()
  }

  val source = config.sourceFactory.getSource(config.sourceConfig)
  val target = config.targetFactory.getTarget(config.targetConfig)

  new Router(source, target, config.groupings).run()

  log.info("Materialist finished.")
}

