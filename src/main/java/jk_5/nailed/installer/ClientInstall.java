package jk_5.nailed.installer;

import argo.format.PrettyJsonFormatter;
import argo.jdom.*;
import argo.saj.InvalidSyntaxException;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;

public class ClientInstall {

    private List<String> grabbed;

    public boolean run(File target) {
        //Check if minecraft is installed at this location
        if (!target.exists()) {
            JOptionPane.showMessageDialog(null, "There is no minecraft installation at this location!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Check if the profiles file exists
        File launcherProfiles = new File(target, "launcher_profiles.json");
        if (!launcherProfiles.exists()) {
            JOptionPane.showMessageDialog(null, "There is no minecraft launcher profile at this location, you need to run the launcher first!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Check if something already exists at our location
        File versionRootDir = new File(target, "versions");
        File versionTarget = new File(versionRootDir, VersionInfo.getVersionTarget());
        if (!versionTarget.mkdirs() && !versionTarget.isDirectory()) {
            if (!versionTarget.delete()) {
                JOptionPane.showMessageDialog(null, "There was a problem with the launcher version data. You will need to clear " + versionTarget.getAbsolutePath() + " manually", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                versionTarget.mkdirs();
            }
        }

        //Copy the minecraft file to the desired location
        File versionJsonFile = new File(versionTarget, VersionInfo.getVersionTarget() + ".json");
        File clientJarFile = new File(versionTarget, VersionInfo.getVersionTarget() + ".jar");
        try {
            VersionInfo.extractFile(clientJarFile, "/dummy.jar");
        } catch (IOException e1) {
            return false;
        }
        File librariesDir = new File(target, "libraries");
        DownloadMonitor monitor = new DownloadMonitor();
        List<JsonNode> libraries = VersionInfo.getVersionInfo().getArrayNode("libraries");
        monitor.setMaximum(libraries.size() + 2);
        int progress = 2;
        grabbed = Lists.newArrayList();
        List<String> bad = Lists.newArrayList();
        progress = DownloadUtils.downloadInstalledLibraries("clientreq", librariesDir, monitor, libraries, progress, grabbed, bad);

        monitor.close();
        if (bad.size() > 0) {
            String list = Joiner.on(", ").join(bad);
            JOptionPane.showMessageDialog(null, "These libraries failed to download. Try again.\n" + list, "Error downloading", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        JsonRootNode versionJson = JsonNodeFactories.object(VersionInfo.getVersionInfo().getFields());

        try {
            BufferedWriter newWriter = Files.newWriter(versionJsonFile, Charsets.UTF_8);
            PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(versionJson, newWriter);
            newWriter.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "There was a problem writing the launcher version data,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        JdomParser parser = new JdomParser();
        JsonRootNode jsonProfileData;

        try {
            jsonProfileData = parser.parse(Files.newReader(launcherProfiles, Charsets.UTF_8));
        } catch (InvalidSyntaxException e) {
            JOptionPane.showMessageDialog(null, "The launcher profile file is corrupted. Re-run the minecraft launcher to fix it!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        File nailedGameDir = new File(launcherProfiles.getParentFile(), "Nailed");

        JsonField[] fields = new JsonField[]{
                JsonNodeFactories.field("name", JsonNodeFactories.string("Nailed")),
                JsonNodeFactories.field("gameDir", JsonNodeFactories.string(nailedGameDir.getAbsolutePath())),
                JsonNodeFactories.field("lastVersionId", JsonNodeFactories.string(VersionInfo.getVersionTarget())),
        };

        HashMap<JsonStringNode, JsonNode> profileCopy = Maps.newHashMap(jsonProfileData.getNode("profiles").getFields());
        HashMap<JsonStringNode, JsonNode> rootCopy = Maps.newHashMap(jsonProfileData.getFields());
        profileCopy.put(JsonNodeFactories.string("Nailed"), JsonNodeFactories.object(fields));
        JsonRootNode profileJsonCopy = JsonNodeFactories.object(profileCopy);

        rootCopy.put(JsonNodeFactories.string("profiles"), profileJsonCopy);

        jsonProfileData = JsonNodeFactories.object(rootCopy);

        try {
            BufferedWriter newWriter = Files.newWriter(launcherProfiles, Charsets.UTF_8);
            PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(jsonProfileData, newWriter);
            newWriter.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "There was a problem writing the launch profile,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public boolean isPathValid(File targetDir) {
        if (targetDir.exists()) {
            File launcherProfiles = new File(targetDir, "launcher_profiles.json");
            return launcherProfiles.exists();
        }
        return false;
    }


    public String getFileError(File targetDir) {
        if (targetDir.exists()) {
            return "The directory is missing a launcher profile. Please run the minecraft launcher first";
        } else {
            return "There is no minecraft directory set up. Either choose an alternative, or run the minecraft launcher to create one";
        }
    }

    public String getSuccessMessage() {
        return String.format("Successfully installed client profile Nailed for version %s into launcher and grabbed %d required libraries", VersionInfo.getVersionTarget(), grabbed.size());
    }
}
