package dev.jackdaw1101.neon.Configurations;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Locales {

    private final Neon plugin;
    private File LocaleConfig;
    private FileConfiguration LocaleFile;

    private static final String HEADER =
            "########################################################\n" +
                    "# _   _                                           #\n" +
                    "#| \\ | | ___  ___  _ __                              #\n" +
                    "#|  \\| |/ _ \\/ _ \\| '_ \\   by Jackdaw1101        #\n" +
                    "#| |\\  |  __/ (_) | | | |                           #\n" +
                    "#|_| \\_|\\___|\\___/|_| |_|  (0.0.1-fe45650)         #\n" +
                    "########################################################";

    public Locales(Neon plugin) {
        this.plugin = plugin;
        createLocalesFile();
    }

    public void createLocalesFile() {
        File serverDir = Bukkit.getServer().getWorldContainer();
        File pluginsDir = new File(serverDir, "plugins");
        String pluginName = "NeonLoader";
        File pluginDir = new File(pluginsDir, pluginName);

        LocaleConfig = new File(pluginDir, "locale.yml");

        if (!LocaleConfig.exists()) {
            plugin.saveResource("locale.yml", false); // This saves the default settings.yml from resources
        }

        LocaleFile = YamlConfiguration.loadConfiguration(LocaleConfig);

        ConfigUpdate();
    }

    public void ConfigUpdate() {
        LocaleFile.options().copyDefaults(true);

        plugin.saveResource("locale.yml", false);

        saveLocales();
    }

    public void saveLocales() {
        try {
            LocaleFile.save(LocaleConfig);
        } catch (IOException e) {
            plugin.getLogger().severe(CC.BD_RED + "[Neon] Can't Save The File");
            e.printStackTrace();
        }
    }

    public Object getValue(String path) {
        return LocaleFile.get(path);
    }

    public Object getValue(String path, Object defaultValue) {
        return LocaleFile.get(path, defaultValue);
    }


    public void addValue(String path, Object value, String comment) {
        LocaleFile.set(path, value);

        // Add the comment
        if (comment != null && !comment.isEmpty()) {
        }

        saveLocales();
    }


    public FileConfiguration getLocalesConfig() {
        return LocaleFile;
    }


    public boolean reloadLocales() {
        try {
            LocaleFile = YamlConfiguration.loadConfiguration(LocaleConfig);

            if (!LocaleConfig.exists()) {
                plugin.saveResource("locale.yml", false);
            }

            File defaultConfigFile = new File(plugin.getDataFolder(), "locale.yml");
            if (defaultConfigFile.exists()) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigFile);
                LocaleFile.setDefaults(defaultConfig);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
