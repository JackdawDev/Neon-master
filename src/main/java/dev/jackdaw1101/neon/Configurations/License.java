package dev.jackdaw1101.neon.Configurations;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.jackdaw1101.neon.Neon;
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

    public License(Neon plugin) {
        this.plugin = plugin;
        this.loadLicense();
    }

    private void loadLicense() {
        File LicenseFile = new File(this.plugin.getDataFolder(), "license.yml");
        if (!LicenseFile.exists()) {
            this.plugin.saveResource("license.yml", false); // Save the default file if it doesn't exist
        }

        FileConfiguration licenseConfig = YamlConfiguration.loadConfiguration(LicenseFile);

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


