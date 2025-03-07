package dev.jackdaw1101.neon.Manager.MOTD;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class WelcomeListener implements Listener {
    private final Neon plugin;

    public WelcomeListener(Neon plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!(boolean) plugin.getSettings().getBoolean("ENABLE-WELCOME-SYSTEM")) {
            return;
        }

            if ((boolean) plugin.getSettings().getBoolean("ON-JOIN-CHAT-CLEAR")) {
                clearChat(player, (int) plugin.getSettings().getInt("ON-JOIN-CHAT-CLEAR-LINE"));
            }
            sendMessage(player);
    }

    private void clearChat(Player player, int lines) {
        for (int i = 0; i < lines; i++) {
            player.sendMessage("");
        }
    }

    private void sendMessage(Player player) {
        List<String> messages = (List<String>) plugin.getSettings().getStringList("WELCOME-MESSAGE");
        if (messages == null) return;

        boolean hoverEnabled = (boolean) plugin.getSettings().getBoolean("HOVER-TEXT.ENABLED");
        List<String> hoverMessages = (List<String>) plugin.getSettings().getStringList("HOVER-TEXT.CONTENT");
        TextComponent hoverComponent = null;

        if (hoverEnabled && hoverMessages != null && !hoverMessages.isEmpty()) {
            // Replace player name placeholder and apply color codes
            String hoverText = ColorHandler.color(String.join("\n", hoverMessages));
            // Parse color codes and placeholders
            hoverText = PlaceholderAPI.setPlaceholders(player, hoverText);
            hoverComponent = new TextComponent(hoverText);
        }

        // Click Events
        boolean openUrlEnabled = (boolean) plugin.getSettings().getBoolean("OPEN-URL.ENABLED");
        String openUrl = (String) plugin.getSettings().getString("OPEN-URL.URL");

        boolean clickCommandEnabled = (boolean) plugin.getSettings().getBoolean("RUN-COMMAND.ENABLED");
        String clickCommand = (String) plugin.getSettings().getString("RUN-COMMAND.COMMAND");

        boolean suggestCommandEnabled = (boolean) plugin.getSettings().getBoolean("SUGGEST-COMMAND.ENABLED");
        String suggestCommand = (String) plugin.getSettings().getString("SUGGEST-COMMAND.COMMAND");

        for (String line : messages) {
            line = ColorHandler.color(PlaceholderAPI.setPlaceholders(player, line));
            TextComponent messageComponent = new TextComponent(line);

            // Apply Hover Event only if enabled
            if (hoverEnabled && hoverComponent != null) {
                // Convert hoverComponent to BaseComponent[] and set hover event
                messageComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hoverComponent}));
            }

            // Apply Click Event
            if (openUrlEnabled && !openUrl.isEmpty()) {
                messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, openUrl));
            } else if (clickCommandEnabled && !clickCommand.isEmpty()) {
                messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand));
            } else if (suggestCommandEnabled && !suggestCommand.isEmpty()) {
                messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCommand));
            }

            player.spigot().sendMessage((BaseComponent) messageComponent);
            if ((boolean) plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                if ((boolean) plugin.getSettings().getBoolean("PLAY-SOUND.ENABLED")) {
                    SoundUtil.playSound(player, (String) plugin.getSettings().getString("PLAY-SOUND.SOUND"), 1.0f, 1.0f);
                }
            } else if ((boolean) plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                XSounds.playSound(player, (String) plugin.getSettings().getString("PLAY-SOUND.SOUND"), 1.0f, 1.0f);
        }
        }
    }
}