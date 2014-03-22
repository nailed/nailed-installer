package jk_5.nailed.installer

import javax.swing._
import java.io.File
import java.awt.{Color, Component}
import java.awt.event.ActionEvent
import javax.swing.border.LineBorder

/**
 * No description given
 *
 * @author jk-5
 */
class InstallerPanel(var targetDir: File) extends JPanel {

  private var dialog: JDialog = _

  this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  val tagLabel = new JLabel("Welcome to the Nailed installer!")
  tagLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
  tagLabel.setAlignmentY(Component.TOP_ALIGNMENT)
  this.add(tagLabel)
  val versionLabel = new JLabel(Installer.list.get.versionName)
  versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
  versionLabel.setAlignmentY(Component.TOP_ALIGNMENT)
  this.add(versionLabel)

  val entryPanel = new JPanel()
  entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.X_AXIS))
  val selectedDirTb = new JTextField()
  selectedDirTb.setEditable(false)
  selectedDirTb.setToolTipText("Path to the minecraft folder")
  selectedDirTb.setColumns(30)
  entryPanel.add(selectedDirTb)

  val btnSelectDir = new JButton()
  btnSelectDir.setAction(new ActionSelectFile)
  btnSelectDir.setText("...")
  btnSelectDir.setToolTipText("Select an alternative minecraft directory")
  entryPanel.add(btnSelectDir)

  entryPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
  entryPanel.setAlignmentY(Component.TOP_ALIGNMENT)
  val infoLabel = new JLabel
  infoLabel.setHorizontalTextPosition(SwingConstants.LEFT)
  infoLabel.setVerticalTextPosition(SwingConstants.TOP)
  infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT)
  infoLabel.setAlignmentY(Component.TOP_ALIGNMENT)
  infoLabel.setForeground(Color.RED)
  infoLabel.setVisible(false)

  val fileEntryPanel = new JPanel
  fileEntryPanel.setLayout(new BoxLayout(fileEntryPanel, BoxLayout.Y_AXIS))
  fileEntryPanel.add(infoLabel)
  fileEntryPanel.add(Box.createVerticalGlue)
  fileEntryPanel.add(entryPanel)
  fileEntryPanel.setAlignmentX(Component.CENTER_ALIGNMENT)
  fileEntryPanel.setAlignmentY(Component.TOP_ALIGNMENT)
  this.add(fileEntryPanel)

  this.updateFilePath()

  def run(){
    val pane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION)
    val frame = new JFrame()
    frame.setUndecorated(true)
    frame.setVisible(true)
    frame.setLocationRelativeTo(null)
    this.dialog = pane.createDialog(frame, "Nailed Installer")
    this.dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    this.dialog.setVisible(true)
    val result = Option(pane.getValue).getOrElse(-1)
    if(result == JOptionPane.OK_OPTION){
      if(InstallTaskClient.runInstall(targetDir)){
        JOptionPane.showMessageDialog(null, "Finished downloading and installing Nailed", "Done", JOptionPane.INFORMATION_MESSAGE)
      }else{
        JOptionPane.showMessageDialog(null, "An error has occurred downloading and installing Nailed", "Error", JOptionPane.ERROR_MESSAGE)
      }
    }
    this.dialog.dispose()
    frame.dispose()
    sys.exit(0)
  }

  def updateFilePath(){
    try{
      targetDir = targetDir.getCanonicalFile
      selectedDirTb.setText(targetDir.getPath)
    }catch{
      case e: Exception => //NOOP
    }
    if(this.isPathValid(targetDir)) {
      selectedDirTb.setForeground(Color.BLACK)
      infoLabel.setVisible(false)
      fileEntryPanel.setBorder(null)
    }else{
      selectedDirTb.setForeground(Color.RED)
      fileEntryPanel.setBorder(new LineBorder(Color.RED))
      if(this.targetDir.exists()){
        infoLabel.setText("<html>The selected directory is missing a launcher profile. Please run the minecraft launcher once to generate one</html>")
      }else{
        infoLabel.setText("<html>The selected directory does not exist. Select an alternative or run the minecraft launcher once to generate the folder</html>")
      }
      infoLabel.setVisible(true)
    }
    if(dialog != null){
      dialog.invalidate()
      dialog.pack()
    }
  }

  @inline def isPathValid(file: File) = file.exists() && new File(file, "launcher_profiles.json").exists()

  private class ActionSelectFile extends AbstractAction {
    override def actionPerformed(e: ActionEvent){
      val chooser = new JFileChooser()
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
      chooser.setFileHidingEnabled(false)
      chooser.ensureFileIsVisible(targetDir)
      chooser.setSelectedFile(targetDir)
      val response = chooser.showOpenDialog(InstallerPanel.this)
      if(response == JFileChooser.APPROVE_OPTION){
        targetDir = chooser.getSelectedFile
        updateFilePath()
      }
    }
  }
}
