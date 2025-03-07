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
        if (!(boolean) plugin.getSettings().getValue("ENABLE-WELCOME-SYSTEM", true)) {
            return;
        }

            if ((boolean) plugin.getSettings().getValue("ON-JOIN-CHAT-CLEAR", false)) {
                clearChat(player, (int) plugin.getSettings().getValue("ON-JOIN-CHAT-CLEAR-LINE", 10));
            }
            sendMessage(player);
    }

    private void clearChat(Player player, int lines) {
        for (int i = 0; i < lines; i++) {
            player.sendMessage("");
        }
    }

    private void sendMessage(Player player) {
        List<String> messages = (List<String>) plugin.getSettings().getValue("WELCOME-MESSAGE");
        if (messages == null) return;

        boolean hoverEnabled = (boolean) plugin.getSettings().getValue("HOVER-TEXT.ENABLED", false);
        List<String> hoverMessages = (List<String>) plugin.getSettings().getValue("HOVER-TEXT.CONTENT");
        TextComponent hoverComponent = null;

        if (hoverEnabled && hoverMessages != null && !hoverMessages.isEmpty()) {
            // Replace player name placeholder and apply color codes
            String hoverText = ColorHandler.color(String.join("\n", hoverMessages));
            // Parse color codes and placeholders
            hoverText = PlaceholderAPI.setPlaceholders(player, hoverText);
            hoverComponent = new TextComponent(hoverText);
        }

        // Click Events
        boolean openUrlEnabled = (boolean) plugin.getSettings().getValue("OPEN-URL.ENABLED", false);
        String openUrl = (String) plugin.getSettings().getValue("OPEN-URL.URL");

        boolean clickCommandEnabled = (boolean) plugin.getSettings().getValue("RUN-COMMAND.ENABLED", false);
        String clickCommand = (String) plugin.getSettings().getValue("RUN-COMMAND.COMMAND", "/help");

        boolean suggestCommandEnabled = (boolean) plugin.getSettings().getValue("SUGGEST-COMMAND.ENABLED", false);
        String suggestCommand = (String) plugin.getSettings().getValue("SUGGEST-COMMAND.COMMAND", "/help");

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
            if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                if ((boolean) plugin.getSettings().getValue("PLAY-SOUND.ENABLED", true)) {
                    SoundUtil.playSound(player, (String) plugin.getSettings().getValue("PLAY-SOUND.SOUND"), 1.0f, 1.0f);
                }
            } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                XSounds.playSound(player, (String) plugin.getSettings().getValue("PLAY-SOUND.SOUND"), 1.0f, 1.0f);
        }
        }
    }
}