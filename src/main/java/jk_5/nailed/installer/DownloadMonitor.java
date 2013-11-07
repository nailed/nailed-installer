package jk_5.nailed.installer;

import javax.swing.*;

/**
 * No description given
 *
 * @author jk-5
 */
public class DownloadMonitor {

    private final ProgressMonitor monitor;

    public DownloadMonitor(){
        monitor = new ProgressMonitor(null, "Downloading libraries", "Libraries are being analyzed", 0, 1);
        monitor.setMillisToPopup(0);
        monitor.setMillisToDecideToPopup(0);
    }

    public void setMaximum(int max) {
        monitor.setMaximum(max);
    }

    public void setNote(String note) {
        System.out.println(note);
        monitor.setNote(note);
    }

    public void setProgress(int progress) {
        monitor.setProgress(progress);
    }

    public void close() {
        monitor.close();
    }
}
