package jk_5.nailed.installer

import java.util
import java.io._
import java.net.URL
import scala.Some
import java.util.concurrent.{CountDownLatch, Executors}
import java.util.concurrent.atomic.AtomicInteger

/**
 * No description given
 *
 * @author jk-5
 */
object LibraryList {
  val downloadThreadPool = Executors.newCachedThreadPool()
  def readFromUrl(url: String): Option[LibraryList] = {
    var reader: Reader = null
    try{
      val u = new URL(url)
      reader = new InputStreamReader(u.openStream())
      return Some(Installer.gson.fromJson(reader, classOf[LibraryList]))
    }finally{
      if(reader != null) reader.close()
    }
    None
  }
}

class Library {
  var name: String = _
  var rev: Int = _
  var destination: String = _
  var location: String = _
  var restart: String = _
  var mod: Boolean = _

  def download(latch: CountDownLatch, monitor: DownloadMonitor, progress: AtomicInteger){
    LibraryList.downloadThreadPool.submit(new Runnable {
      override def run(){
        val startTime = System.currentTimeMillis()
        println("Starting the download of " + name)
        val destination = Installer.resolve(Library.this.destination)
        if(!destination.getParentFile.exists()) destination.getParentFile.mkdirs()
        val in = new BufferedInputStream(new URL(location).openStream())
        val out = new BufferedOutputStream(new FileOutputStream(destination))
        val data = new Array[Byte](1024)
        var read = in.read(data, 0, 1024)
        while(read >= 0){
          out.write(data, 0, read)
          read = in.read(data, 0, 1024)
        }
        out.close()
        in.close()
        println("Finished the download of " + name + " in " + (System.currentTimeMillis() - startTime) + "ms")
        monitor.setProgress(progress.getAndIncrement)
        latch.countDown()
      }
    })
  }
}

class LibraryList {
  var versionName: String = _
  var libraries: util.List[Library] = _ //We need a java list here. Gson doesn't know how to handle scala collections
}
