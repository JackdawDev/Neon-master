package dev.jackdaw1101.neon.modules.moderation;

import dev.jackdaw1101.neon.API.modules.events.AntiLinkTriggerEvent;
import dev.jackdaw1101.neon.API.modules.events.NeonPlayerChatEvent;
import dev.jackdaw1101.neon.utils.DebugUtil;
import dev.jackdaw1101.neon.utils.webhooks.AntiAdvertiseWebhookManager;
import dev.jackdaw1101.neon.utils.logs.AntiAdvertiseLogger;
import dev.jackdaw1101.neon.commands.modules.AlertManager;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.CC;
import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.utils.sounds.ISound;
import dev.jackdaw1101.neon.utils.sounds.XSounds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.*;
import java.util.regex.Pattern;

public class AntiLinkSystem implements Listener {
    private final Neon plugin;
    private final Pattern urlPattern;
    private final Set<String> whitelistedDomains;
    private final AlertManager alertManager;

    private static final String URL_REGEX =
            "(?i)(?:^|[\\s\\[({<])(?:" +
                    "(?:https?|ftp|file)://|" +
                    "(?:www\\d{0,3}[.])|" +
                    "(?:[a-z0-9-]+[.]){1,}[a-z]{2,}" +
                    ")(?:[^\\s)\\]}>]+\\.?[^\\s)\\]}>]*)";

    private static final Pattern IP_PATTERN = Pattern.compile(
            "(?i)(?:^|[\\s\\[({<])(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(?::\\d{1,5})?(?:/[^\\s]*)?"
    );

    private static final Pattern SHORTENED_DOMAIN_PATTERN = Pattern.compile(
            "(?i)(?:^|[\\s\\[({<])(?:bit\\.ly|tinyurl\\.com|short\\.link|rb\\.gy|ow\\.ly|is\\.gd|buff\\.ly|adf\\.ly|goo\\.gl|t\\.co|[a-z0-9]+\\.\\w{2,3}/[a-z0-9]+)"
    );

    private static final Pattern DISCORD_INVITE_PATTERN = Pattern.compile(
            "(?i)(?:^|[\\s\\[({<])(?:discord(?:(?:\\.com|app\\.com)/invite|app\\.com/channels|gg)|discord\\.me)/([a-zA-Z0-9-]+)"
    );

    public AntiLinkSystem(Neon plugin, AlertManager alertManager) {
        this.plugin = plugin;
        this.urlPattern = Pattern.compile(URL_REGEX, Pattern.CASE_INSENSITIVE);
        this.whitelistedDomains = new HashSet<>();
        this.alertManager = alertManager;
        loadWhitelist();
    }

    private void loadWhitelist() {
        whitelistedDomains.clear();
        List<String> whitelist = plugin.getSettings().getStringList("ANTI-LINK.WHITELIST");
        for (String domain : whitelist) {
            whitelistedDomains.add(domain.toLowerCase().trim());
            if (domain.toLowerCase().startsWith("www.")) {
                whitelistedDomains.add(domain.toLowerCase().substring(4));
            } else {
                whitelistedDomains.add("www." + domain.toLowerCase());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(NeonPlayerChatEvent event) {
        if (event.isCancelled()) return;

        if (plugin.getSettings().getBoolean("ANTI-LINK.ENABLED")) {
            handleLinkCheck(event.getPlayer(), event.getMessage(), event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        List<String> bypassedCommands = plugin.getSettings().getStringList("ANTI-LINK.BYPASSED-COMMANDS");
        String command = event.getMessage().split(" ")[0].toLowerCase();

        if (bypassedCommands.contains(command)) {
            return;
        }

        if (plugin.getSettings().getBoolean("ANTI-LINK.CHECK-COMMANDS")) {
            handleLinkCheck(event.getPlayer(), event.getMessage(), event);
        }
    }

    private void handleLinkCheck(Player player, String message, Cancellable cancellable) {
        if (player.hasPermission(this.plugin.getPermissionManager().getString("ANTI-LINK-BYPASS"))) {
            return;
        }

        LinkInfo detectedLink = findLink(message);

        if (detectedLink != null) {
            AntiLinkTriggerEvent event = new AntiLinkTriggerEvent(
                    player,
                    message,
                    sanitizeMessage(message),
                    detectedLink.url,
                    plugin.getSettings().getString("ANTI-LINK.CANCEL-TYPE"),
                    plugin.getSettings().getBoolean("ANTI-LINK.ALERT-ADMINS"),
                    plugin.getSettings().getBoolean("ANTI-LINK.LOG"),
                    true,
                    plugin.getSettings().getString("ANTI-LINK.WARN-SOUND"),
                    plugin.getSettings().getString("ANTI-LINK.ALERT-SOUND"),
                    ColorHandler.color(plugin.getMessageManager().getString("ANTI-LINK.WARNING-MESSAGE")),
                    ColorHandler.color(plugin.getMessageManager().getString("ANTI-LINK.ALERT-MESSAGE"))
            );

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(event);
            });

            if (event.isCancelled()) {
                return;
            }

            cancellable.setCancelled(true);

            String cancelType = event.getCancelType().toLowerCase();
            switch (cancelType) {
                case "silent":
                    processSilentCancel(player, message, event);
                    break;
                case "kick":
                    processKickCancel(player, message, event);
                    break;
                default:
                    DebugUtil.debug(CC.RED + "[Neon] Invalid CANCEL-TYPE '" + cancelType + "' for Anti-Link. Using 'silent' as default.");
                    processSilentCancel(player, message, event);
            }
        }
    }

    private void processSilentCancel(Player player, String message, AntiLinkTriggerEvent event) {
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
    }

    private void processKickCancel(Player player, String message, AntiLinkTriggerEvent event) {
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            String kickMessage = event.getWarnMessage();
            player.kickPlayer(kickMessage);

            notifyAdmins(player, message, event);

            if (event.shouldSendWebhook()) {
                AntiAdvertiseWebhookManager webhook = new AntiAdvertiseWebhookManager(plugin);
                webhook.sendWebhook(player, message, "kick");
            }

            if (event.shouldLogToConsole()) {
                new AntiAdvertiseLogger(player, message, plugin);
            }
        });
    }

    private LinkInfo findLink(String message) {
        String decodedMessage = decodeObfuscatedUrls(message);

        java.util.regex.Matcher ipMatcher = IP_PATTERN.matcher(decodedMessage);
        if (ipMatcher.find()) {
            String ipUrl = ipMatcher.group().trim();
            if (!isWhitelisted(ipUrl)) {
                return new LinkInfo(ipUrl, "ip");
            }
        }

        java.util.regex.Matcher shortMatcher = SHORTENED_DOMAIN_PATTERN.matcher(decodedMessage);
        if (shortMatcher.find()) {
            String shortUrl = shortMatcher.group().trim();
            if (!isWhitelisted(shortUrl)) {
                return new LinkInfo(shortUrl, "shortened");
            }
        }

        java.util.regex.Matcher discordMatcher = DISCORD_INVITE_PATTERN.matcher(decodedMessage);
        if (discordMatcher.find()) {
            String discordUrl = discordMatcher.group().trim();
            if (!isWhitelisted(discordUrl)) {
                return new LinkInfo(discordUrl, "discord");
            }
        }

        java.util.regex.Matcher urlMatcher = urlPattern.matcher(decodedMessage);
        if (urlMatcher.find()) {
            String url = urlMatcher.group().trim();
            url = url.replaceAll("^[\\[({<]|[\\]})>]$", "");
            if (!isWhitelisted(url)) {
                return new LinkInfo(url, "url");
            }
        }

        return null;
    }

    private String decodeObfuscatedUrls(String message) {
        String decoded = message
                .toLowerCase()
                .replace("(dot)", ".")
                .replace("[dot]", ".")
                .replace("{dot}", ".")
                .replace("<dot>", ".")
                .replace(" dot ", ".")
                .replace(" dot", ".")
                .replace(",", ".")
                .replace("(at)", "@")
                .replace("[at]", "@")
                .replace("{at}", "@")
                .replace("<at>", "@")
                .replace(" at ", "@")
                .replaceAll("(?i)https?\\s*:\\s*/\\s*/", "https://")
                .replaceAll("(?i)www\\s*\\.", "www.")
                .replaceAll("(?i)\\s*\\.\\s*", ".")
                .replace("hxxp", "http")
                .replace("hxxps", "https")
                .replace("ttp://", "http://")
                .replaceAll("\\|", "")
                .replaceAll("\\*", "")
                .replaceAll("\\^", "");

        return decoded;
    }

    private boolean isWhitelisted(String url) {
        String lowerUrl = url.toLowerCase();

        for (String whitelistDomain : whitelistedDomains) {
            if (lowerUrl.contains(whitelistDomain)) {
                return true;
            }
            if (lowerUrl.endsWith("." + whitelistDomain) ||
                    lowerUrl.endsWith("/" + whitelistDomain) ||
                    lowerUrl.equals(whitelistDomain)) {
                return true;
            }
        }

        return false;
    }

    private void notifyAdmins(Player player, String message, AntiLinkTriggerEvent event) {
        String formattedAlert = event.getAlertMessage()
                .replace("<player>", player.getName())
                .replace("%message%", message)
                .replace("%link%", event.getDetectedLink());

        String permission = this.plugin.getPermissionManager().getString("ANTI-LINK-ADMIN-ALERT");

        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin != null && admin.hasPermission(permission)) {
                if (!Neon.getInstance().getAlertManager().isAlertsDisabled(admin)) {
                    admin.sendMessage(formattedAlert);
                    playSound(admin, event.getAlertSound());
                }
            }
        }
        DebugUtil.debugChecked(formattedAlert);
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

    private String sanitizeMessage(String message) {
        return message
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static class LinkInfo {
        final String url;
        final String type;

        LinkInfo(String url, String type) {
            this.url = url;
            this.type = type;
        }
    }

    public void reloadWhitelist() {
        loadWhitelist();
    }
}