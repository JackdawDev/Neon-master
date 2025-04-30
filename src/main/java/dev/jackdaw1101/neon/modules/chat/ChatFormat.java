package dev.jackdaw1101.neon.modules.chat;

import dev.jackdaw1101.neon.api.modules.events.ChatMessageEvent;
import dev.jackdaw1101.neon.manager.moderation.SwearManager;
import dev.jackdaw1101.neon.utils.logs.ChatLogger;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.api.modules.chat.ChatAPI;
import dev.jackdaw1101.neon.modules.moderation.AntiSwearSystem;
import dev.jackdaw1101.neon.manager.commands.AlertManager;
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
        boolean isSuggestCommand = (Boolean) this.plugin.getSettings().getBoolean("SUGGEST-COMMAND-ENABLED");

        Player sender = event.getPlayer();
        String message = event.getMessage();

        if (isChatFormatEnabled) {
            event.setCancelled(true);


            if (isAntiSwearEnabled && this.antiSwearSystem.checkForSwear(sender, message)) {
                event.setCancelled(true);
                return;
            }

            if (logchat) {
                new ChatLogger(sender, message, plugin);
            }


            message = this.chatAPI.processMessageColorCodes(sender, message);


            String format = chatAPI.getChatFormat(sender);

            String clickCommand = this.plugin.getSettings().getString("CLICK-COMMAND");


            String hoverText = this.chatAPI.processHoverLines(sender, message);


            format = format.replace("{MESSAGE}", message);
            clickCommand = clickCommand.replace("<player>", sender.getName());


            ChatMessageEvent chatMessageEvent = new ChatMessageEvent(sender, message, hoverText, clickCommand);


            plugin.getServer().getPluginManager().callEvent(chatMessageEvent);


            if (chatMessageEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }


            String finalMessage = chatMessageEvent.getMessage();
            String finalHoverText = chatMessageEvent.getHoverText();


            for (Player viewer : event.getRecipients()) {
                this.chatAPI.sendFormattedMessage(viewer, format, finalHoverText, chatMessageEvent.getClickCommand(), isHoverEnabled, isClickEventEnabled, isRunCommandEnabled, isSuggestCommand);
            }


            if (isChatInConsoleEnabled) {
                this.chatAPI.sendMessageToConsole(format);
            }
        }
    }
}
