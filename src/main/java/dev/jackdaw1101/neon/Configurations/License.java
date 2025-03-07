package dev.jackdaw1101.neon.Configurations;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.jackdaw1101.neon.Neon;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class License {
    private final Neon plugin;
    private final Map<String, String> license = new HashMap<>();
    private File licenseFile;

    public License(Neon plugin) {
        this.plugin = plugin;
        this.loadLicense();
    }

    private void loadLicense() {
        File serverDir = Bukkit.getServer().getWorldContainer();
        File pluginsDir = new File(serverDir, "plugins");
        String pluginName = "NeonLoader";
        File pluginDir = new File(pluginsDir, pluginName);

        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
        }

        licenseFile = new File(pluginDir, "license.yml");

        FileConfiguration licenseConfig = YamlConfiguration.loadConfiguration(licenseFile);

        for (String key : licenseConfig.getKeys(true)) {
            if (licenseConfig.isString(key)) {
                this.license.put(key, licenseConfig.getString(key));
            }
        }
    }

    public String getLicense(String key, Object... placeholders) {
        String license = this.license.getOrDefault(key, key); // Get the message or use the key as fallback
        return license;
    }
}


