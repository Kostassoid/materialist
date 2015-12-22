package com.kostassoid.materialist

import java.io.File
import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}

object MainApp extends App with Logging with Metrics {

  log.info("Materialist started.")

  val configPath = Option(System.getProperty("config.path"))

  val config = AppConfig(
    configPath.map(c ⇒ ConfigFactory.parseFile(new File(c))).getOrElse(ConfigFactory.empty())
      .withFallback(ConfigFactory.load())
      .getConfig("materialist")
  )

  initMetricsReporter(config.raw.getConfig("graphite"))

  metrics.gauge("heartbeat") { 1 }

  val coordinator = new Coordinator(config)

  sys.addShutdownHook {
    log.info("Received shutdown signal. Closing.")
    coordinator.shutdown()
  }

  coordinator.run()

  log.info("Materialist is stopped.")

  def initMetricsReporter(config: Config) = {
    if (config.hasPath("enabled") && config.getBoolean("enabled")) {
      val host = config.getString("host")
      val port = config.getInt("port")
      log.info(s"Starting graphite reporter for $host:$port")
      Metrics.startReporter(
        host,
        port,
        config.getString("prefix"),
        config.getDuration("report-period", TimeUnit.MILLISECONDS)
      )
    }
  }
}

