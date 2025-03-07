package dev.jackdaw1101.neon.Configurations;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.jackdaw1101.neon.Neon;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Discord {
    private final Neon plugin;
    private final Map<String, String> discord = new HashMap<>();

    public Discord(Neon plugin) {
        this.plugin = plugin;
        this.loadDiscord();
    }

    private void loadDiscord() {
        File serverDir = Bukkit.getServer().getWorldContainer();
        File pluginsDir = new File(serverDir, "plugins");
        String pluginName = "NeonLoader";
        File pluginDir = new File(pluginsDir, pluginName);

        File discordFile = new File(pluginDir, "discord.yml");

        if (!discordFile.exists()) {
            this.plugin.saveResource("discord.yml", false); // Save the default file if it doesn't exist
        }

        FileConfiguration discordConfig = YamlConfiguration.loadConfiguration(discordFile);

        for (String key : discordConfig.getKeys(true)) {
            if (discordConfig.isString(key)) {
                this.discord.put(key, discordConfig.getString(key));
            }
        }
    }

    public String getDiscord(String key, Object... placeholders) {
        String discord = this.discord.getOrDefault(key, key); // Get the message or use the key as fallback
        return discord;
    }

    public Object getValue(String path) {
        File discordFile = new File(this.plugin.getDataFolder(), "discord.yml");
        FileConfiguration discordConfig = YamlConfiguration.loadConfiguration(discordFile);
        return discordConfig.get(path);
    }

    public Object getValue(String path, Object defaultValue) {
        File discordFile = new File(this.plugin.getDataFolder(), "discord.yml");
        FileConfiguration discordConfig = YamlConfiguration.loadConfiguration(discordFile);
        return discordConfig.get(path, defaultValue);
    }

    public boolean reloadDiscord() {
        File serverDir = Bukkit.getServer().getWorldContainer();
        File pluginsDir = new File(serverDir, "plugins");
        String pluginName = "NeonLoader";
        File pluginDir = new File(pluginsDir, pluginName);

        File discordFile = new File(pluginDir, "discord.yml");

        try {
            ConfigUpdater.update(this.plugin, "discord.yml", discordFile);
            this.plugin.reloadConfig();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        this.loadDiscord();
        return true;
    }
}


