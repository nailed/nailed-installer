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

public class VersionInfo {

    public static final VersionInfo INSTANCE = new VersionInfo();
    public final JsonRootNode versionData;

    public VersionInfo() {
        InputStream installProfile = getClass().getResourceAsStream("/install_profile.json");
        JdomParser parser = new JdomParser();

        try {
            versionData = parser.parse(new InputStreamReader(installProfile, Charsets.UTF_8));
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
