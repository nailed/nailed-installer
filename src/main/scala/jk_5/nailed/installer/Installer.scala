package jk_5.nailed.installer

import java.io.File
import scala.util.Properties
import com.google.gson.GsonBuilder
import javax.swing.JOptionPane

/**
 * No description given
 *
 * @author jk-5
 */
object Installer extends App {

  val gameDir = "Nailed/runtime"
  val gson = new GsonBuilder().setPrettyPrinting().create()
  val minecraftHome = {
    val userHome = Properties.propOrElse("user.home", ".")
    if(Properties.isWin && Properties.propIsSet("APPDATA")){
      new File(Properties.propOrNull("APPDATA"), ".minecraft")
    }else if(Properties.isMac){
      new File(new File(new File(userHome, "Library"), "Application Support"), "minecraft")
    }else{
      new File(userHome, ".minecraft")
    }
  }

  println("Reading remote version info")
  val list = LibraryList.readFromUrl("http://maven.reening.nl/nailed/versions-2.json")
  if(list.isEmpty){
    JOptionPane.showMessageDialog(null, "Remote versions file could not be read. Are you connected to the internet?", "Error", JOptionPane.ERROR_MESSAGE)
    sys.exit(0)
  }

  new InstallerPanel(minecraftHome).run()

  def resolve(input: String): File = {
    var dir = minecraftHome.getAbsolutePath + "/" + input
    dir = dir.replace("{MC_LIB_DIR}", "libraries")
    dir = dir.replace("{MC_GAME_DIR}", this.gameDir)
    dir = dir.replace("{MC_VERSION_DIR}", "versions/" + list.get.versionName)
    dir = dir.replace("{MC_VERSION_NAME}", list.get.versionName)
    new File(dir)
  }

  def stripTrailing(in: String) = if(in.endsWith("/")) in.substring(0, in.length() - 1) else in
}
