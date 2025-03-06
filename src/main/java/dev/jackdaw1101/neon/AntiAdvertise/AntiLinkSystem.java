package dev.jackdaw1101.neon.AntiAdvertise;

import dev.jackdaw1101.neon.AntiAdvertise.Discord.WebhookManager;
import dev.jackdaw1101.neon.AntiAdvertise.Logger.AntiADLogger;
import dev.jackdaw1101.neon.AntiSwear.AntiSwearSystem;
import dev.jackdaw1101.neon.AntiSwear.Logger.AntiSwearLogger;
import dev.jackdaw1101.neon.Command.Alerts.AlertManager;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Chat.CC;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.*;
import java.util.regex.Pattern;

public class AntiLinkSystem implements Listener {
    private final Neon plugin;
    private final Pattern urlPattern;
    private List<String> whitelistedLinks;
    private final AlertManager alertManager;

    public AntiLinkSystem(Neon plugin, AlertManager alertManager) {
        this.plugin = plugin;
        this.urlPattern = Pattern.compile("^(http://www\\.|https://www\\.|http://|https://)?[a-z0-9]+([\\-.][a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(/.*)?$");
        this.whitelistedLinks = (List<String>) plugin.getSettings().getValue("ANTI-LINK.WHITELIST");
        this.alertManager = alertManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        if ((Boolean) this.plugin.getSettings().getValue("ANTI-LINK.ENABLED", true)) {
            handleLinkCheck(event.getPlayer(), event.getMessage(), event);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if ((Boolean) this.plugin.getSettings().getValue("ANTI-LINK.CHECK-COMMANDS", true)) {
            handleLinkCheck(event.getPlayer(), event.getMessage(), event);
        }
    }

    private void handleLinkCheck(Player player, String message, Cancellable cancellable) {
        if (!player.hasPermission(this.plugin.getPermissionManager().getPermission("ANTI-LINK-BYPASS"))) {
            // Clean message to remove obfuscated links
            String sanitizedMessage = sanitizeMessage(message);

            if (containsLink(sanitizedMessage)) {
                String alertMessage = ColorHandler.color(
                        this.plugin.getMessageManager().getMessage("ANTI-LINK.ALERT-MESSAGE"));
                String cancelType = (String) this.plugin.getSettings().getValue("ANTI-LINK.CANCEL-TYPE", "silent");

                cancellable.setCancelled(true);

                switch (cancelType) {
                    case "silent":
                        boolean log = (boolean) plugin.getSettings().getValue("ANTI-LINK.LOG", true);
                        String warnMessage = ColorHandler.color(this.plugin.getMessageManager().getMessage("ANTI-LINK.WARNING-MESSAGE"));
                        Bukkit.getScheduler().runTask(this.plugin, () -> {
                            notifyAdmins(player, message);
                            player.sendMessage(warnMessage);
                            WebhookManager webhook2 = new WebhookManager(plugin);
                            webhook2.sendWebhook(player, message, "silent");
                            if (log) {
                                new AntiADLogger(player, message, plugin);
                            }
                            if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                                if ((boolean) plugin.getSettings().getValue("ANTI-LINK.WARN-SOUND-ENABLED", true)) {
                                    SoundUtil.playSound(player, (String) plugin.getSettings().getValue("ANTI-LINK.WARN-SOUND"), 1.0f, 1.0f);
                                }
                            } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                                XSounds.playSound(player, (String) plugin.getSettings().getValue("ANTI-LINK.WARN-SOUND"), 1.0f, 1.0f);
                            }
                        });
                        break;
                    default:
                        Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Invalid CANCEL-TYPE for Anti-Link in Settings. Using 'silent' as default.");
                        cancellable.setCancelled(true);
                        if ((Boolean) this.plugin.getSettings().getValue("ANTI-LINK.ALERT-ADMINS", true)) {
                            notifyAdmins(player, message);
                        }
                }
            }
        }
    }

    private boolean containsLink(String message) {
        // Split message by spaces to check each word individually
        String[] words = message.split("\\s+");

        // Check if any of the words match the URL pattern
        for (String word : words) {
            // If the word matches the URL pattern and isn't whitelisted, it's a link
            if (!isWhitelisted(word) && urlPattern.matcher(word).matches()) {
                return true;
            }
        }
        return false;
    }

    private boolean isWhitelisted(String word) {
        for (String whitelist : whitelistedLinks) {
            if (word.toLowerCase().contains(whitelist.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String sanitizeMessage(String message) {
        // Remove all non-alphanumeric characters except dots (.)
        return message.replaceAll("[^a-zA-Z0-9.]", "")
                .replace("(dot)", ".")
                .replace("[dot]", ".")
                .replace("/./", ".")
                .replace(",", ".")
                .trim();
    }


    private void notifyAdmins(Player player, String message) {
        String alert = this.plugin.getMessageManager().getMessage("ANTI-LINK.ALERT-MESSAGE");
        String formattedAlert = alert.replace("<player>", player.getName()).replace("%message%", message);
        formattedAlert = ColorHandler.color(formattedAlert);
        String permission = this.plugin.getPermissionManager().getPermission("ANTI-LINK-ADMIN-ALERT");

        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin != null && admin.hasPermission(permission) && alertManager.isAlertsDisabled(player)) {
                admin.sendMessage(formattedAlert);
                if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                    if ((boolean) plugin.getSettings().getValue("ANTI-LINK.ALERT-SOUND-ENABLED", true)) {
                        SoundUtil.playSound(player, (String) plugin.getSettings().getValue("ANTI-LINK.ALERT-SOUND"), 1.0f, 1.0f);
                    }
                } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                    XSounds.playSound(player, (String) plugin.getSettings().getValue("ANTI-LINK.ALERT-SOUND"), 1.0f, 1.0f);
                }
            }
        }
    }
}
