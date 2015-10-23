package com.kostassoid.materialist

class Router(source: Source, target: Target, groupings: List[Grouping]) extends Logging {
  def run() = {
    log.info(s"Starting source $source")
    source.start()

    log.info(s"Starting target $target")
    target.start()

    try {
      source.iterator
        .filter(_.nonEmpty)
        .foreach { batch ⇒
          val toSend = batch.flatMap { record ⇒
            val key = /*if (config.preventKeyCollision) s"${r.key}-${r.stream}" else*/ record.key
            groupings.flatMap(_.resolveGroup(record.key)).map(TargetRecord(_, key, record.value))
          }
          target.push(toSend)
          source.commit()
        }
    } finally {
      log.info(s"Stopping source $source")
      source.stop()
      log.info(s"Stopping target $target")
      target.stop()
    }
  }
}
