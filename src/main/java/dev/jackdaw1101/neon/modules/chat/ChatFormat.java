package dev.jackdaw1101.neon.modules.chat;

import dev.jackdaw1101.neon.API.modules.events.NeonPlayerChatEvent;
import dev.jackdaw1101.neon.manager.moderation.SwearManager;
import dev.jackdaw1101.neon.utils.logs.ChatLogger;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.modules.chat.IChat;
import dev.jackdaw1101.neon.modules.moderation.AntiSwearSystem;
import dev.jackdaw1101.neon.manager.commands.AlertManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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


            message = this.IChat.processMessageColorCodes(sender, message);


            String format = IChat.getChatFormat(sender);

            String clickCommand = this.plugin.getSettings().getString("CLICK-COMMAND");


            String hoverText = this.IChat.processHoverLines(sender, message);


            format = format.replace("{MESSAGE}", message);
            clickCommand = clickCommand.replace("<player>", sender.getName());


            NeonPlayerChatEvent neonPlayerChatEvent = new NeonPlayerChatEvent(sender, message, hoverText, clickCommand);

            String finalFormat = format;
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getServer().getPluginManager().callEvent(neonPlayerChatEvent);

                if (neonPlayerChatEvent.isCancelled()) return;

                String finalMessage = neonPlayerChatEvent.getMessage();
                String finalHoverText = neonPlayerChatEvent.getHoverText();

                for (Player viewer : event.getRecipients()) {
                    this.IChat.sendFormattedMessage(sender, viewer, finalFormat, finalHoverText,
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
