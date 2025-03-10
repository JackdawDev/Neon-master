package dev.jackdaw1101.neon.Configurations;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class ConfigFile {

    private final String configName;
    protected File file;
    protected YamlConfiguration config;
    private final Map<String, List<String>> commentsMap = new LinkedHashMap<>();

    @SneakyThrows
    public ConfigFile(String configName) {
        this.configName = configName;

        File serverDir = Bukkit.getServer().getWorldContainer();
        File pluginsDir = new File(serverDir, "plugins");
        String pluginName = "AstroLoader";
        String Plugin = "Neon";

        File pluginDir = new File(pluginsDir, pluginName);
        File subPluginDir = new File(pluginDir, Plugin);

        if (!pluginDir.exists()) pluginDir.mkdirs();
        if (!subPluginDir.exists()) subPluginDir.mkdirs();

        file = new File(subPluginDir, configName);
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Neon] Error initializing config file: " + configName);
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
        //loadComments();
        mergeDefaults();
    }

    private void saveDefaultConfig() {
        if (!file.exists()) {
            try (InputStream defaultStream = getClass().getResourceAsStream("/" + configName)) {
                if (defaultStream != null) {
                    Files.copy(defaultStream, file.toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            File configFile = new File(file.getParent(), configName);
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(getClass().getResourceAsStream("/" + configName)));
            YamlConfiguration existingConfig = YamlConfiguration.loadConfiguration(configFile);

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
            Set<String> addedKeys = new HashSet<>(); // To track already added comments

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    lines.add(line);
                } else {
                    int colonIndex = line.indexOf(":");
                    if (colonIndex > 0) {
                        currentKey = line.substring(0, colonIndex).trim();
                    }

                    // Add comments only if they haven't been added before
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
            //Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Config file saved: " + configName);
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

    public void reloadConfig() {
        try {
            if (!configExists()) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Neon] Config file " + configName + " does not exist. Creating...");
                file.createNewFile();
                saveDefaultConfig();
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Neon] Reloading config file " + configName);
            }

            if (config == null) {
                config = new YamlConfiguration();
            }

            config.load(file);
            loadComments();
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Neon] Successfully reloaded the config file: " + configName);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Neon] Error reloading config file: " + configName);
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
