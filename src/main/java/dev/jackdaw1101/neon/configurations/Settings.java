package dev.jackdaw1101.neon.configurations;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.api.utils.CC;
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

        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
        }

        settingsFile = new File(pluginDir, "settings.yml");

        if (!settingsFile.exists()) {
            plugin.saveResource("settings.yml", false);
        }

        settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);

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


        if (comment != null && !comment.isEmpty()) {
        }

        saveSettings();
    }

    public FileConfiguration getSettingsConfig() {
        return settingsConfig;
    }

    public boolean reloadSettings() throws IOException {
        settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);

        ConfigUpdater.update(this.plugin, "settings.yml", settingsFile);

        this.plugin.reloadConfig();

        return true;

    }
}
