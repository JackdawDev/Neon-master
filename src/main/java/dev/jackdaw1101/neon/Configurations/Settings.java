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

    private static final String HEADER =
            "########################################################\n" +
                    "# _   _   by Jackdaw1101                             #\n" +
                    "#| \\ | | ___  ___  _ __      Main Configuration For  #\n" +
                    "#|  \\| |/ _ \\/ _ \\| '_ \\     NeonPlugin!            #\n" +
                    "#| |\\  |  __/ (_) | | | |                             #\n" +
                    "#|_| \\_|\\___|\\___/|_| |_|  (0.0.1-fe45618)         #\n" +
                    "########################################################";

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

        File settingsFile = new File(pluginDir, "permissions.yml");

        settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);

        if (!settingsConfig.contains("header")) {
            settingsConfig.options().header(HEADER);
        }

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
