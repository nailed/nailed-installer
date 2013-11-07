package jk_5.nailed.installer;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
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
        return INSTANCE.versionData.getStringValue("install", "target");
    }

    public static File getLibraryPath(File root) {
        String path = INSTANCE.versionData.getStringValue("install", "path");
        String[] split = Iterables.toArray(Splitter.on(':').omitEmptyStrings().split(path), String.class);
        File dest = root;
        Iterable<String> subSplit = Splitter.on('.').omitEmptyStrings().split(split[0]);
        for (String part : subSplit) {
            dest = new File(dest, part);
        }
        dest = new File(new File(dest, split[1]), split[2]);
        String fileName = split[1] + "-" + split[2] + ".jar";
        return new File(dest, fileName);
    }

    public static String getVersion() {
        return INSTANCE.versionData.getStringValue("install", "version");
    }

    public static JsonNode getVersionInfo() {
        return INSTANCE.versionData.getNode("versionInfo");
    }

    public static void extractFile(File path, String file) throws IOException {
        InputStream inputStream = VersionInfo.class.getResourceAsStream(file);
        OutputSupplier<FileOutputStream> outputSupplier = Files.newOutputStreamSupplier(path);
        ByteStreams.copy(inputStream, outputSupplier);
    }

    public static String getMinecraftVersion() {
        return INSTANCE.versionData.getStringValue("install", "minecraft");
    }
}
