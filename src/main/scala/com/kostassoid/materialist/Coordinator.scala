package com.kostassoid.materialist

import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

class Coordinator(config: AppConfig) extends Runnable with Logging {

  lazy val workers = config.routes.map { route ⇒

    val targetStream = route.to match {
      case Some(to) ⇒ to
      case None ⇒ route.from
    }

    val operationPredicate: StorageOperation ⇒ Boolean = route.matchKey match {
      case None ⇒
        _ ⇒ true
      case Some(keyRegex) ⇒
        val keyMatcher = keyRegex.r
        op ⇒ keyMatcher.pattern.matcher(op.key).matches()
    }

    val source = config.sourceFactory.getSource(config.sourceConfig, route.from)
    val target = config.targetFactory.getTarget(config.targetConfig, targetStream)

    new RouteWorker(Route(source, target, operationPredicate), config.checkpointInterval)
  }

  val pool = new ThreadPoolWatcherExecutor(config.routes.size)

  def run() = {
    try {
      val futures = workers map { pool.submit }

      pool.awaitTermination(Long.MaxValue, TimeUnit.DAYS) // todo: refactor
    } catch {
      case _: InterruptedException ⇒
    }
  }

  def shutdown(): Unit = {
    workers foreach { _.shutdown() }
    pool.shutdown()
    if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
      pool.shutdownNow()
    }
  }

  class ThreadPoolWatcherExecutor(threads: Int)
    extends ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue()) {
    override def afterExecute(runnable: Runnable, throwable: Throwable): Unit = {
      super.afterExecute(runnable, throwable)
      if (!isShutdown) {
        log.warn("Shutting down pool due to premature worker completion.")
        Coordinator.this.shutdown()
      }
    }
  }


}
