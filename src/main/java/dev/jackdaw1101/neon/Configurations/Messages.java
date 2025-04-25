package dev.jackdaw1101.neon.Configurations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.jackdaw1101.neon.Neon;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Messages {
    private final Neon plugin;
    private final Map<String, String> messages = new HashMap<>();
    private final List<String> welcomeMessages = new ArrayList<>();

    public Messages(Neon plugin) {
        this.plugin = plugin;
        this.loadMessages();
    }

    private void loadMessages() {
        File serverDir = Bukkit.getServer().getWorldContainer();
        File pluginsDir = new File(serverDir, "plugins");
        String pluginName = "NeonLoader";
        File pluginDir = new File(pluginsDir, pluginName);

        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
        }

        File messagesFile = new File(pluginDir, "messages.yml");

        if (!messagesFile.exists()) {
            this.plugin.saveResource("messages.yml", false);
        }

        FileConfiguration messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        for (String key : messagesConfig.getKeys(true)) {
            if (messagesConfig.isString(key)) {
                this.messages.put(key, messagesConfig.getString(key));
            }
        }
    }

    public String getMessage(String key, Object... placeholders) {
        String message = this.messages.getOrDefault(key, key);
        return this.applyPlaceholders(message, placeholders);
    }

    private String applyPlaceholders(String message, Object... placeholders) {
        for (int i = 0; i < placeholders.length; i += 2) {
            String placeholder = (String) placeholders[i];
            String value = placeholders[i + 1].toString();
            message = message.replace(placeholder, value);
        }

        String prefix = this.messages.getOrDefault("PREFIX", "");
        message = message.replace("{prefix}", prefix);
        String maintheme = this.messages.getOrDefault("MAIN-THEME", "");
        message = message.replace("{main_theme}", maintheme);
        String secondtheme = this.messages.getOrDefault("SECOND-THEME", "");
        message = message.replace("{second_theme}", secondtheme);
        String thirdtheme = this.messages.getOrDefault("THIRD-THEME", "");
        message = message.replace("{third_theme}", thirdtheme);
        String autoresponseprefix = this.messages.getOrDefault("AUTO-RESPONSE-PREFIX", "");
        message = message.replace("{auto_response_prefix}", autoresponseprefix);
        return message;
    }

    public boolean reloadMessages() {
        File serverDir = Bukkit.getServer().getWorldContainer();
        File pluginsDir = new File(serverDir, "plugins");
        String pluginName = "NeonLoader";
        File pluginDir = new File(pluginsDir, pluginName);

        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
        }

        File messagesFile = new File(pluginDir, "messages.yml");


        this.plugin.reloadConfig();

        this.loadMessages();
        return true;
    }
}


