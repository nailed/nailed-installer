package jk_5.nailed.installer

import java.io.{FileWriter, FileReader, FileOutputStream, File}
import java.util.jar.{JarOutputStream, Attributes, Manifest}
import javax.swing.JOptionPane
import scala.collection.JavaConversions._
import java.util.concurrent.CountDownLatch
import com.google.gson.{JsonParseException, JsonParser, JsonObject}
import java.util.concurrent.atomic.AtomicInteger

/**
 * No description given
 *
 * @author jk-5
 */
trait InstallTask {

  private var targetDir: File = _
  @inline def target = this.targetDir

  protected def run(): Boolean
  def runInstall(targetDir: File): Boolean = {
    this.targetDir = targetDir
    try{
      this.run()
    }catch{
      case AbortInstallException => return false
      case e: Exception =>
        JOptionPane.showMessageDialog(null, "An error has occurred while installing nailed. See the log for details", "An error has occured", JOptionPane.ERROR_MESSAGE)
        sys.exit(0)
        return false
    }
  }

  def error(message: String){
    JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
    throw AbortInstallException
  }
}

object InstallTaskClient extends InstallTask {

  protected def run(): Boolean = {
    if(!target.exists()) this.error("The selected directory does not exist!")
    val launcherProfiles = new File(target, "launcher_profiles.json")
    if(!launcherProfiles.exists()) this.error("There is no minecraft launcher profile at this location. Please run the minecraft launcher once to generate it")

    val libList = Installer.list.get

    val versionRoot = new File(target, "versions")
    val versionTarget = new File(versionRoot, libList.versionName)
    if(!versionTarget.mkdirs() && !versionTarget.isDirectory){
      if(!versionTarget.delete()){
        error("There was a problem with launcher version data directories. You need to remove " + versionTarget.getAbsolutePath + " to fix this")
      }else{
        versionTarget.mkdirs()
      }
    }

    //Generate a dummy jar. We load minecraft as a library
    val manifest = new Manifest
    manifest.getMainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0")
    new JarOutputStream(new FileOutputStream(new File(versionTarget, libList.versionName + ".jar"))).close()

    val latch = new CountDownLatch(libList.libraries.size())
    val monitor = new DownloadMonitor
    val progress = new AtomicInteger(0)
    monitor.setMaximum(libList.libraries.size() + 2)
    monitor.setNote("Downloading files...")
    libList.libraries.foreach(_.download(latch, monitor, progress))

    val startTime = System.currentTimeMillis()
    monitor.setProgress(progress.getAndIncrement)

    val parser = new JsonParser
    var jsonProfileData: JsonObject = null

    monitor.setProgress(progress.getAndIncrement)
    monitor.setNote("Writing launcher profile")

    try{
      jsonProfileData = parser.parse(new FileReader(launcherProfiles)).getAsJsonObject
    }catch{
      case e: JsonParseException => error("The launcher profile file is corrupted. Re-run the minecraft launcher to fix it!")
      case e: Exception => throw new RuntimeException(e)
    }

    val nailedGameDir = new File(target, Installer.gameDir)
    nailedGameDir.mkdirs()

    val profileName = "Nailed"

    val nailedProfile = new JsonObject
    nailedProfile.addProperty("name", profileName)
    nailedProfile.addProperty("gameDir", nailedGameDir.getAbsolutePath)
    nailedProfile.addProperty("lastVersionId", libList.versionName)

    jsonProfileData.getAsJsonObject("profiles").add(profileName, nailedProfile)

    try{
      val writer = new FileWriter(launcherProfiles)
      Installer.gson.toJson(jsonProfileData, writer)
      writer.close()
    }catch{
      case e: Exception => error("There was a problem writing the launch profile, is it write protected?")
    }

    println("Waiting until all downloads are finished")
    latch.await()
    println("All downloads are finished (Took " + (System.currentTimeMillis() - startTime) + "ms)")

    monitor.close()
    true
  }
}

object AbortInstallException extends RuntimeException {
  override def fillInStackTrace(): Throwable = this
  override def initCause(cause: Throwable): Throwable = this
}
