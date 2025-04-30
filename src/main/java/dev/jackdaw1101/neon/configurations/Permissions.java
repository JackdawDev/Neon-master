package dev.jackdaw1101.neon.configurations;

import dev.jackdaw1101.neon.Neon;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Permissions {
    private final Neon plugin;
    private final Map<String, String> permission = new HashMap<>();

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

        File permissionFile = new File(pluginDir, "permissions.yml");

        if (!permissionFile.exists()) {
            this.plugin.saveResource("permissions.yml", false);
        }

        FileConfiguration permissionConfig = YamlConfiguration.loadConfiguration(permissionFile);

        for (String key : permissionConfig.getKeys(true)) {
            if (permissionConfig.isString(key)) {
                this.permission.put(key, permissionConfig.getString(key));
            }
        }
    }

    public String getPermission(String key) {
        return this.permission.getOrDefault(key, key);
    }

    public boolean reloadPermissions() {
        File serverDir = Bukkit.getServer().getWorldContainer();
        File pluginsDir = new File(serverDir, "plugins");
        String pluginName = "NeonLoader";
        File pluginDir = new File(pluginsDir, pluginName);

        File permissionFile = new File(pluginDir, "permissions.yml");


        this.plugin.reloadConfig();

        this.loadPermissions();
        return true;
    }
}
