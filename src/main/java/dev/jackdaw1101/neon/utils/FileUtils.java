package dev.jackdaw1101.neon.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List<String> getYmlFiles(File pluginFolder) {
        List<String> ymlFiles = new ArrayList<>();

        if (!pluginFolder.exists() || !pluginFolder.isDirectory()) {
            return ymlFiles;
        }

        File[] files = pluginFolder.listFiles((dir, name) -> name.endsWith(".yml") && !name.equalsIgnoreCase("plugin.yml"));
        if (files != null) {
            for (File file : files) {
                ymlFiles.add(file.getName());
            }
        }

        return ymlFiles;
    }
}

