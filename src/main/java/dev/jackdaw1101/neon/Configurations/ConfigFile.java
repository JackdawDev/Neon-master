package dev.jackdaw1101.neon.Configurations;

import com.tchristofferson.configupdater.ConfigUpdater;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class ConfigFile {

    private final JavaPlugin plugin;
    private final String configName;
    protected File file;
    protected YamlConfiguration config;
    private final Map<String, List<String>> commentsMap = new LinkedHashMap<>();

    @SneakyThrows
    public ConfigFile(JavaPlugin plugin, String configName) {
        this.plugin = plugin;
        this.configName = configName;

        file = new File(plugin.getDataFolder(), configName);
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe(ChatColor.RED + "[Neon] Error initializing config file: " + configName);
        }
    }

    public void init() throws IOException {
        if (file == null) throw new IOException("File object is null. Cannot initialize.");

        updateConfig();
        config = YamlConfiguration.loadConfiguration(file);
    }

    @SneakyThrows
    public void updateConfig() {
        saveDefaultConfig();
        mergeDefaults();
    }

    private void saveDefaultConfig() {
        if (!file.exists()) {
            plugin.saveResource(configName, false);
        }
    }

    public void loadComments() {
        try {
            if (!file.exists()) return;

            commentsMap.clear();
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            String currentKey = "";
            List<String> commentBuffer = new ArrayList<>();

            for (String line : lines) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    commentBuffer.add(line);
                } else {
                    int colonIndex = line.indexOf(":");
                    if (colonIndex > 0) {
                        currentKey = line.substring(0, colonIndex).trim();
                    }
                    if (!commentBuffer.isEmpty()) {
                        commentsMap.put(currentKey, new ArrayList<>(commentBuffer));
                        commentBuffer.clear();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mergeDefaults() {
        try {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(plugin.getResource(configName), StandardCharsets.UTF_8));
            YamlConfiguration existingConfig = YamlConfiguration.loadConfiguration(file);

            for (String key : defaultConfig.getKeys(true)) {
                if (!existingConfig.contains(key)) {
                    existingConfig.set(key, defaultConfig.get(key));
                }
            }

            saveWithComments(existingConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveWithComments(YamlConfiguration config) {
        try {
            List<String> lines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            String currentKey = "";
            Set<String> addedKeys = new HashSet<>();

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    lines.add(line);
                } else {
                    int colonIndex = line.indexOf(":");
                    if (colonIndex > 0) {
                        currentKey = line.substring(0, colonIndex).trim();
                    }

                    if (commentsMap.containsKey(currentKey) && !addedKeys.contains(currentKey)) {
                        lines.addAll(commentsMap.get(currentKey));
                        addedKeys.add(currentKey);
                    }

                    lines.add(line);
                }
            }
            reader.close();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            saveWithComments(config);
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

    public List<String> getStringList(String path) {
        return this.config.getStringList(path);
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public boolean configExists() {
        return file.exists();
    }

    public boolean reload() {
        try {
            config = YamlConfiguration.loadConfiguration(file);

            // If you use a default config resource inside your jar, uncomment the following lines
            InputStream defConfigStream = plugin.getResource(configName);
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
                config.setDefaults(defConfig);
            }

            // Optional: update the config with a utility method if you have one
            ConfigUpdater.update(plugin, configName, file); // If you use a config updater util

            return true;
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Neon] Failed to reload config: " + configName);
            e.printStackTrace();
            return false;
        }
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

    public ConfigFile get() {
        return this;
    }

    public void set(String path, Object value) {
        this.config.set(path, value);
        save();
    }
}
