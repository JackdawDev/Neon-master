package dev.jackdaw1101.neon.Manager.MOTD;

import dev.jackdaw1101.neon.API.Features.Player.WelcomeEvent;
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
import org.bukkit.plugin.PluginManager;

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

        // Create the list of messages
        List<String> messages = (List<String>) plugin.getSettings().getStringList("WELCOME-MESSAGE");
        if (messages == null) return;

        // Trigger the custom WelcomeEvent before sending the message
        WelcomeEvent welcomeEvent = new WelcomeEvent(player, messages);

        // Optionally modify the event settings
        // (This is where other plugins or listeners can modify the event's data)
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.callEvent(welcomeEvent);

        // If the event is canceled, do not send any messages
        if (welcomeEvent.isCancelled()) {
            return;
        }

        // Get the final messages after potential modifications
        List<String> finalMessages = welcomeEvent.getMessageLines();
        String soundToPlay = welcomeEvent.getSound();

        sendMessage(player, finalMessages, soundToPlay, welcomeEvent);
    }

    private void clearChat(Player player, int lines) {
        for (int i = 0; i < lines; i++) {
            player.sendMessage("");
        }
    }

    private void sendMessage(Player player, List<String> messages, String sound, WelcomeEvent event) {
        boolean hoverEnabled = (boolean) plugin.getSettings().getBoolean("HOVER-TEXT.ENABLED");
        TextComponent hoverComponent = null;

        if (hoverEnabled && event.getHoverMessages() != null && !event.getHoverMessages().isEmpty()) {
            String hoverText = ColorHandler.color(String.join("\n", event.getHoverMessages()));
            hoverText = PlaceholderAPI.setPlaceholders(player, hoverText);
            hoverComponent = new TextComponent(hoverText);
        }

        boolean openUrlEnabled = (boolean) plugin.getSettings().getBoolean("OPEN-URL.ENABLED");
        String openUrl = (String) plugin.getSettings().getString("OPEN-URL.URL");

        boolean clickCommandEnabled = (boolean) plugin.getSettings().getBoolean("RUN-COMMAND.ENABLED");
        String clickCommand = (String) plugin.getSettings().getString("RUN-COMMAND.COMMAND");

        boolean suggestCommandEnabled = (boolean) plugin.getSettings().getBoolean("SUGGEST-COMMAND.ENABLED");
        String suggestCommand = (String) plugin.getSettings().getString("SUGGEST-COMMAND.COMMAND");

        for (String line : messages) {
            line = ColorHandler.color(PlaceholderAPI.setPlaceholders(player, line));
            TextComponent messageComponent = new TextComponent(line);

            if (hoverEnabled && hoverComponent != null) {
                messageComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hoverComponent}));
            }

            if (event.isOpenUrlEnabled() && !openUrl.isEmpty()) {
                messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, openUrl));
            } else if (event.isClickCommandEnabled() && !clickCommand.isEmpty()) {
                messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand));
            } else if (event.isSuggestCommandEnabled() && !suggestCommand.isEmpty()) {
                messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCommand));
            }

            player.spigot().sendMessage((BaseComponent) messageComponent);
        }

        // Play sound
        if ((boolean) plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
            if ((boolean) plugin.getSettings().getBoolean("PLAY-SOUND.ENABLED") && !sound.isEmpty()) {
                SoundUtil.playSound(player, sound, 1.0f, 1.0f);
            }
        } else if ((boolean) plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
            if ((boolean) plugin.getSettings().getBoolean("PLAY-SOUND.ENABLED") && !sound.isEmpty()) {
                XSounds.playSound(player, sound, 1.0f, 1.0f);
            }
        }
    }
}
