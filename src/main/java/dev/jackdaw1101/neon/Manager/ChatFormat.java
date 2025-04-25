package dev.jackdaw1101.neon.Manager;

import dev.jackdaw1101.neon.API.Chat.Events.ChatMessageEvent;
import dev.jackdaw1101.neon.AntiSwear.SwearManager;
import dev.jackdaw1101.neon.Manager.Logger.ChatLogger;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.Chat.ChatAPI;
import dev.jackdaw1101.neon.AntiSwear.AntiSwearSystem;
import dev.jackdaw1101.neon.Command.Alerts.AlertManager;
import dev.jackdaw1101.neon.API.Utils.ColorHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatFormat implements Listener {
    private final Neon plugin;
    private final ChatAPI chatAPI;
    private final AntiSwearSystem antiSwearSystem;

    public ChatFormat(Neon plugin) {
        this.plugin = plugin;
        this.chatAPI = new ChatAPI(plugin);
        this.antiSwearSystem = new AntiSwearSystem(plugin, new AlertManager(plugin), new SwearManager(plugin));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        boolean isChatFormatEnabled = (Boolean) this.plugin.getSettings().getBoolean("CHAT-FORMAT-ENABLED");
        boolean isHoverEnabled = (Boolean) this.plugin.getSettings().getBoolean("HOVER-ENABLED");
        boolean isClickEventEnabled = (Boolean) this.plugin.getSettings().getBoolean("CLICK-EVENT-ENABLED");
        boolean isAntiSwearEnabled = (Boolean) this.plugin.getSettings().getBoolean("ANTI-SWEAR.ENABLED");
        boolean isChatInConsoleEnabled = (Boolean) this.plugin.getSettings().getBoolean("CHAT-IN-CONSOLE");
        boolean logchat = (boolean) plugin.getSettings().getBoolean("LOG-CHAT");
        boolean isRunCommandEnabled = (Boolean) this.plugin.getSettings().getBoolean("RUN-COMMAND-ENABLED");

        Player sender = event.getPlayer();
        String message = event.getMessage();

        if (isChatFormatEnabled) {
            event.setCancelled(true);

            // Check and censor swear words if enabled
            if (isAntiSwearEnabled && this.antiSwearSystem.checkForSwear(sender, message)) {
                event.setCancelled(true);
                return;
            }

            if (logchat) {
                new ChatLogger(sender, message, plugin);
            }

            // Process message for color codes and placeholders
            message = this.chatAPI.processMessageColorCodes(sender, message);

            // Prepare chat format
            String format = chatAPI.getChatFormat(sender);

            String clickCommand = ColorHandler.color(this.plugin.getSettings().getString("CLICK-COMMAND").toString());

            // Process hover text
            String hoverText = this.chatAPI.processHoverLines(sender, message);

            // Replace placeholders in format
            format = format.replace("{MESSAGE}", message);
            clickCommand = clickCommand.replace("<player>", sender.getName());

            // Create the custom ChatMessageEvent
            ChatMessageEvent chatMessageEvent = new ChatMessageEvent(sender, message, hoverText, clickCommand);

            // Call the event
            plugin.getServer().getPluginManager().callEvent(chatMessageEvent);

            // If the event is cancelled, do not send the message
            if (chatMessageEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            // Get the final message and hover text after potential modifications
            String finalMessage = chatMessageEvent.getMessage();
            String finalHoverText = chatMessageEvent.getHoverText();

            // Send formatted message to all online players
            for (Player viewer : event.getRecipients()) {
                this.chatAPI.sendFormattedMessage(viewer, format, finalHoverText, chatMessageEvent.getClickCommand(), isHoverEnabled, isClickEventEnabled, isRunCommandEnabled);
            }

            // If CHAT-IN-CONSOLE is enabled, send the message to the console
            if (isChatInConsoleEnabled) {
                this.chatAPI.sendMessageToConsole(format);
            }
        }
    }
}
