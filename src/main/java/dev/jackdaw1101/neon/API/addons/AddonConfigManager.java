package dev.jackdaw1101.neon.API.addons;

import dev.jackdaw1101.neon.API.utilities.CC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class AddonConfigManager {
    private final JavaPlugin addon;
    private final String configName;
    private File configFile;
    private YamlConfiguration config;
    private final Map<String, List<String>> commentsMap = new LinkedHashMap<>();
    private List<String> header = new ArrayList<>();
    private final boolean useSubfolder;
    private final String customSubfolder;


    public AddonConfigManager(JavaPlugin addon, String configName, boolean useSubfolder, String customSubfolder) {
        this.addon = addon;
        this.configName = configName.endsWith(".yml") ? configName : configName + ".yml";
        this.useSubfolder = useSubfolder;
        this.customSubfolder = useSubfolder ?
            (customSubfolder != null ? customSubfolder : "configs") :
            null;
        initialize();
    }

    private void initialize() {
        File configFolder;
        if (customSubfolder != null) {
            configFolder = new File(addon.getDataFolder().getParentFile(),
                "Neon/Addons/" + addon.getName() + "/" + customSubfolder);
        } else {
            configFolder = new File(addon.getDataFolder().getParentFile(),
                "Neon/Addons/" + addon.getName());
        }

        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        this.configFile = new File(configFolder, configName);
        load();
    }

    public void setHeader(String... headerLines) {
        this.header.clear();
        for (String line : headerLines) {
            this.header.add("# " + line);
        }
        saveWithComments();
    }

    public void load() {
        try {
            if (!configFile.exists()) {
                if (addon.getResource(configName) != null) {
                    addon.saveResource(configName, false);
                    if (useSubfolder) {
                        File tempFile = new File(addon.getDataFolder(), configName);
                        if (tempFile.exists()) {
                            Files.move(tempFile.toPath(), configFile.toPath());
                        }
                    }
                } else {
                    configFile.createNewFile();
                    if (!header.isEmpty()) {
                        saveWithComments();
                    }
                }
            }

            this.config = YamlConfiguration.loadConfiguration(configFile);

            mergeDefaults();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Failed to load config for " + addon.getName() + ": " + configName);
            e.printStackTrace();
        }
    }

    private void saveWithComments() {
        try {
            List<String> lines = new ArrayList<>();


            if (!header.isEmpty() && (!configFile.exists() || configFile.length() == 0)) {
                lines.addAll(header);
                lines.add("");
            }

            BufferedReader reader = null;
            if (configFile.exists()) {
                reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));

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
            }

            if (reader != null) {
                reader.close();
            }

            File tempFile = new File(configFile.getParentFile(), configFile.getName() + ".tmp");
            BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8));

            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }
            writer.flush();
            writer.close();

            Files.move(tempFile.toPath(), configFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            config = YamlConfiguration.loadConfiguration(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean reload() {
        try {
            config = YamlConfiguration.loadConfiguration(configFile);


            InputStream defConfigStream = addon.getResource(configName);
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
                config.setDefaults(defConfig);
            }


            loadComments();

            return true;
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Failed to reload config: " + configName);
            e.printStackTrace();
            return false;
        }
    }

    private void loadComments() {
        commentsMap.clear();
        if (!configFile.exists()) return;

        try {
            List<String> lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
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
            InputStream resource = addon.getResource(configName);
            if (resource == null) return;

            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(resource, StandardCharsets.UTF_8));

            for (String key : defaultConfig.getKeys(true)) {
                if (!config.contains(key)) {
                    config.set(key, defaultConfig.get(key));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateConfig() {
        try {

            List<String> ignoreKeys = new ArrayList<>();


            if (configFile.exists()) {
                YamlConfiguration currentConfig = YamlConfiguration.loadConfiguration(configFile);
                ignoreKeys.addAll(currentConfig.getKeys(true));
            }

            reload();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try {
            config.save(configFile);
            saveWithComments();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getString(String path, Object... placeholders) {
        String message = config.getString(path);
        if (message == null) return null;

        if (placeholders.length > 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                if (i + 1 >= placeholders.length) break;
                message = message.replace(
                    String.valueOf(placeholders[i]),
                    String.valueOf(placeholders[i + 1])
                );
            }
        }
        return message;
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public void set(String path, Object value) {
        config.set(path, value);
        save();
    }

    public boolean contains(String path) {
        return config.contains(path);
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public File getConfigFile() {
        return configFile;
    }

    public void addDefault(String path, Object value, String... comments) {
        if (!config.contains(path)) {
            config.set(path, value);
            if (comments != null && comments.length > 0) {
                List<String> commentList = new ArrayList<>();
                for (String comment : comments) {
                    commentList.add("# " + comment);
                }
                commentsMap.put(path.split("\\.")[0], commentList);
            }
            save();
        }
    }
}
