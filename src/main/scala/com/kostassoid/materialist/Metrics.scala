package com.kostassoid.materialist

import java.net.{InetAddress, InetSocketAddress}
import java.util.{Timer, TimerTask}

import com.codahale.metrics.graphite.{Graphite, GraphiteReporter}
import nl.grons.metrics.scala.{FutureMetrics, MetricName, InstrumentedBuilder}

object Metrics {
  val metricRegistry = new com.codahale.metrics.MetricRegistry()

  def startReporter(host: String, port: Int, prefix: String, reportPeriod: Long) = {

    val graphite = new Graphite(new InetSocketAddress(host, port))

    val reporter = GraphiteReporter.forRegistry(metricRegistry)
      .prefixedWith(prefix + "." + InetAddress.getLocalHost.getHostName.replaceAll("\\.", "-"))
      .build(graphite)

    new Timer("graphite-reporter-timer").schedule(
      new TimerTask { def run() = reporter.report() },
      reportPeriod,
      reportPeriod
    )
  }
}

trait Metrics extends InstrumentedBuilder with FutureMetrics {
  override lazy val metricBaseName = MetricName("materialist")

  lazy val metricRegistry = Metrics.metricRegistry
}
