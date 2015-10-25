package com.kostassoid.materialist

class Coordinator(source: Source, target: Target, groupings: List[Grouping]) extends Logging {

  def run() = {
    log.info(s"Starting source $source")
    source.start()

    log.info(s"Starting target $target")
    target.start()


    try {
      Iterator.continually(source.pull())
        .filter(_.nonEmpty)
        .foreach { batch ⇒

          // todo: optimize
          batch.flatMap { r ⇒ groupings.flatMap(_.resolveGroup(r.key)).map(_ → r) }
            .groupBy(_._1).map(gi ⇒ gi._1 → gi._2.map(_._2))
            .foreach { gd ⇒
              target.push(gd._1, gd._2)
            }

          target.flush()
          source.commit()
        }
    } catch {
      case _: InterruptedException ⇒ // ok
      case e: Throwable ⇒ log.error("Unexpected exception. Closing.", e)
    } finally {
      log.info(s"Stopping source $source")
      source.stop()
      log.info(s"Stopping target $target")
      target.stop()
    }
  }
}
