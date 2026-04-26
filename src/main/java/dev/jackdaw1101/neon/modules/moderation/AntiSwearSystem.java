package dev.jackdaw1101.neon.modules.moderation;

import dev.jackdaw1101.neon.API.modules.events.SwearDetectEvent;
import dev.jackdaw1101.neon.API.modules.events.SwearPunishEvent;
import dev.jackdaw1101.neon.manager.moderation.SwearManager;
import dev.jackdaw1101.neon.utils.webhooks.AntiSwearWebhookManager;
import dev.jackdaw1101.neon.utils.logs.AntiSwearLogger;
import dev.jackdaw1101.neon.manager.commands.AlertManager;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.CC;
import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.implementions.IAntiSwearImpl;
import dev.jackdaw1101.neon.API.modules.moderation.IAntiSwear;
import dev.jackdaw1101.neon.utils.sounds.ISound;
import dev.jackdaw1101.neon.utils.sounds.XSounds;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.*;
import java.util.regex.Pattern;

public class AntiSwearSystem implements Listener {
    private final Neon plugin;
    private final AlertManager alertManager;
    private final SwearManager swearManager;
    private final IAntiSwear antiSwearAPI;
    private final Pattern ignorePattern;
    private final Set<String> temporaryBlacklist = new HashSet<>();
    private final Set<String> temporaryWhitelist = new HashSet<>();

    public AntiSwearSystem(Neon plugin, AlertManager alertManager, SwearManager swearManager) {
        this.plugin = plugin;
        this.alertManager = alertManager;
        this.swearManager = swearManager;
        this.antiSwearAPI = new IAntiSwearImpl(plugin, swearManager);
        this.ignorePattern = Pattern.compile("[^a-zA-Z]");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public IAntiSwear getAPI() {
        return antiSwearAPI;
    }

    public void addToBlacklist(String word) {
        antiSwearAPI.addToBlacklist(word);
    }

    public void removeFromBlacklist(String word) {
        antiSwearAPI.removeFromBlacklist(word);
    }

    public void addToWhitelist(String word) {
        antiSwearAPI.addToWhitelist(word);
    }

    public void removeFromWhitelist(String word) {
        antiSwearAPI.removeFromWhitelist(word);
    }

    public void addTemporaryBlacklistWord(String word) {
        antiSwearAPI.addTemporaryBlacklistWord(word);
        temporaryBlacklist.add(word.toLowerCase());
    }

    public void removeTemporaryBlacklistWord(String word) {
        antiSwearAPI.removeTemporaryBlacklistWord(word);
        temporaryBlacklist.remove(word.toLowerCase());
    }

    public void addTemporaryWhitelistWord(String word) {
        antiSwearAPI.addTemporaryWhitelistWord(word);
        temporaryWhitelist.add(word.toLowerCase());
    }

    public void removeTemporaryWhitelistWord(String word) {
        antiSwearAPI.removeTemporaryWhitelistWord(word);
        temporaryWhitelist.remove(word.toLowerCase());
    }

    public void clearTemporaryBlacklist() {
        antiSwearAPI.clearTemporaryBlacklist();
        temporaryBlacklist.clear();
    }

    public void clearTemporaryWhitelist() {
        antiSwearAPI.clearTemporaryWhitelist();
        temporaryWhitelist.clear();
    }

    public List<String> getBlacklist() {
        return antiSwearAPI.getBlacklist();
    }

    public List<String> getWhitelist() {
        return antiSwearAPI.getWhitelist();
    }

    public Set<String> getTemporaryBlacklist() {
        return antiSwearAPI.getTemporaryBlacklist();
    }

    public Set<String> getTemporaryWhitelist() {
        return antiSwearAPI.getTemporaryWhitelist();
    }

    public int getSwearStrikes(Player player) {
        return antiSwearAPI.getSwearStrikes(player);
    }

    public void resetSwearStrikes(Player player) {
        antiSwearAPI.resetSwearStrikes(player);
    }

    public void setSwearStrikes(Player player, int strikes) {
        antiSwearAPI.setSwearStrikes(player, strikes);
    }

    public boolean checkForSwear(Player player, String message) {
        if (player.hasPermission(plugin.getPermissionManager().getString("ANTI-SWEAR-BYPASS"))) {
            return false;
        }
        return antiSwearAPI.containsSwear(message);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled() || !plugin.getSettings().getBoolean("ANTI-SWEAR.ENABLED")) {
            return;
        }

        handleSwearCheck(event.getPlayer(), event.getMessage(), event);
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled() || !plugin.getSettings().getBoolean("ANTI-SWEAR.CHECK-COMMANDS")) {
            return;
        }

        handleSwearCheck(event.getPlayer(), event.getMessage(), event);
    }

    private String prepareMessageForCheck(String message) {
        boolean ignoreSpaces = plugin.getSettings().getBoolean("ANTI-SWEAR.IGNORE-SPACES");

        if (ignoreSpaces) {
            // Remove all spaces for bypass detection
            return message.replaceAll("\\s+", "");
        }
        return message;
    }

    private void handleSwearCheck(Player player, String message, Cancellable cancellable) {
        if (player.hasPermission(plugin.getPermissionManager().getString("ANTI-SWEAR-BYPASS"))) {
            return;
        }

        String messageToCheck = prepareMessageForCheck(message);

        if (!antiSwearAPI.containsSwear(messageToCheck)) {
            return;
        }

        String detectedWord = findDetectedWord(messageToCheck);
        String censoredMessage = antiSwearAPI.censorMessage(message);

        SwearDetectEvent detectEvent = new SwearDetectEvent(
                player,
                message,
                detectedWord,
                censoredMessage
        );

        // Call event synchronously
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(detectEvent);
        });

        if (!detectEvent.isCancelled()) {
            handleSwearViolation(
                    player,
                    detectEvent.getMessage(),
                    detectEvent.getDetectedWord(),
                    plugin.getSettings().getString("ANTI-SWEAR.CENSOR.SYMBOL"),
                    cancellable,
                    detectEvent.shouldNotifyAdmins(),
                    detectEvent.shouldLogToConsole()
            );
        }
    }

    private String findDetectedWord(String message) {
        String sanitized = antiSwearAPI.sanitizeMessage(message);
        List<String> blacklist = antiSwearAPI.getBlacklist();

        for (String swear : blacklist) {
            if (sanitized.contains(swear)) {
                return swear;
            }
        }
        return "unknown";
    }

    private void handleSwearViolation(Player player, String originalMessage, String swearWord,
                                      String censorSymbol, Cancellable cancellable, boolean notifyAdmins, boolean logToConsole) {

        cancellable.setCancelled(true);

        String cancelType = plugin.getSettings().getString("ANTI-SWEAR.CANCEL-TYPE").toLowerCase();
        boolean alertAdmins = notifyAdmins && plugin.getSettings().getBoolean("ANTI-SWEAR.ALERT-ADMINS");
        boolean log = logToConsole && plugin.getSettings().getBoolean("ANTI-SWEAR.LOG");

        switch (cancelType) {
            case "censor":
                String censoredMessage = antiSwearAPI.censorMessage(originalMessage);
                AntiSwearWebhookManager webhookManager = new AntiSwearWebhookManager(plugin);
                webhookManager.sendWebhook(player, originalMessage, "censor");

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (alertAdmins) notifyAdmins(player, originalMessage);
                    if (log) new AntiSwearLogger(player, originalMessage, plugin);
                    player.chat(censoredMessage);
                });
                break;

            case "silent":
                AntiSwearWebhookManager webhook2 = new AntiSwearWebhookManager(plugin);
                webhook2.sendWebhook(player, originalMessage, "silent");
                String warnMessage = plugin.getMessageManager().getString("SWEAR-WARN-MESSAGE");

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (alertAdmins) notifyAdmins(player, originalMessage);
                    if (log) new AntiSwearLogger(player, originalMessage, plugin);
                    player.sendMessage(ColorHandler.color(warnMessage.replace("%message%", originalMessage)));
                    playWarnSound(player);
                });
                break;

            default:
                Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Invalid CANCEL-TYPE for Anti-Swear in Settings. Using 'silent' as default.");
                if (alertAdmins) notifyAdmins(player, originalMessage);
        }

        handleSwearPunishment(player);
    }

    private void handleSwearPunishment(Player player) {
        int strikes = swearManager.addSwear(player);
        int punishLimit = plugin.getSettings().getInt("PUNISH.LIMIT");
        boolean punishEnabled = plugin.getSettings().getBoolean("PUNISH.ENABLED");

        if (strikes >= punishLimit && punishEnabled) {
            String command = plugin.getSettings().getString("PUNISH.COMMAND");

            SwearPunishEvent event = new SwearPunishEvent(player, "", strikes, command);

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(event);
            });

            String finalCommand = event.getPunishCommand();

            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand.replace("%player%", player.getName())));
            swearManager.resetStrikes(player);
        }
    }

    private void notifyAdmins(Player player, String message) {
        String alert = plugin.getMessageManager().getString("ADMIN-ALERT");
        String formattedAlert = ColorHandler.color(alert
                .replace("<player>", player.getName())
                .replace("%message%", message));

        String permission = plugin.getPermissionManager().getString("ADMIN-ALERT");

        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission(permission) && !alertManager.isAlertsDisabled(admin)) {
                admin.sendMessage(formattedAlert);
                playAlertSound(admin);
            }
        }
    }

    private void playWarnSound(Player player) {
        if (plugin.getSettings().getBoolean("ISOUNDS-UTIL") &&
                plugin.getSettings().getBoolean("ANTI-SWEAR.WARN-SOUND-ENABLED")) {
            ISound.playSound(player, plugin.getSettings().getString("ANTI-SWEAR.WARN-SOUND"), 1.0f, 1.0f);
        } else if (plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
            XSounds.playSound(player, plugin.getSettings().getString("ANTI-SWEAR.WARN-SOUND"), 1.0f, 1.0f);
        }
    }

    private void playAlertSound(Player admin) {
        if (plugin.getSettings().getBoolean("ANTI-SWEAR.ALERT-SOUND-ENABLED")) {
            if (plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                admin.playSound(admin.getLocation(),
                        Sound.valueOf(plugin.getSettings().getString("ANTI-SWEAR.ALERT-SOUND")), 1.0f, 1.0f);
            } else if (plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                XSounds.playSound(admin, plugin.getSettings().getString("ANTI-SWEAR.ALERT-SOUND"), 1.0f, 1.0f);
            }
        }
    }

    private List<String> getCombinedBlacklist() {
        List<String> combined = new ArrayList<>(getBlacklist());
        combined.addAll(temporaryBlacklist);
        return combined;
    }

    private List<String> getCombinedWhitelist() {
        List<String> combined = new ArrayList<>(getWhitelist());
        combined.addAll(temporaryWhitelist);
        return combined;
    }
}