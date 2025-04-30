package dev.jackdaw1101.neon.modules.chat;

import dev.jackdaw1101.neon.api.modules.chat.ChatAPI;
import dev.jackdaw1101.neon.api.modules.events.ChatMessageEvent;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.api.utilities.ColorHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class PerWorldChatSystem implements Listener {

    private final Neon plugin;
    private final ChatAPI api;

    public PerWorldChatSystem(Neon plugin) {
        this.plugin = plugin;
        this.api = new ChatAPI(plugin);
    }

    @EventHandler
    public void onChatMessage(ChatMessageEvent event) {
        Player sender = event.getSender();
        String worldName = sender.getWorld().getName();

        if (!plugin.getSettings().getBoolean("PER-WORLD-CHAT-ENABLED")) {
            return;
        }

        ConfigurationSection worldChats = plugin.getConfig().getConfigurationSection("PER-WORLD-CHATS");
        if (worldChats == null) return;

        ConfigurationSection worldSection = worldChats.getConfigurationSection(worldName);
        if (worldSection == null) return;

        String format = worldSection.getString("FORMAT");
        List<String> hoverLines = worldSection.getStringList("HOVER");

        if (worldSection.getBoolean("HOVER-ENABLED") && hoverLines.isEmpty()) {
            hoverLines.add("&7No hover text.");
        }

        format = ColorHandler.color(format)
            .replace("<player>", sender.getName())
            .replace("<message>", event.getMessage());

        String hoverText = ColorHandler.color(String.join("\n", hoverLines))
            .replace("<player>", sender.getName());

        event.setCancelled(true);

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getWorld().equals(sender.getWorld())) {
                api.sendFormattedMessage(
                    viewer,
                    format,
                    hoverText,
                    worldSection.getString("CLICK-COMMAND"),
                    worldSection.getBoolean("HOVER-ENABLED"),
                    worldSection.getBoolean("CLICK-EVENT"),
                    worldSection.getBoolean("RUN-COMMAND"),
                    worldSection.getBoolean("SUGGEST-COMMAND")
                );
            }
        }

        api.sendMessageToConsole(format);
    }
}
