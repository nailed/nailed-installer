package jk_5.nailed.installer;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.*;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ClientInstall {

    private List<String> grabbed;

    public boolean run(File target){
        //Check if minecraft is installed at this location
        if(!target.exists()){
            JOptionPane.showMessageDialog(null, "There is no minecraft installation at this location!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Check if the profiles file exists
        File launcherProfiles = new File(target, "launcher_profiles.json");
        if(!launcherProfiles.exists()){
            JOptionPane.showMessageDialog(null, "There is no minecraft launcher profile at this location, you need to run the launcher first!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Check if something already exists at our location
        File versionRootDir = new File(target, "versions");
        File versionTarget = new File(versionRootDir, VersionInfo.getVersionTarget());
        if(!versionTarget.mkdirs() && !versionTarget.isDirectory()){
            if(!versionTarget.delete()){
                JOptionPane.showMessageDialog(null, "There was a problem with the launcher version data. You will need to clear " + versionTarget.getAbsolutePath() + " manually", "Error", JOptionPane.ERROR_MESSAGE);
            }else{
                versionTarget.mkdirs();
            }
        }

        //Copy the minecraft file to the desired location
        File versionJsonFile = new File(versionTarget, VersionInfo.getVersionTarget() + ".json");
        File clientJarFile = new File(versionTarget, VersionInfo.getVersionTarget() + ".jar");
        try{
            VersionInfo.extractFile(clientJarFile, "/dummy.jar");
        }catch(IOException e1){
            return false;
        }
        File librariesDir = new File(target, "libraries");
        DownloadMonitor monitor = new DownloadMonitor();
        JsonArray libraries = VersionInfo.getVersionInfo().getAsJsonArray("libraries");
        monitor.setMaximum(libraries.size() + 2);
        int progress = 2;
        grabbed = Lists.newArrayList();
        List<String> bad = Lists.newArrayList();
        progress = DownloadUtils.downloadInstalledLibraries("clientreq", librariesDir, monitor, libraries, progress, grabbed, bad);

        monitor.close();
        if(bad.size() > 0){
            String list = Joiner.on(", ").join(bad);
            JOptionPane.showMessageDialog(null, "These libraries failed to download. Try again.\n" + list, "Error downloading", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try{
            BufferedWriter newWriter = Files.newWriter(versionJsonFile, Charsets.UTF_8);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(VersionInfo.getVersionInfo(), newWriter);
            newWriter.close();
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "There was a problem writing the launcher version data,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        JsonParser parser = new JsonParser();
        JsonObject jsonProfileData;

        try{
            jsonProfileData = parser.parse(Files.newReader(launcherProfiles, Charsets.UTF_8)).getAsJsonObject();
        }catch(JsonParseException e){
            JOptionPane.showMessageDialog(null, "The launcher profile file is corrupted. Re-run the minecraft launcher to fix it!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }catch(Exception e){
            throw Throwables.propagate(e);
        }

        File nailedGameDir = new File(new File(launcherProfiles.getParentFile(), "Nailed"), "runtime");
        nailedGameDir.mkdirs();

        String profileName = "Nailed";

        JsonObject nailedProfile = new JsonObject();
        nailedProfile.addProperty("name", profileName);
        nailedProfile.addProperty("gameDir", nailedGameDir.getAbsolutePath());
        nailedProfile.addProperty("lastVersionId", VersionInfo.getVersionTarget());

        jsonProfileData.getAsJsonObject("profiles").add(profileName, nailedProfile);

        try{
            BufferedWriter newWriter = Files.newWriter(launcherProfiles, Charsets.UTF_8);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonProfileData, newWriter);
            newWriter.close();
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "There was a problem writing the launch profile,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public boolean isPathValid(File targetDir){
        return targetDir.exists() && new File(targetDir, "launcher_profiles.json").exists();
    }


    public String getFileError(File targetDir){
        if(targetDir.exists()){
            return "The directory is missing a launcher profile. Please run the minecraft launcher first";
        }else{
            return "There is no minecraft directory set up. Either choose an alternative, or run the minecraft launcher to create one";
        }
    }

    public String getSuccessMessage(){
        return String.format("Successfully installed client profile Nailed for version %s into launcher and grabbed %d required libraries", VersionInfo.getVersionTarget(), grabbed.size());
    }
}
