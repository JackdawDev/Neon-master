package dev.jackdaw1101.neon.Configurations;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Settings {

    private final Neon plugin;
    private File settingsFile;
    private FileConfiguration settingsConfig;

    public Settings(Neon plugin) {
        this.plugin = plugin;
        createSettingsFile();
    }

    private void createSettingsFile() {
        File serverDir = Bukkit.getServer().getWorldContainer();
        File pluginsDir = new File(serverDir, "plugins");
        String pluginName = "NeonLoader";
        File pluginDir = new File(pluginsDir, pluginName);

        settingsFile = new File(pluginDir, "settings.yml");

        if (!settingsFile.exists()) {
            plugin.saveResource("settings.yml", false); // This saves the default settings.yml from resources
        }

        settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);

        ConfigUpdate();
    }

    public void ConfigUpdate() {
        settingsConfig.options().copyDefaults(true);
    }

    public void saveSettings() {
        try {
            settingsConfig.save(settingsFile);
        } catch (IOException e) {
            plugin.getLogger().severe(CC.BD_RED + "Can't Save The File");
            e.printStackTrace();
        }
    }

    public Object getValue(String path) {
        return settingsConfig.get(path);
    }

    public Object getValue(String path, Object defaultValue) {
        return settingsConfig.get(path, defaultValue);
    }

    public void addValue(String path, Object value, String comment) {
        settingsConfig.set(path, value);

        // Add the comment
        if (comment != null && !comment.isEmpty()) {
        }

        saveSettings();
    }

    public FileConfiguration getSettingsConfig() {
        return settingsConfig;
    }

    public boolean reloadSettings() {
        try {
            settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);

            ConfigUpdater.update(this.plugin, "settings.yml", settingsFile); // Assuming this is a utility method

            this.plugin.reloadConfig();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
