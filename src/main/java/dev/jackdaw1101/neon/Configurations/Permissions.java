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

public class Permissions {
    private final Neon plugin;
    private final Map<String, String> permission = new HashMap<>();
    private File permissionsFile;

    public Permissions(Neon plugin) {
        this.plugin = plugin;
        this.loadPermissions();
    }


    private void loadPermissions() {
        File serverDir = Bukkit.getServer().getWorldContainer();
        File pluginsDir = new File(serverDir, "plugins");
        String pluginName = "NeonLoader";
        File pluginDir = new File(pluginsDir, pluginName);

        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
        }

        File permissionsFile = new File(pluginDir, "permissions.yml");

        FileConfiguration permissionConfig = YamlConfiguration.loadConfiguration(permissionsFile);

        for (String key : permissionConfig.getKeys(true)) {
            if (permissionConfig.isString(key)) {
                this.permission.put(key, permissionConfig.getString(key));
            }
        }
    }

    public String getPermission(String key) {
        String message = this.permission.getOrDefault(key, key); // Get the message or use the key as fallback
        return message;
    }


    public boolean reloadPermissions() {
        File permissionFile = new File(this.plugin.getDataFolder(), "permissions.yml");

        try {
            ConfigUpdater.update(this.plugin, "permissions.yml", permissionFile);

            this.plugin.reloadConfig();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        this.loadPermissions();

        return true;
    }
}


