package dev.jackdaw1101.neon.API.Features.AutoResponse;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoResponseAPIImpl implements AutoResponseAPI {
    private final Neon plugin;
    private final Map<String, List<String>> customResponses = new HashMap<>();

    public AutoResponseAPIImpl(Neon plugin) {
        this.plugin = plugin;
        loadFromConfig();
    }

    @Override
    public void addResponse(String triggerWord, List<String> responses) {
        customResponses.put(triggerWord.toLowerCase(), new ArrayList<>(responses));
        saveToConfig();
    }

    @Override
    public void removeResponse(String triggerWord) {
        customResponses.remove(triggerWord.toLowerCase());
        saveToConfig();
    }

    @Override
    public void updateResponse(String triggerWord, List<String> responses) {
        addResponse(triggerWord, responses);
    }

    @Override
    public void setGlobalHoverText(List<String> hoverText) {
        plugin.getSettings().set("AUTO-RESPONSE-HOVER", hoverText);
    }

    @Override
    public void setGlobalSound(String sound) {
        plugin.getSettings().set("AUTO-RESPONSE-SOUND", sound);
    }

    @Override
    public void setGlobalSoundEnabled(boolean enabled) {
        plugin.getSettings().set("AUTO-RESPONSE-USE-SOUND", enabled);
    }

    @Override
    public void setGlobalHoverEnabled(boolean enabled) {
        plugin.getSettings().set("AUTO-RESPONSE-HOVER-ENABLED", enabled);
    }

    @Override
    public Map<String, List<String>> getAllResponses() {
        return new HashMap<>(customResponses);
    }

    @Override
    public List<String> getResponsesForWord(String triggerWord) {
        return new ArrayList<>(customResponses.getOrDefault(triggerWord.toLowerCase(), new ArrayList<>()));
    }

    @Override
    public List<String> getGlobalHoverText() {
        return plugin.getSettings().getStringList("AUTO-RESPONSE-HOVER");
    }

    @Override
    public String getGlobalSound() {
        return plugin.getSettings().getString("AUTO-RESPONSE-SOUND");
    }

    @Override
    public boolean isSoundEnabled() {
        return plugin.getSettings().getBoolean("AUTO-RESPONSE-USE-SOUND");
    }

    @Override
    public boolean isHoverEnabled() {
        return plugin.getSettings().getBoolean("AUTO-RESPONSE-HOVER-ENABLED");
    }

    @Override
    public void reloadResponses() {
        customResponses.clear();
        loadFromConfig();
    }

    @Override
    public void saveResponses() {
        saveToConfig();
    }

    private void loadFromConfig() {
        ConfigurationSection section = plugin.getLocales().getConfig().getConfigurationSection("AUTO-RESPONSES");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                customResponses.put(key.toLowerCase(), section.getStringList(key));
            }
        }
    }

    private void saveToConfig() {
        FileConfiguration config = plugin.getLocales().getConfig();
        config.set("AUTO-RESPONSES", null);

        for (Map.Entry<String, List<String>> entry : customResponses.entrySet()) {
            config.set("AUTO-RESPONSES." + entry.getKey(), entry.getValue());
        }
    }
}
