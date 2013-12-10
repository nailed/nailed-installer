package jk_5.nailed.installer;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class VersionInfo {

    public static final VersionInfo INSTANCE = new VersionInfo();
    public final JsonObject versionData;

    public VersionInfo(){
        JsonParser parser = new JsonParser();
        try{
            URLConnection conn = new URL("http://maven.reening.nl/nailed/launcherProfile.json").openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            versionData = parser.parse(new InputStreamReader(conn.getInputStream(), Charsets.UTF_8)).getAsJsonObject();
            conn.getInputStream().close();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static String getVersionTarget(){
        return INSTANCE.versionData.get("id").getAsString();
    }

    public static JsonObject getVersionInfo(){
        return INSTANCE.versionData;
    }

    public static void extractFile(File path, String file) throws IOException{
        InputStream inputStream = VersionInfo.class.getResourceAsStream(file);
        OutputSupplier<FileOutputStream> outputSupplier = Files.newOutputStreamSupplier(path);
        ByteStreams.copy(inputStream, outputSupplier);
    }
}
