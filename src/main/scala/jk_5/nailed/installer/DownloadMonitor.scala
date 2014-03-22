package jk_5.nailed.installer

import javax.swing.ProgressMonitor

/**
 * No description given
 *
 * @author jk-5
 */
class DownloadMonitor {
  private final val monitor = new ProgressMonitor(null, "Downloading libraries", "Libraries are being analyzed", 0, 1)
  monitor.setMillisToPopup(0)
  monitor.setMillisToDecideToPopup(0)

  def setNote(note: String){
    println(note)
    monitor.setNote(note)
  }

  def setMaximum(max: Int) = monitor.setMaximum(max)
  def setProgress(progress: Int) = monitor.setProgress(progress)
  def close() = monitor.close()
}
