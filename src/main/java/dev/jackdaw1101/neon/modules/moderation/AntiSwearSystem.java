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
    private final Pattern ignorePattern;
    private final Set<String> temporaryBlacklist = new HashSet<>();
    private final Set<String> temporaryWhitelist = new HashSet<>();

    public AntiSwearSystem(Neon plugin, AlertManager alertManager, SwearManager swearManager) {
        this.plugin = plugin;
        this.alertManager = alertManager;
        this.swearManager = swearManager;
        this.ignorePattern = Pattern.compile("[^a-zA-Z]");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }


    public void addToBlacklist(String word) {
        List<String> blacklist = getBlacklist();
        if (!blacklist.contains(word.toLowerCase())) {
            blacklist.add(word.toLowerCase());
            plugin.getSettings().set("ANTI-SWEAR.BLACKLIST", blacklist);
            plugin.getSettings().save();
        }
    }

    public void removeFromBlacklist(String word) {
        List<String> blacklist = getBlacklist();
        if (blacklist.remove(word.toLowerCase())) {
            plugin.getSettings().set("ANTI-SWEAR.BLACKLIST", blacklist);
            plugin.getSettings().save();
        }
    }

    public void addToWhitelist(String word) {
        List<String> whitelist = getWhitelist();
        if (!whitelist.contains(word.toLowerCase())) {
            whitelist.add(word.toLowerCase());
            plugin.getSettings().set("ANTI-SWEAR.WHITELIST", whitelist);
            plugin.getSettings().save();
        }
    }

    public void removeFromWhitelist(String word) {
        List<String> whitelist = getWhitelist();
        if (whitelist.remove(word.toLowerCase())) {
            plugin.getSettings().set("ANTI-SWEAR.WHITELIST", whitelist);
            plugin.getSettings().save();
        }
    }

    public void addTemporaryBlacklistWord(String word) {
        temporaryBlacklist.add(word.toLowerCase());
    }

    public void removeTemporaryBlacklistWord(String word) {
        temporaryBlacklist.remove(word.toLowerCase());
    }

    public void addTemporaryWhitelistWord(String word) {
        temporaryWhitelist.add(word.toLowerCase());
    }

    public void removeTemporaryWhitelistWord(String word) {
        temporaryWhitelist.remove(word.toLowerCase());
    }

    public void clearTemporaryBlacklist() {
        temporaryBlacklist.clear();
    }

    public void clearTemporaryWhitelist() {
        temporaryWhitelist.clear();
    }

    public List<String> getBlacklist() {
        return plugin.getSettings().getStringList("ANTI-SWEAR.BLACKLIST");
    }

    public List<String> getWhitelist() {
        return plugin.getSettings().getStringList("ANTI-SWEAR.WHITELIST");
    }

    /**
     * Checks if a player's message contains any swear words
     * @param player The player to check permissions for
     * @param message The message to check
     * @return true if the message contains a swear word and player doesn't have bypass permission, false otherwise
     */
    public boolean checkForSwear(Player player, String message) {

        if (player.hasPermission(plugin.getPermissionManager().getString("ANTI-SWEAR-BYPASS"))) {
            return false;
        }


        String sanitizedMessage = sanitizeMessage(message.toLowerCase());
        List<String> blacklist = getCombinedBlacklist();
        List<String> whitelist = getCombinedWhitelist();


        for (String swear : blacklist) {

            if (whitelist.stream().anyMatch(sanitizedMessage::contains)) {
                continue;
            }


            if (sanitizedMessage.contains(swear)) {
                return true;
            }
        }

        return false;
    }

    public Set<String> getTemporaryBlacklist() {
        return Collections.unmodifiableSet(temporaryBlacklist);
    }

    public Set<String> getTemporaryWhitelist() {
        return Collections.unmodifiableSet(temporaryWhitelist);
    }

    public int getSwearStrikes(Player player) {
        return swearManager.getStrikes(player);
    }

    public void resetSwearStrikes(Player player) {
        swearManager.resetStrikes(player);
    }

    public void setSwearStrikes(Player player, int strikes) {
        swearManager.resetStrikes(player);
        for (int i = 0; i < strikes; i++) {
            swearManager.addSwear(player);
        }
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


    private void handleSwearCheck(Player player, String message, Cancellable cancellable) {
        if (player.hasPermission(plugin.getPermissionManager().getString("ANTI-SWEAR-BYPASS"))) {
            return;
        }

        String sanitizedMessage = sanitizeMessage(message.toLowerCase());
        List<String> blacklist = getCombinedBlacklist();
        List<String> whitelist = getCombinedWhitelist();
        String censorSymbol = plugin.getSettings().getString("ANTI-SWEAR.CENSOR.SYMBOL");

        for (String swear : blacklist) {
            if (whitelist.stream().anyMatch(sanitizedMessage::contains)) {
                continue;
            }

            if (sanitizedMessage.contains(swear)) {

                String censored = sanitizedMessage.replaceAll("(?i)" + Pattern.quote(swear),
                    new String(new char[swear.length()]).replace("\0", censorSymbol));


                    SwearDetectEvent detectEvent = new SwearDetectEvent(
                    player,
                    message,
                    swear,
                    censored
                );
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(detectEvent);
                });


                if (!detectEvent.isCancelled()) {
                    handleSwearViolation(
                        player,
                        detectEvent.getMessage(),
                        detectEvent.getDetectedWord(),
                        censorSymbol,
                        cancellable,
                        detectEvent.shouldNotifyAdmins(),
                        detectEvent.shouldLogToConsole()
                    );
                }
                return;
            }
        }
    }

    private void handleSwearViolation(Player player, String originalMessage, String swearWord,
                                      String censorSymbol, Cancellable cancellable, boolean notifyAdmins, boolean logToConsole) {

        cancellable.setCancelled(true);

        String cancelType = plugin.getSettings().getString("ANTI-SWEAR.CANCEL-TYPE").toLowerCase();
        boolean alertAdmins = notifyAdmins && plugin.getSettings().getBoolean("ANTI-SWEAR.ALERT-ADMINS");
        boolean log = logToConsole && plugin.getSettings().getBoolean("ANTI-SWEAR.LOG");

        switch (cancelType) {
            case "censor":
                String censoredMessage = censorMessage(originalMessage, swearWord, censorSymbol);
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


    private String sanitizeMessage(String message) {
        boolean threeReturnE = plugin.getSettings().getBoolean("ANTI-SWEAR.SENSITIVE-CHECK-THREE-RETURN-E");

        if (threeReturnE) {
            message = message
                .replace("3", "e")
                .replace("1", "i")
                .replace("!", "i")
                .replace("@", "a")
                .replace("7", "t")
                .replace("0", "o")
                .replace("5", "s")
                .replace("$", "s")
                .replace("8", "b");
        } else {
            message = message
                .replace("3", "s")
                .replace("1", "i")
                .replace("!", "i")
                .replace("@", "a")
                .replace("7", "t")
                .replace("0", "o")
                .replace("5", "s")
                .replace("$", "s")
                .replace("8", "b");
        }

        return message.replaceAll("\\p{Punct}|\\d", "").trim();
    }

    private String censorMessage(String message, String swear, String censorSymbol) {
        String cleanedSwear = ignorePattern.matcher(swear).replaceAll("").toLowerCase();
        StringBuilder replacement = new StringBuilder();
        for (int i = 0; i < swear.length(); i++) {
            replacement.append(censorSymbol);
        }

        StringBuilder regexBuilder = new StringBuilder("(?i)");
        for (char c : cleanedSwear.toCharArray()) {
            regexBuilder.append("[^a-zA-Z]*");
            regexBuilder.append(Pattern.quote(String.valueOf(c)));
        }

        try {
            return message.replaceAll(regexBuilder.toString(), replacement.toString());
        } catch (Exception e) {
            return message;
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
