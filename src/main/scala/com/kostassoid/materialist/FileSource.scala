package com.kostassoid.materialist

import java.io._

import com.typesafe.config.Config

class FileSourceFactory extends SourceFactory {
  override def getSource(config: Config, stream: String): Source = {
    new FileSource(new File(stream))
  }
}

class FileSource(file: File) extends Source with Logging {

  private var stream: BufferedReader = null
  private var offset: Long = 0

  override def toString = s"File(${file.getAbsolutePath})"

  override def start(): Unit = {
    log.debug(s"Reading ${file.getAbsolutePath} from offset $offset.")
    stream = new BufferedReader(new FileReader(file))
  }

  override def stop(): Unit = {
    if (stream != null) {
      stream.close()
      stream = null
    }
  }

  override def pull(): Iterable[StorageOperation] = {
    stream.readLine() match {
      case null ⇒
        log.trace("Nothing to read. Waiting 1000 ms.")
        Thread.sleep(1000)
        List.empty
      case l ⇒
        offset += l.getBytes.length
        val Array(key, message) = l.trim().split("->", 2)
        List(Upsert(key, message))
    }
  }

  override def commit(): Unit = {
    log.trace(s"Committing offset $offset")
    //todo:
  }
}
