package jk_5.nailed.installer;

import com.google.common.base.Throwables;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class InstallerPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private File targetDir;
    private JTextField selectedDirText;
    private JLabel infoLabel;
    private JDialog dialog;
    private JPanel fileEntryPanel;

    private class FileSelectAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dirChooser.setFileHidingEnabled(false);
            dirChooser.ensureFileIsVisible(targetDir);
            dirChooser.setSelectedFile(targetDir);
            int response = dirChooser.showOpenDialog(InstallerPanel.this);
            switch (response) {
                case JFileChooser.APPROVE_OPTION:
                    targetDir = dirChooser.getSelectedFile();
                    updateFilePath();
                    break;
                default:
                    break;
            }
        }
    }

    public InstallerPanel(File targetDir) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        BufferedImage image;
        try {
            image = ImageIO.read(SimpleInstaller.class.getResourceAsStream("/big_logo.png"));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }

        JPanel logoSplash = new JPanel();
        logoSplash.setLayout(new BoxLayout(logoSplash, BoxLayout.Y_AXIS));
        ImageIcon icon = new ImageIcon(image);
        JLabel logoLabel = new JLabel(icon);
        logoLabel.setAlignmentX(CENTER_ALIGNMENT);
        logoLabel.setAlignmentY(CENTER_ALIGNMENT);
        logoLabel.setSize(image.getWidth(), image.getHeight());
        logoSplash.add(logoLabel);
        JLabel tag = new JLabel("Welcome to the nailed installer!");
        tag.setAlignmentX(CENTER_ALIGNMENT);
        tag.setAlignmentY(CENTER_ALIGNMENT);
        logoSplash.add(tag);
        tag = new JLabel(VersionInfo.getVersion());
        tag.setAlignmentX(CENTER_ALIGNMENT);
        tag.setAlignmentY(CENTER_ALIGNMENT);
        logoSplash.add(tag);

        logoSplash.setAlignmentX(CENTER_ALIGNMENT);
        logoSplash.setAlignmentY(TOP_ALIGNMENT);
        this.add(logoSplash);

        JPanel entryPanel = new JPanel();
        entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.X_AXIS));

        this.targetDir = targetDir;
        selectedDirText = new JTextField();
        selectedDirText.setEditable(false);
        selectedDirText.setToolTipText("Path to minecraft");
        selectedDirText.setColumns(30);

        entryPanel.add(selectedDirText);
        JButton dirSelect = new JButton();
        dirSelect.setAction(new FileSelectAction());
        dirSelect.setText("...");
        dirSelect.setToolTipText("Select an alternative minecraft directory");
        entryPanel.add(dirSelect);

        entryPanel.setAlignmentX(LEFT_ALIGNMENT);
        entryPanel.setAlignmentY(TOP_ALIGNMENT);
        infoLabel = new JLabel();
        infoLabel.setHorizontalTextPosition(JLabel.LEFT);
        infoLabel.setVerticalTextPosition(JLabel.TOP);
        infoLabel.setAlignmentX(LEFT_ALIGNMENT);
        infoLabel.setAlignmentY(TOP_ALIGNMENT);
        infoLabel.setForeground(Color.RED);
        infoLabel.setVisible(false);

        fileEntryPanel = new JPanel();
        fileEntryPanel.setLayout(new BoxLayout(fileEntryPanel, BoxLayout.Y_AXIS));
        fileEntryPanel.add(infoLabel);
        fileEntryPanel.add(Box.createVerticalGlue());
        fileEntryPanel.add(entryPanel);
        fileEntryPanel.setAlignmentX(CENTER_ALIGNMENT);
        fileEntryPanel.setAlignmentY(TOP_ALIGNMENT);
        this.add(fileEntryPanel);
        updateFilePath();
    }


    private void updateFilePath() {
        try {
            targetDir = targetDir.getCanonicalFile();
            selectedDirText.setText(targetDir.getPath());
        } catch (IOException e) {
            //NOOP
        }

        if (SimpleInstaller.install.isPathValid(targetDir)) {
            selectedDirText.setForeground(Color.BLACK);
            infoLabel.setVisible(false);
            fileEntryPanel.setBorder(null);
        } else {
            selectedDirText.setForeground(Color.RED);
            fileEntryPanel.setBorder(new LineBorder(Color.RED));
            infoLabel.setText("<html>" + SimpleInstaller.install.getFileError(targetDir) + "</html>");
            infoLabel.setVisible(true);
        }
        if (dialog != null) {
            dialog.invalidate();
            dialog.pack();
        }
    }

    public void run() {
        JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

        Frame emptyFrame = new Frame("Nailed installer");
        emptyFrame.setUndecorated(true);
        emptyFrame.setVisible(true);
        emptyFrame.setLocationRelativeTo(null);
        dialog = optionPane.createDialog(emptyFrame, "Nailed installer");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
        int result = (Integer) (optionPane.getValue() != null ? optionPane.getValue() : -1);
        if (result == JOptionPane.OK_OPTION) {
            if(SimpleInstaller.install.run(targetDir)){
                JOptionPane.showMessageDialog(null, SimpleInstaller.install.getSuccessMessage(), "Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        dialog.dispose();
        emptyFrame.dispose();
    }
}
