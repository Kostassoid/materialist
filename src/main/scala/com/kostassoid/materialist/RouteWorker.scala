package com.kostassoid.materialist

import java.util.concurrent.atomic.AtomicBoolean

class RouteWorker(route: Route, checkpointInterval: Long) extends Runnable with Logging {

  val mustShutdown = new AtomicBoolean(false)

  def shutdown() = {
    mustShutdown.set(true)
  }

  def run() = {

    log.info(s"Starting route worker for ${route.source} -> ${route.target}")

    try {
      log.info(s"Starting source ${route.source}")
      route.source.start()

      log.info(s"Starting target ${route.target}")
      route.target.start()

      var checkpointTime = System.currentTimeMillis()

      Iterator.continually(route.source.pull())
        .takeWhile(_ ⇒ !mustShutdown.get())
        .filter(_.nonEmpty)
        .foreach { batch ⇒

          batch filter { route.operationPredicate } foreach { route.target.push }

          if (System.currentTimeMillis() - checkpointTime > checkpointInterval) {
            log.trace(s"Checkpoint for ${route.source} -> ${route.target}")
            route.target.flush()
            route.source.commit()
            checkpointTime = System.currentTimeMillis()
          }
        }

      route.target.flush()
      route.source.commit()

    } catch {
      case _: InterruptedException ⇒ // ok
      case e: Throwable ⇒
        log.error("Unexpected exception. Closing.", e)
    } finally {
      log.info(s"Stopping source ${route.source}")
      route.source.stop()
      log.info(s"Stopping target ${route.target}")
      route.target.stop()
    }
  }
}
