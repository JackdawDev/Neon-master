package dev.jackdaw1101.neon.modules.chat;

import dev.jackdaw1101.neon.API.modules.events.NeonPlayerChatEvent;
import dev.jackdaw1101.neon.manager.moderation.SwearManager;
import dev.jackdaw1101.neon.utils.logs.ChatLogger;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.modules.chat.IChat;
import dev.jackdaw1101.neon.modules.moderation.AntiSwearSystem;
import dev.jackdaw1101.neon.commands.modules.AlertManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatFormat implements Listener {
    private final Neon plugin;
    private final IChat IChat;
    private final AntiSwearSystem antiSwearSystem;

    public ChatFormat(Neon plugin) {
        this.plugin = plugin;
        this.IChat = new IChat(plugin);
        this.antiSwearSystem = new AntiSwearSystem(plugin, new AlertManager(plugin), new SwearManager(plugin));
    }

    @EventHandler()
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        boolean isChatFormatEnabled = (Boolean) this.plugin.getSettings().getBoolean("CHAT-FORMAT.ENABLED");
        boolean isHoverEnabled = (Boolean) this.plugin.getSettings().getBoolean("CHAT-FORMAT.HOVER-ENABLED");
        boolean isClickEventEnabled = (Boolean) this.plugin.getSettings().getBoolean("CHAT-FORMAT.CLICK-EVENT-ENABLED");
        boolean isAntiSwearEnabled = (Boolean) this.plugin.getSettings().getBoolean("CHAT-FORMAT.ANTI-SWEAR.ENABLED");
        boolean isChatInConsoleEnabled = (Boolean) this.plugin.getSettings().getBoolean("CHAT-FORMAT.CHAT-IN-CONSOLE");
        boolean logchat = (boolean) plugin.getSettings().getBoolean("CHAT-FORMAT.LOG-CHAT");
        boolean isRunCommandEnabled = (Boolean) this.plugin.getSettings().getBoolean("CHAT-FORMAT.RUN-COMMAND-ENABLED");
        boolean isSuggestCommand = (Boolean) this.plugin.getSettings().getBoolean("CHAT-FORMAT.SUGGEST-COMMAND-ENABLED");

        Player sender = event.getPlayer();
        String eventmessage = event.getMessage();

        if (isChatFormatEnabled) {
            event.setCancelled(true);


            if (isAntiSwearEnabled && this.antiSwearSystem.checkForSwear(sender, eventmessage)) {
                event.setCancelled(true);
                return;
            }

            if (logchat) {
                new ChatLogger(sender, eventmessage, plugin);
            }


            eventmessage = this.IChat.processMessageColorCodes(sender, eventmessage);


            String format = IChat.getChatFormat(sender);

            String clickCommand = this.plugin.getSettings().getString("CHAT-FORMAT.CLICK-COMMAND");


            String hoverText = this.IChat.processHoverLines(sender, eventmessage);


            format = format.replace("{MESSAGE}", eventmessage);
            clickCommand = clickCommand.replace("<player>", sender.getName());


            NeonPlayerChatEvent neonPlayerChatEvent = new NeonPlayerChatEvent(sender, eventmessage, hoverText, clickCommand);

            String finalFormat = format;
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getServer().getPluginManager().callEvent(neonPlayerChatEvent);

                if (neonPlayerChatEvent.isCancelled()) return;

                String finalMessage = neonPlayerChatEvent.getMessage();
                String finalHoverText = neonPlayerChatEvent.getHoverText();

                for (Player viewer : event.getRecipients()) {
                    this.IChat.sendFormattedMessage(finalMessage, sender, viewer, finalFormat, finalHoverText,
                        neonPlayerChatEvent.getClickCommand(), isHoverEnabled, isClickEventEnabled,
                        isRunCommandEnabled, isSuggestCommand);

                }

                if (isChatInConsoleEnabled) {
                    this.IChat.sendMessageToConsole(finalFormat);
                }
            });
        }
    }
}
