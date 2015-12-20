package com.kostassoid.materialist

object Metrics {
  val metricRegistry = new com.codahale.metrics.MetricRegistry()
}

trait Metrics {
  val metrics = Metrics.metricRegistry
}
