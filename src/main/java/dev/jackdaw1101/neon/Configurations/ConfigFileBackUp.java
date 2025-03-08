package dev.jackdaw1101.neon.Configurations;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.Collection;
import java.util.List;

public class ConfigFileBackUp {

    private final String configName;
    protected File file;
    protected YamlConfiguration config;

    @SneakyThrows
    public ConfigFileBackUp(String configName) {
        this.configName = configName;

        File serverDir = Bukkit.getServer().getWorldContainer();
        File pluginsDir = new File(serverDir, "plugins");
        String pluginName = "NeonLoader";
        File pluginDir = new File(pluginsDir, pluginName);

        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
        }

        file = new File(pluginDir, configName);

        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error initializing the config file: " + configName);
        }
    }

    public void init() throws IOException {
        if (file == null) {
            throw new IOException("File object is null. Cannot initialize.");
        }

        updateConfig();

        config = YamlConfiguration.loadConfiguration(file);
    }

    @SneakyThrows
    public void updateConfig() {
        saveDefaultConfig();

        File configFile = new File(file.getParent(), configName);

        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(getClass().getResourceAsStream("/" + configName)));

        YamlConfiguration existingConfig = YamlConfiguration.loadConfiguration(configFile);

        for (String key : defaultConfig.getKeys(true)) {
            if (!existingConfig.contains(key)) {
                existingConfig.set(key, defaultConfig.get(key));
            }
        }

        // Save the updated configuration back to file
        existingConfig.save(configFile);
    }

    private void saveDefaultConfig() {}

    public void loadDefaults() throws IOException {
        InputStream is = getClass().getResourceAsStream("/" + configName);
        if (is != null) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(readFile(is));
            writer.close();
        }
    }

    public String readFile(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String content = "";
        String line;
        while ((line = reader.readLine()) != null) {
            content += line + "\n";
        }

        reader.close();
        return content.trim();
    }

    public void replacePlaceholdersInConfig(String... placeholdersAndValues) {
        if (placeholdersAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid number of arguments. Provide placeholders and values in pairs.");
        }

        for (String key : config.getKeys(true)) {
            if (config.isString(key)) {
                String value = config.getString(key);
                for (int i = 0; i < placeholdersAndValues.length; i += 2) {
                    value = value.replace(placeholdersAndValues[i], placeholdersAndValues[i + 1]);
                }
                config.set(key, value);
            }
        }

        save();
    }

    public void save() {
        try {
            getConfig().save(file);
            System.out.println("Config file saved!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getString(String path) {
        return this.config.getString(path);
    }

    public int getInt(String path) {
        return this.config.getInt(path);
    }

    public double getDouble(String path) {
        return this.config.getDouble(path);
    }

    public boolean getBoolean(String path) {
        return this.config.getBoolean(path);
    }

    public Collection<String> getSection(String section) {
        return this.config.getConfigurationSection(section).getKeys(false);
    }

    public List<String> getStringList(String path) {
        return this.config.getStringList(path);
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public void reloadConfig() {
        try {
            if (!file.exists()) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Config file " + configName + " does not exist. Creating...");
                file.createNewFile();
                loadDefaults();
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Reloading config file " + configName);
            }

            if (config == null) {
                config = new YamlConfiguration();
            }

            config.load(file);
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Successfully reloaded the config file: " + configName);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error reloading the file " + configName);
        }
    }

    public ConfigFileBackUp get() {
        return this;
    }

    public void set(String path, Object value) {
        this.config.set(path, value);
        save();
    }

    public void reloadAllConfigs() {
        config = null; // Unload the config
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Unloading config file " + configName);

        reloadConfig(); // Reload the config
    }
}
