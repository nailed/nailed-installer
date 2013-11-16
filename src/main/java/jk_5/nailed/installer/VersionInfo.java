package jk_5.nailed.installer;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class VersionInfo {

    public static final VersionInfo INSTANCE = new VersionInfo();
    public final JsonRootNode versionData;

    public VersionInfo() {
        JdomParser parser = new JdomParser();
        try {
            URLConnection conn = new URL("http://maven.reening.nl/nailed/launcherProfile.json").openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            versionData = parser.parse(new InputStreamReader(conn.getInputStream(), Charsets.UTF_8));
            conn.getInputStream().close();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static String getVersionTarget() {
        return INSTANCE.versionData.getStringValue("id");
    }

    public static JsonNode getVersionInfo() {
        return INSTANCE.versionData;
    }

    public static void extractFile(File path, String file) throws IOException {
        InputStream inputStream = VersionInfo.class.getResourceAsStream(file);
        OutputSupplier<FileOutputStream> outputSupplier = Files.newOutputStreamSupplier(path);
        ByteStreams.copy(inputStream, outputSupplier);
    }
}
