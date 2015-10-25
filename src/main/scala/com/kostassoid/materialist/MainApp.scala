package com.kostassoid.materialist

import java.io.File

import com.typesafe.config.ConfigFactory

object MainApp extends App with Logging {

  log.info("Materialist is starting.")

  val config = AppConfig(
    ConfigFactory.parseFile(new File(System.getProperty("config.path")))
      .withFallback(ConfigFactory.load())
      .getConfig("materialist")
  )

  val thisThread = Thread.currentThread()
  sys.addShutdownHook {
    log.info("Received shutdown signal. Closing.")
    thisThread.interrupt()
    thisThread.join()
  }

  val source = config.sourceFactory.getSource(config.sourceConfig)
  val target = config.targetFactory.getTarget(config.targetConfig)

  new Coordinator(source, target, config.groupings).run()

  log.info("Materialist finished.")
}

