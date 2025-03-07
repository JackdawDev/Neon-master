package dev.jackdaw1101.neon.AntiSwear;

import dev.jackdaw1101.neon.AntiSpam.ListenerAntiSpam;
import dev.jackdaw1101.neon.AntiSwear.Discord.WebhookManager;
import dev.jackdaw1101.neon.AntiSwear.Logger.AntiSwearLogger;
import dev.jackdaw1101.neon.Command.Alerts.AlertManager;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Chat.CC;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    private final Pattern ignorePattern;
    private final AlertManager alertManager;


    public AntiSwearSystem(Neon plugin, AlertManager alertManager) {
        this.plugin = plugin;
        this.ignorePattern = Pattern.compile("[^a-zA-Z]");
        this.alertManager = alertManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        if ((Boolean) this.plugin.getSettings().getBoolean("ANTI-SWEAR.ENABLED")) {
            handleSwearCheck(event.getPlayer(), event.getMessage(), event);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if ((Boolean) this.plugin.getSettings().getBoolean("ANTI-SWEAR.CHECK-COMMANDS")) {
            handleSwearCheck(event.getPlayer(), event.getMessage(), event);
        }
    }

    private void handleSwearCheck(Player player, String message, Cancellable cancellable) {
        if (!player.hasPermission(this.plugin.getPermissionManager().getString("ANTI-SWEAR-BYPASS"))) {
            // Check if SENSITIVE-CHECK is enabled
            boolean sensitiveCheckEnabled = (Boolean) this.plugin.getSettings().getBoolean("ANTI-SWEAR.SENSITIVE-CHECK");
            String sanitizedMessage = message;

            if (sensitiveCheckEnabled) {
                sanitizedMessage = sanitizeMessage(message.toLowerCase());
            }

            List<String> blacklist = getSettingList("ANTI-SWEAR.BLACKLIST");
            List<String> whitelist = getSettingList("ANTI-SWEAR.WHITELIST");
            String censorSymbol = (String) this.plugin.getSettings().getString("ANTI-SWEAR.CENSOR.SYMBOL");

            for (String swear : blacklist) {
                // Check if the message contains a blacklisted word and not a whitelisted word
                if (whitelist.stream().anyMatch(sanitizedMessage::contains)) {
                    break;
                }

                boolean alertAdmins = (boolean) plugin.getSettings().getBoolean("ANTI-SWEAR.ALERT-ADMINS");
                boolean log = (boolean) plugin.getSettings().getBoolean("ANTI-SWEAR.LOG");

                if (sanitizedMessage.contains(swear)) {
                    String censoredMessage = censorMessage(message, swear, censorSymbol);
                    String cancelType = (String) this.plugin.getSettings().getString("ANTI-SWEAR.CANCEL-TYPE");
                    cancelType = cancelType.toLowerCase(); // Safely calling toLowerCase()

                    // Cancel the event to prevent the original message from being sent
                    cancellable.setCancelled(true);

                    // Handle different cancel types
                    switch (cancelType) {
                        case "censor":
                            WebhookManager webhookManager = new WebhookManager(plugin);
                            webhookManager.sendWebhook(player, message, "censor"); // For censored message
                            Bukkit.getScheduler().runTask(this.plugin, () -> {
                                if (alertAdmins) {
                                    notifyAdmins(player, message);
                                }
                                if (log) {
                                    new AntiSwearLogger(player, message, plugin);
                                }
                                player.chat(censoredMessage); // Send censored message
                            });
                            break;
                        case "silent":
                            WebhookManager webhook2 = new WebhookManager(plugin);
                            webhook2.sendWebhook(player, message, "silent"); // For silent handling
                            String warnMessage = this.plugin.getMessageManager().getString("SWEAR-WARN-MESSAGE");
                            Bukkit.getScheduler().runTask(this.plugin, () -> {
                                if (alertAdmins) {
                                    notifyAdmins(player, message);
                                }
                                if (log) {
                                    new AntiSwearLogger(player, message, plugin);
                                }
                                player.sendMessage(ColorHandler.color( warnMessage.replace("%message%", message)));
                                if ((boolean) plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                                    if ((boolean) plugin.getSettings().getBoolean("ANTI-SWEAR.WARN-SOUND-ENABLED")) {
                                        SoundUtil.playSound(player, (String) plugin.getSettings().getString("ANTI-SWEAR.WARN-SOUND"), 1.0f, 1.0f);
                                    }
                                } else if ((boolean) plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                                    XSounds.playSound(player, (String) plugin.getSettings().getString("ANTI-SWEAR.WARN-SOUND"), 1.0f, 1.0f);
                                }
                            });
                            break;
                        default:
                            Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Invalid CANCEL-TYPE for Anti-Swear in Settings. Using 'silent' as default.");
                            cancellable.setCancelled(true); // Default behavior is silent
                            if (alertAdmins) {
                                notifyAdmins(player, message);
                            }
                    }

                    // Add strike and check for punishment
                    int strikes = this.plugin.getSwearManager().addSwear(player);
                    int punishLimit = (Integer) this.plugin.getSettings().getInt("PUNISH.LIMIT");
                    boolean punishEnabled = (Boolean) this.plugin.getSettings().getBoolean("PUNISH.ENABLED");

                    if (strikes >= punishLimit && punishEnabled) {
                        String command = (String) this.plugin.getSettings().getString("PUNISH.COMMAND");
                        Bukkit.getScheduler().runTask(this.plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName())));
                    }

                    return; // Exit once a swear word is detected and handled
                }
            }
        }
    }

    private String sanitizeMessage(String message) {
        // Perform the character replacements
        boolean threereturnE = (Boolean) this.plugin.getSettings().getBoolean("ANTI-SWEAR.SENSITIVE-CHECK-THREE-RETURN-E");

        if (threereturnE) {
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

        // Remove punctuation and digits, then trim the result
        return message.replaceAll("\\p{Punct}|\\d", "").trim();
    }


    public String censorMessage(String message, String swear, String censorSymbol) {
        if (message.isEmpty() || swear.isEmpty()) {
            return message; // Return original if input is null or empty
        }

        // Clean the swear word: remove all non-alphabetic characters and make it lowercase
        String cleanedSwear = ignorePattern.matcher(swear).replaceAll("").toLowerCase();

        // Prepare the replacement string: one symbol for each character in the original swear word
        StringBuilder replacement = new StringBuilder();
        for (int i = 0; i < swear.length(); i++) {
            replacement.append(censorSymbol); // Replace each character in the swear word with the symbol
        }

        // Build the regex to match the swear word with any number of non-alphabetic characters and spaces inside the word
        StringBuilder regexBuilder = new StringBuilder("(?i)"); // Case-insensitive regex
        for (char c : cleanedSwear.toCharArray()) {
            regexBuilder.append("[^a-zA-Z]*"); // Match any non-alphabetic character or none inside the swear word
            regexBuilder.append(Pattern.quote(String.valueOf(c)));
        }
        String regex = regexBuilder.toString();

        // Replace the swear word in the original message (ignoring spaces and symbols)
        try {
            message = message.replaceAll(regex, replacement.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return message; // Return the original message if something goes wrong
        }

        return message; // Return the fully censored message with only the swear word censored
    }

    public void notifyAdmins(Player player, String message) {
        String alert = this.plugin.getMessageManager().getString("ADMIN-ALERT");
        String formattedAlert = alert.replace("<player>", player.getName()).replace("%message%", message);
        formattedAlert = ColorHandler.color(formattedAlert);
        String permission = this.plugin.getPermissionManager().getString("ADMIN-ALERT");

        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin != null && admin.hasPermission(permission) && !alertManager.isAlertsDisabled(player)) {
                admin.sendMessage(formattedAlert);
                if ((boolean) plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                    if ((boolean) plugin.getSettings().getBoolean("ANTI-SWEAR.ALERT-SOUND-ENABLED")) {
                        admin.playSound(admin.getLocation(), Sound.valueOf(
                                (String) plugin.getSettings().getString("ANTI-SWEAR.ALERT-SOUND")), 1.0f, 1.0f);
                    }
                }
            }
        }
    }

    public boolean checkForSwear(Player player, String message) {
        if (player.hasPermission(this.plugin.getPermissionManager().getString("ANTI-SWEAR-BYPASS"))) {
            return false;
        } else {
            String sanitizedMessage = ignorePattern.matcher(message.toLowerCase()).replaceAll("");
            List<String> blacklist = getSettingList("ANTI-SWEAR.BLACKLIST");
            List<String> whitelist = getSettingList("ANTI-SWEAR.WHITELIST");

            for (String swear : blacklist) {
                if (whitelist.stream().anyMatch(sanitizedMessage::contains)) {
                    break;
                }

                if (sanitizedMessage.contains(swear)) {
                    return true;
                }
            }

            return false;
        }
    }

    private List<String> getSettingList(String path) {
        return (List<String>) this.plugin.getSettings().getStringList(path);
    }
}
