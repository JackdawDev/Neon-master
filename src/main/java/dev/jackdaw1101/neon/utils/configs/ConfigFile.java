package dev.jackdaw1101.neon.utils.configs;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.jackdaw1101.neon.API.NeonAPI;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.utils.DebugUtil;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class ConfigFile {

    private final Neon plugin;
    private final String configName;
    protected File file;
    private File localeConfig;
    private FileConfiguration localeFile;
    protected YamlConfiguration config;
    private final Map<String, List<String>> commentsMap = new LinkedHashMap<>();
    private boolean isLocaleConfig = false;

    @SneakyThrows
    public ConfigFile(Neon plugin, String configName) {
        this.plugin = plugin;
        this.configName = configName;
        this.isLocaleConfig = configName.equals("locale.yml");

        file = new File(plugin.getDataFolder(), configName);
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
            DebugUtil.debug(ChatColor.RED + "[Neon] Error initializing config file: " + configName);
        }
    }

    public boolean reloadLocales() {
        try {
            localeConfig = new File(plugin.getDataFolder(), "locale.yml");
            if (!localeConfig.exists()) {
                plugin.saveResource("locale.yml", false);
            }
            localeFile = YamlConfiguration.loadConfiguration(localeConfig);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getString(String path, Object... placeholders) {
        String message = this.config.getString(path);
        if (message == null) return null;

        if (placeholders.length > 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                if (i + 1 >= placeholders.length) break;
                String placeholder = String.valueOf(placeholders[i]);
                String value = String.valueOf(placeholders[i + 1]);
                message = message.replace(placeholder, value);
            }
        }

        String prefix = this.config.getString("PREFIX");
        if (prefix != null) message = message.replace("{prefix}", prefix);

        String mainTheme = this.config.getString("MAIN-THEME");
        if (mainTheme != null) message = message.replace("{main_theme}", mainTheme);

        String secondTheme = this.config.getString("SECOND-THEME");
        if (secondTheme != null) message = message.replace("{second_theme}", secondTheme);

        String thirdTheme = this.config.getString("THIRD-THEME");
        if (thirdTheme != null) message = message.replace("{third_theme}", thirdTheme);

        String autoResponsePrefix = this.config.getString("AUTO-RESPONSE-PREFIX");
        if (autoResponsePrefix != null) message = message.replace("{auto_response_prefix}", autoResponsePrefix);

        return message;
    }

    public void init() throws IOException {
        if (file == null) throw new IOException("File object is null. Cannot initialize.");

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            plugin.saveResource(configName, false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        loadComments();

        if (!isLocaleConfig) {
            mergeWithDefaults();
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

    private void mergeWithDefaults() {
        try {
            InputStream defConfigStream = plugin.getResource(configName);
            if (defConfigStream == null) return;

            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));

            boolean modified = false;

            for (String key : defaultConfig.getKeys(true)) {
                if (!config.contains(key)) {
                    if (!defaultConfig.isConfigurationSection(key)) {
                        config.set(key, defaultConfig.get(key));
                        modified = true;
                    }
                }
            }

            if (modified) {
                save();
            }
        } catch (Exception e) {
            DebugUtil.debug("Failed to merge defaults for " + configName + ": " + e.getMessage());
        }
    }

    public void save() {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            config.save(file);

            saveWithComments();
        } catch (IOException e) {
            DebugUtil.debug("Failed to save config: " + configName);
            e.printStackTrace();
        }
    }

    private void saveWithComments() {
        try {
            List<String> lines;
            if (file.exists()) {
                lines = new ArrayList<>(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
            } else {
                lines = new ArrayList<>();
            }

            if (!commentsMap.isEmpty()) {
                List<String> newLines = new ArrayList<>();
                String currentKey = "";
                Set<String> addedKeys = new HashSet<>();

                for (String line : lines) {
                    if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                        int colonIndex = line.indexOf(":");
                        if (colonIndex > 0) {
                            currentKey = line.substring(0, colonIndex).trim();
                        }

                        if (commentsMap.containsKey(currentKey) && !addedKeys.contains(currentKey)) {
                            newLines.addAll(commentsMap.get(currentKey));
                            addedKeys.add(currentKey);
                        }
                    }
                    newLines.add(line);
                }

                Files.write(file.toPath(), newLines, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            DebugUtil.debug("Failed to save comments for " + configName + ": " + e.getMessage());
        }
    }

    public boolean reload() {
        try {
            if (!file.exists()) {
                plugin.saveResource(configName, false);
            }

            config = YamlConfiguration.loadConfiguration(file);
            ConfigUpdater.update(plugin, configName, file);

            if (!isLocaleConfig) {
                mergeWithDefaults();
            }

            return true;
        } catch (Exception e) {
            DebugUtil.debug(ChatColor.RED + "[Neon] Failed to reload config: " + configName);
            e.printStackTrace();
            return false;
        }
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

    public void set(String path, Object value) {
        this.config.set(path, value);
        save();
    }

    public void replacePlaceholdersInConfig(String... placeholdersAndValues) {
        if (placeholdersAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Provide placeholders and values in pairs.");
        }

        boolean modified = false;
        for (String key : config.getKeys(true)) {
            if (config.isString(key)) {
                String value = config.getString(key);
                if (value == null) continue;

                for (int i = 0; i < placeholdersAndValues.length; i += 2) {
                    String placeholder = placeholdersAndValues[i];
                    String replacement = placeholdersAndValues[i + 1];
                    String newValue = value.replace(placeholder, replacement != null ? replacement : "");

                    if (!newValue.equals(value)) {
                        config.set(key, newValue);
                        modified = true;
                        value = newValue;
                    }
                }
            }
        }

        if (modified) {
            save();
        }
    }

    public FileConfiguration getLocaleFile() {
        if (localeFile == null) {
            reloadLocales();
        }
        return localeFile;
    }

    public ConfigFile get() {
        return this;
    }
}