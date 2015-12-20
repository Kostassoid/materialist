package com.kostassoid.materialist

import java.io.File

import com.typesafe.config.ConfigFactory

object MainApp extends App with Logging {

  log.info("Materialist started.")

  val configPath = Option(System.getProperty("config.path"))

  val config = AppConfig(
    configPath.map(c ⇒ ConfigFactory.parseFile(new File(c))).getOrElse(ConfigFactory.empty())
      .withFallback(ConfigFactory.load())
      .getConfig("materialist")
  )

  val coordinator = new Coordinator(config)

  sys.addShutdownHook {
    log.info("Received shutdown signal. Closing.")
    coordinator.shutdown()
  }

  coordinator.run()

  log.info("Materialist is stopped.")
}

