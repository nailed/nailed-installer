package jk_5.nailed.installer;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class DownloadUtils {

    public static int downloadInstalledLibraries(String jsonMarker, File librariesDir, DownloadMonitor monitor, JsonArray libraries, int progress, List<String> grabbed, List<String> bad){
        for(JsonElement l : libraries){
            JsonObject library = l.getAsJsonObject();
            String libName = library.get("name").getAsString();
            monitor.setNote(String.format("Considering library %s", libName));
            if(library.has(jsonMarker) && library.get(jsonMarker).isJsonPrimitive() && library.get(jsonMarker).getAsBoolean()){
                String[] nameparts = Iterables.toArray(Splitter.on(':').split(libName), String.class);
                nameparts[0] = nameparts[0].replace('.', '/');
                String jarName = nameparts[1] + '-' + nameparts[2] + ".jar";
                String pathName = nameparts[0] + '/' + nameparts[1] + '/' + nameparts[2] + '/' + jarName;
                File libPath = new File(librariesDir, pathName.replace('/', File.separatorChar));
                String libURL = "https://libraries.minecraft.net/";
                if(library.has("url")){
                    libURL = library.get("url").getAsString() + "/";
                }
                if(libPath.exists()){
                    monitor.setProgress(progress++);
                    continue;
                }

                libPath.getParentFile().mkdirs();
                monitor.setNote(String.format("Downloading library %s", libName));
                libURL += pathName;
                if(library.has("url")){
                    monitor.setNote(String.format("Trying library %s", libName));
                }
                if(!downloadFile(libPath, libURL)){
                    bad.add(libName);
                }else{
                    grabbed.add(libName);
                }
            }
            monitor.setProgress(progress++);
        }
        return progress;
    }

    public static boolean downloadFile(File libPath, String libURL){
        try{
            URL url = new URL(libURL);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            InputSupplier<InputStream> urlSupplier = new URLISSupplier(connection);
            Files.copy(urlSupplier, libPath);
            return true;
        }catch(FileNotFoundException fnf){
            return false;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    static class URLISSupplier implements InputSupplier<InputStream> {
        private final URLConnection connection;

        private URLISSupplier(URLConnection connection){
            this.connection = connection;
        }

        @Override
        public InputStream getInput() throws IOException{
            return connection.getInputStream();
        }
    }
}
