package dev.jackdaw1101.neon.Manager;

import dev.jackdaw1101.neon.Command.Alerts.AlertManager;
import dev.jackdaw1101.neon.Manager.Logger.ChatLogger;
import dev.jackdaw1101.neon.Manager.MentionManager.ListenerMentions;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.AntiSwear.AntiSwearSystem;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatFormat implements Listener {
    private final Neon plugin;
    private final AntiSwearSystem antiSwearSystem;

    public ChatFormat(Neon plugin) {
        this.plugin = plugin;
        this.antiSwearSystem = new AntiSwearSystem(plugin, new AlertManager(plugin));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        boolean isChatFormatEnabled = (Boolean) this.plugin.getSettings().getValue("CHAT-FORMAT-ENABLED", true);
        boolean isHoverEnabled = (Boolean) this.plugin.getSettings().getValue("HOVER-ENABLED", true);
        boolean isClickEventEnabled = (Boolean) this.plugin.getSettings().getValue("CLICK-EVENT-ENABLED", true);
        boolean isAntiSwearEnabled = (Boolean) this.plugin.getSettings().getValue("ANTI-SWEAR.ENABLED", true);
        boolean isChatInConsoleEnabled = (Boolean) this.plugin.getSettings().getValue("CHAT-IN-CONSOLE", true);
        boolean logchat = (boolean) plugin.getSettings().getValue("LOG-CHAT", true);
        boolean isRunCommandEnabled = (Boolean) this.plugin.getSettings().getValue("RUN-COMMAND-ENABLED", true);

        Player sender = event.getPlayer();
        String message = event.getMessage();

        if (isChatFormatEnabled) {
            event.setCancelled(true);

            // Check and censor swear words if enabled
            if (isAntiSwearEnabled && this.antiSwearSystem.checkForSwear(sender, message)) {
                event.setCancelled(true);

                String censoredMessage = message;
                List<String> blacklist = (List<String>) this.plugin.getSettings().getValue("ANTI-SWEAR.BLACKLIST", Arrays.asList());

                for (String swear : blacklist) {
                    if (message.toLowerCase().contains(swear.toLowerCase())) {
                        censoredMessage = this.antiSwearSystem.censorMessage(message, swear, (String) plugin.getSettings().getValue("ANTI-SWEAR.CENSOR.SYMBOL"));
                        event.setCancelled(true);
                        return;
                    }
                }

                // If message is censored, cancel the event
                if (!censoredMessage.equals(message)) {
                    event.setCancelled(true);
                    return;
                }
                return;
            }

            if (logchat) {
                new ChatLogger(sender, message, plugin);
            }

            // Process message for color codes and placeholders
            message = this.processMessageColorCodes(sender, message);

            // Prepare chat format
            String format = ColorHandler.color(this.plugin.getSettings().getValue("CHAT-FORMAT", "&7<player>: &f%message%").toString());
            if (isLuckpermsInstalled()) {
                 format = handleLuckPermsPrefixSuffix(sender, format);
            } else {
                format = format;
            }

            String clickCommand = ColorHandler.color(this.plugin.getSettings().getValue("CLICK-COMMAND", "/message <player> ").toString());

            // Get hover lines
            List<String> hoverLines = (List) this.plugin.getSettings().getValue("HOVER");
            if (hoverLines == null || hoverLines.isEmpty()) {
                hoverLines = new ArrayList<>(Arrays.asList("&aDefault Hover Line: &7<player>"));
            }

            // Replace placeholders in format
            format = format.replace("<player>", sender.getName()).replace("%message%", "{MESSAGE}");
            clickCommand = clickCommand.replace("<player>", sender.getName());

            if (this.plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                format = PlaceholderAPI.setPlaceholders(sender, format);
                clickCommand = PlaceholderAPI.setPlaceholders(sender, clickCommand);
            }

            format = format.replace("{MESSAGE}", message);
            if (sender.hasPermission(plugin.getPermissionManager().getPermission("COLOR-CODES"))) {
                ColorHandler.color(message);
                //ColorHandler.color(message);
            } else if (!sender.hasPermission(plugin.getPermissionManager().getPermission("COLOR-CODES"))) {
                removeColorCodes(message);
            }


            // Send formatted message to all online players
            //for (Player viewer : this.plugin.getServer().getOnlinePlayers()) {
            for (Player viewer : event.getRecipients()) {
                List<String> parsedHoverLines = new ArrayList<>();

                // Process hover lines
                for (String line : hoverLines) {
                    line = line.replace("<player>", sender.getName());
                    if (this.plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        line = PlaceholderAPI.setPlaceholders(sender, line);
                        line = PlaceholderAPI.setRelationalPlaceholders(sender, viewer, line);
                        line = handleLuckPermsPrefixSuffix(sender, line);
                    }

                    parsedHoverLines.add(ColorHandler.color(line));
                }

                // Join hover lines into a single string
                String hoverText = String.join("\n", parsedHoverLines);

                // Create the chat message with hover and click events
                TextComponent chatMessage = new TextComponent(TextComponent.fromLegacyText(format));
                if (isHoverEnabled) {
                    chatMessage.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new TextComponent[]{new TextComponent(hoverText)}));
                }

                if (isClickEventEnabled && !isRunCommandEnabled) {
                    chatMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand));
                } else if (isRunCommandEnabled && !isClickEventEnabled) {
                    chatMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand));
                } else if (isRunCommandEnabled && isClickEventEnabled) {
                    chatMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand));
                }

                // Send the message to the player
                viewer.spigot().sendMessage(chatMessage);
            }

            // If CHAT-IN-CONSOLE is enabled, send the message to the console
            if (isChatInConsoleEnabled) {
                this.sendMessageToConsole(format);
            }
        }
    }

    private void sendMessageToConsole(String format) {
        String consoleMessage = format.replace("<player>", "[Console]"); // Console will be shown as [Console]
        consoleMessage = ColorHandler.color(consoleMessage); // Translate color codes

        // Send the message to the console
        this.plugin.getServer().getConsoleSender().sendMessage(consoleMessage);
    }

    private boolean isSwearMessage(Player sender, String message) {
        return this.antiSwearSystem.checkForSwear(sender, message);
    }


    private String processMessageColorCodes(Player sender, String message) {
        // If the player has permission to use color codes, process the message for color codes
        return sender.hasPermission(this.plugin.getPermissionManager().getPermission("COLOR-CODES")) ? ChatColor.translateAlternateColorCodes('&', message) : this.removeColorCodes(message);
    }

    private String removeColorCodes(String message) {
        // Remove color codes if the player doesn't have permission
        return message.replaceAll("&[0-9a-fk-or]", "");
    }

    private String handleLuckPermsPrefixSuffix(Player player, String format) {
        LuckPerms luckPerms = this.plugin.getServer().getServicesManager().getRegistration(LuckPerms.class).getProvider();
        if (luckPerms != null) {
           User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                String suffix = user.getCachedData().getMetaData().getSuffix();

                 format = format.replace("<lp_prefix>", (prefix != null ? prefix : ""))
                        .replace("<lp_suffix>", (suffix != null ? suffix : ""));
            }
        }
        return format;
    }
    private boolean isLuckpermsInstalled() {
        Plugin LP = plugin.getServer().getPluginManager().getPlugin("LuckPerms");
        return LP != null && LP.isEnabled();
    }
}

