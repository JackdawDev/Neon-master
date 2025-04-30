package dev.jackdaw1101.neon.configurations;

import dev.jackdaw1101.neon.Neon;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
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

        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
        }

        File discordFile = new File(pluginDir, "discord.yml");

        if (!discordFile.exists()) {
            this.plugin.saveResource("discord.yml", false);
        }

        FileConfiguration discordConfig = YamlConfiguration.loadConfiguration(discordFile);

        for (String key : discordConfig.getKeys(true)) {
            if (discordConfig.isString(key)) {
                this.discord.put(key, discordConfig.getString(key));
            }
        }
    }

    public String getDiscord(String key, Object... placeholders) {
        String discord = this.discord.getOrDefault(key, key);
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

        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
        }

        File discordFile = new File(pluginDir, "discord.yml");

        try {

            this.plugin.reloadConfig();





            this.loadDiscord();
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }}


