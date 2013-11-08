package jk_5.nailed.installer;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class SimpleInstaller {

    public static ClientInstall install = new ClientInstall();

    public static void main(String[] args) throws IOException {
        String userHomeDir = System.getProperty("user.home", ".");
        String osType = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        File targetDir;
        String mcDir = ".minecraft";
        if (osType.contains("win") && System.getenv("APPDATA") != null) {
            targetDir = new File(System.getenv("APPDATA"), mcDir);
        } else if (osType.contains("mac")) {
            targetDir = new File(new File(new File(userHomeDir, "Library"), "Application Support"), "minecraft");
        } else {
            targetDir = new File(userHomeDir, mcDir);
        }

        try {
            VersionInfo.getVersionTarget();
        } catch (Throwable e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Corrupt download detected, cannot install", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            //NOOP
        }

        InstallerPanel panel = new InstallerPanel(targetDir);
        panel.run();
    }
}
