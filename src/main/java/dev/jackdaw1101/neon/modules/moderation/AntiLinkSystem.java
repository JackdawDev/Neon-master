package dev.jackdaw1101.neon.modules.moderation;

import dev.jackdaw1101.neon.API.modules.events.AntiLinkTriggerEvent;
import dev.jackdaw1101.neon.utils.webhooks.AntiAdvertiseWebhookManager;
import dev.jackdaw1101.neon.utils.logs.AntiAdvertiseLogger;
import dev.jackdaw1101.neon.manager.commands.AlertManager;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.CC;
import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.utils.sounds.ISound;
import dev.jackdaw1101.neon.utils.sounds.XSounds;
import org.bukkit.Bukkit;
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
        this.whitelistedLinks = plugin.getSettings().getStringList("ANTI-LINK.WHITELIST");
        this.alertManager = alertManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        if (plugin.getSettings().getBoolean("ANTI-LINK.ENABLED")) {
            handleLinkCheck(event.getPlayer(), event.getMessage(), event);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (plugin.getSettings().getBoolean("ANTI-LINK.CHECK-COMMANDS")) {
            handleLinkCheck(event.getPlayer(), event.getMessage(), event);
        }
    }

    private void handleLinkCheck(Player player, String message, Cancellable cancellable) {
        if (!player.hasPermission(this.plugin.getPermissionManager().getString("ANTI-LINK-BYPASS"))) {
            String sanitizedMessage = sanitizeMessage(message);
            String detectedLink = findLink(sanitizedMessage);

            if (detectedLink != null) {
                AntiLinkTriggerEvent event = new AntiLinkTriggerEvent(
                    player,
                    message,
                    sanitizedMessage,
                    detectedLink,
                    plugin.getSettings().getString("ANTI-LINK.CANCEL-TYPE"),
                    plugin.getSettings().getBoolean("ANTI-LINK.ALERT-ADMINS"),
                    plugin.getSettings().getBoolean("ANTI-LINK.LOG"),
                    true,
                    plugin.getSettings().getString("ANTI-LINK.WARN-SOUND"),
                    plugin.getSettings().getString("ANTI-LINK.ALERT-SOUND"),
                    ColorHandler.color(plugin.getMessageManager().getString("ANTI-LINK.WARNING-MESSAGE")),
                    ColorHandler.color(plugin.getMessageManager().getString("ANTI-LINK.ALERT-MESSAGE"))
                );

                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return;
                }

                cancellable.setCancelled(true);

                switch (event.getCancelType()) {
                    case "silent":
                        Bukkit.getScheduler().runTask(this.plugin, () -> {
                            if (event.shouldAlertAdmins()) {
                                notifyAdmins(player, message, event);
                            }
                            player.sendMessage(event.getWarnMessage());

                            if (event.shouldSendWebhook()) {
                                AntiAdvertiseWebhookManager webhook = new AntiAdvertiseWebhookManager(plugin);
                                webhook.sendWebhook(player, message, "silent");
                            }

                            if (event.shouldLogToConsole()) {
                                new AntiAdvertiseLogger(player, message, plugin);
                            }

                            playSound(player, event.getWarnSound());
                        });
                        break;
                    default:
                        Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Invalid CANCEL-TYPE for Anti-Link in Settings. Using 'silent' as default.");
                        cancellable.setCancelled(true);
                        if (event.shouldAlertAdmins()) {
                            notifyAdmins(player, message, event);
                        }
                }
            }
        }
    }

    private String findLink(String message) {
        String[] words = message.split("\\s+");
        for (String word : words) {
            if (!isWhitelisted(word) && urlPattern.matcher(word).matches()) {
                return word;
            }
        }
        return null;
    }

    private void notifyAdmins(Player player, String message, AntiLinkTriggerEvent event) {
        String formattedAlert = event.getAlertMessage()
            .replace("<player>", player.getName())
            .replace("%message%", message)
            .replace("%link%", event.getDetectedLink());

        String permission = this.plugin.getPermissionManager().getString("ANTI-LINK-ADMIN-ALERT");

        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin != null && admin.hasPermission(permission)) {
                admin.sendMessage(formattedAlert);
                playSound(admin, event.getAlertSound());
            }
        }
    }

    private void playSound(Player player, String sound) {
        if (sound != null && !sound.isEmpty()) {
            if (plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                ISound.playSound(player, sound, 1.0f, 1.0f);
            } else if (plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                XSounds.playSound(player, sound, 1.0f, 1.0f);
            }
        }
    }

    private boolean containsLink(String message) {

        String[] words = message.split("\\s+");


        for (String word : words) {

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

        return message.replaceAll("[^a-zA-Z0-9.]", "")
                .replace("(dot)", ".")
                .replace("[dot]", ".")
                .replace("/./", ".")
                .replace(",", ".")
                .trim();
    }
}
