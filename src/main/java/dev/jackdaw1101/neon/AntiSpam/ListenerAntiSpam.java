package dev.jackdaw1101.neon.AntiSpam;

import dev.jackdaw1101.neon.AntiSwear.AntiSwearSystem;
import dev.jackdaw1101.neon.Command.Alerts.AlertManager;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.UUID;

public class ListenerAntiSpam implements Listener {

    private final Neon plugin;
    private final AntiSwearSystem antiSwearSystem;

    public ListenerAntiSpam(Neon plugin) {
        this.plugin = plugin;
        this.antiSwearSystem = new AntiSwearSystem(plugin, new AlertManager(plugin));
    }

    @EventHandler(ignoreCancelled = true)
    public void antiSpamChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String message = event.getMessage();
        UUID uuid = player.getUniqueId();

        // Chat Anti-Spam
        boolean blockRepetitive = (boolean) plugin.getSettings().getValue("ANTI-SPAM.CHAT.BLOCK-REPETITIVE-MESSAGE");
        boolean expireEnabled = (boolean) plugin.getSettings().getValue("ANTI-SPAM.CHAT.EXPIRE-ENABLED", true);
        long expireTimeMs = (int) plugin.getSettings().getValue("ANTI-SPAM.CHAT.EXPIRE", 8) * 1000L;
        int similarityThreshold = (int) plugin.getSettings().getValue("ANTI-SPAM.CHAT.SIMILARITY-PERCENTAGE", 80);
        boolean isSimilarityBlockageEnabled = (boolean) plugin.getSettings().getValue("ANTI-SPAM.CHAT.BLOCK-SIMILAR-MESSAGES", true);  // Default to true if not set

        if (isSimilarityBlockageEnabled && isSimilarMessage(uuid, message, similarityThreshold, (int) expireTimeMs) && !player.hasPermission(plugin.getPermissionManager().getPermission("SIMILARITY-BYPASS"))) {
            player.sendMessage(ColorHandler.color(
                    plugin.getMessageManager().getMessage("ANTI-SPAM.CHAT.SIMILARITY-MESSAGE-WARN")));
            if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                if ((boolean) plugin.getSettings().getValue("ANTI-SPAM.CHAT.SIMILARITY-SOUND-ENABLED", true)) {
                    SoundUtil.playSound(player, (String) plugin.getSettings().getValue("ANTI-SPAM.CHAT.SIMILARITY-SOUND"), 1.0f, 1.0f);
                }
            } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                XSounds.playSound(player, (String) plugin.getSettings().getValue("ANTI-SPAM.CHAT.SIMILARITY-SOUND"), 1.0f, 1.0f);
            }
            event.setCancelled(true);
            return;
        }

        // Repetitive Message Blockage
        if (blockRepetitive && !player.hasPermission(plugin.getPermissionManager().getPermission("ANTI-SPAM-BYPASS"))) {
            if (plugin.getAntiSpamManager().isDuplicateMessage(uuid, message)) {
                if (expireEnabled && plugin.getAntiSpamManager().isExpiredMessage(uuid, message, (int) expireTimeMs)) {
                    plugin.getAntiSpamManager().forgetLastMessage(uuid); // Forget old message after expiration
                    plugin.getAntiSpamManager().storeLastMessage(uuid, message);
                } else {
                    player.sendMessage(ColorHandler.color(
                            plugin.getMessageManager().getMessage("ANTI-SPAM.CHAT.REPETITIVE-MESSAGE-WARN")));
                    if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                        if ((boolean) plugin.getSettings().getValue("ANTI-SPAM.CHAT.REPETITIVE-SOUND-ENABLED", true)) {
                            SoundUtil.playSound(player, (String) plugin.getSettings().getValue("ANTI-SPAM.CHAT.REPETITIVE-SOUND"), 1.0f, 1.0f);
                        }
                    } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                        XSounds.playSound(player, (String) plugin.getSettings().getValue("ANTI-SPAM.CHAT.REPETITIVE-SOUND"), 1.0f, 1.0f);
                    }
                    event.setCancelled(true);
                    return;
                }
            } else {
                plugin.getAntiSpamManager().storeLastMessage(uuid, message);
            }

            isChatDelay(event);
        }

        // Anti-Repetitive Character Detection
        int maxRepetitions = (int) plugin.getSettings().getValue("ANTI-SPAM.CHAT.ANTI-REPETITIVE-CHARACTERS", 4);
        if (maxRepetitions > 0 && hasExcessiveRepetitiveCharacters(message, maxRepetitions) && !player.hasPermission(plugin.getPermissionManager().getPermission("BYPASS-REPETITIVE-CHARACTER-CHAT"))) {
            player.sendMessage(ColorHandler.color(
                    plugin.getMessageManager().getMessage("ANTI-SPAM.CHAT.ANTI-REPETITIVE-CHARACTER-WARN")));
            if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                if ((boolean) plugin.getSettings().getValue("ANTI-SPAM.CHAT.ANTI-REPETITIVE-CHARACTERS-SOUND-ENABLED", true)) {
                    SoundUtil.playSound(player, (String) plugin.getSettings().getValue("ANTI-SPAM.CHAT.ANTI-REPETITIVE-CHARACTERS-SOUND"), 1.0f, 1.0f);
                }
            } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                XSounds.playSound(player, (String) plugin.getSettings().getValue("ANTI-SPAM.CHAT.ANTI-REPETITIVE-CHARACTERS-SOUND"), 1.0f, 1.0f);
            }
            event.setCancelled(true);
            return;
        }
    }

    public boolean isChatDelay(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        UUID uuid = player.getUniqueId();

        boolean isAntiSwearEnabled = (Boolean) this.plugin.getSettings().getValue("ANTI-SWEAR.ENABLED", true);

        long chatDelayMs = (int) plugin.getSettings().getValue("ANTI-SPAM.CHAT.CHAT-DELAY", 3000);
        if (chatDelayMs > 0 && !player.hasPermission(plugin.getPermissionManager().getPermission("CHAT-DELAY-BYPASS"))) {
            if (isAntiSwearEnabled && this.antiSwearSystem.checkForSwear(player, message)) {

                String censoredMessage = message;

                // If message is censored, cancel the event
                if (!censoredMessage.equals(message)) {
                    return isAntiSwearEnabled;
                }
            } else if
            (plugin.getAntiSpamManager().isOnCooldown(uuid)) {
                long remainingTime = plugin.getAntiSpamManager().getChatCooldownTimeMs(uuid);
                player.sendMessage(ColorHandler.color(
                        plugin.getMessageManager().getMessage("ANTI-SPAM.CHAT.CHAT-COOLDOWN-WARN")
                                .replace("{Time}", String.format("%.2f", remainingTime / 1000.0))));
                if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                    if ((boolean) plugin.getSettings().getValue("ANTI-SPAM.CHAT.CHAT-DELAY-SOUND-ENABLED", true)) {
                        SoundUtil.playSound(player, (String) plugin.getSettings().getValue("ANTI-SPAM.CHAT.CHAT-DELAY-SOUND"), 1.0f, 1.0f);
                    }
                } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                    XSounds.playSound(player, (String) plugin.getSettings().getValue("ANTI-SPAM.CHAT.CHAT-DELAY-SOUND"), 1.0f, 1.0f);
                }
                event.setCancelled(true);
            } else {
                plugin.getAntiSpamManager().startCooldown(uuid, (int) chatDelayMs);
            }
        }
        return isAntiSwearEnabled;
    }

    public boolean isSimilarMessage(UUID uuid, String newMessage, int similarityThreshold, int expireTimeMs) {
        // Get the last message and its timestamp
        String lastMessage = plugin.getAntiSpamManager().getLastMessage(uuid);
        long lastMessageTime = plugin.getAntiSpamManager().getLastMessageTime(uuid);

        // If the message is expired, forget the previous one
        if (System.currentTimeMillis() - lastMessageTime > expireTimeMs) {
            plugin.getAntiSpamManager().forgetLastMessage(uuid);
            return false;
        }

        // If no previous message exists, return false
        if (lastMessage == null) {
            return false;
        }

        // Calculate the similarity between the new message and the last message
        double similarity = calculateMessageSimilarity(lastMessage, newMessage);

        // If the similarity exceeds the threshold, return true
        return similarity >= (similarityThreshold / 100.0);
    }

    private double calculateMessageSimilarity(String lastMessage, String newMessage) {
        String[] lastWords = lastMessage.split("\\s+");
        String[] newWords = newMessage.split("\\s+");

        int commonWords = 0;
        for (String word : lastWords) {
            for (String newWord : newWords) {
                if (word.equalsIgnoreCase(newWord)) {
                    commonWords++;
                    break;
                }
            }
        }

        // Return the percentage of common words between the two messages
        return (double) commonWords / Math.max(lastWords.length, newWords.length);
    }



    @EventHandler
    public void commandSpamProtection(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        String command = event.getMessage();
        UUID uuid = player.getUniqueId();

        // Command Anti-Spam
        boolean blockRepetitive = (boolean) plugin.getSettings().getValue("ANTI-SPAM.COMMANDS.BLOCK-REPETITIVE-COMMANDS");
        boolean expireEnabled = (boolean) plugin.getSettings().getValue("ANTI-SPAM.COMMANDS.EXPIRE-ENABLED", true);
        long expireTimeMs = (int) plugin.getSettings().getValue("ANTI-SPAM.COMMANDS.EXPIRE", 4) * 1000L;

        List<String> whitelist = (List<String>) plugin.getSettings().getValue("ANTI-SPAM.COMMANDS.WHITELIST");
        if (blockRepetitive && !player.hasPermission(plugin.getPermissionManager().getPermission("BYPASS-DUP-COMMAND")) &&
                !isCommandWhitelisted(command, whitelist)) {
            if (plugin.getAntiSpamManager().isDuplicateCommand(uuid, command)) {
                if (expireEnabled && plugin.getAntiSpamManager().isExpiredCommand(uuid, command, (int) expireTimeMs)) {
                    plugin.getAntiSpamManager().forgetLastCommand(uuid); // Forget old command after expiration
                    plugin.getAntiSpamManager().storeLastCommand(uuid, command);
                } else {
                    player.sendMessage(ColorHandler.color(
                            plugin.getMessageManager().getMessage("ANTI-SPAM.COMMANDS.REPETITIVE-COMMAND-WARN")));
                    if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                        if ((boolean) plugin.getSettings().getValue("ANTI-SPAM.COMMANDS.SPAM-SOUND-ENABLED", true)) {
                            SoundUtil.playSound(player, (String) plugin.getSettings().getValue("ANTI-SPAM.COMMANDS.SPAM-SOUND"), 1.0f, 1.0f);
                        }
                    } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                        XSounds.playSound(player, (String) plugin.getSettings().getValue("ANTI-SPAM.COMMANDS.SPAM-SOUND"), 1.0f, 1.0f);
                    }
                    event.setCancelled(true);
                    return;
                }
            } else {
                plugin.getAntiSpamManager().storeLastCommand(uuid, command);
            }
        }

        // Anti-Repetitive Character Detection
        int maxRepetitions = (int) plugin.getSettings().getValue("ANTI-SPAM.COMMANDS.ANTI-REPETITIVE-CHARACTERS", 4);
        if (maxRepetitions > 0 && hasExcessiveRepetitiveCharacters(command, maxRepetitions) && !player.hasPermission(plugin.getPermissionManager().getPermission("BYPASS-REPETITIVE-CHARACTER-COMMAND"))) {
            player.sendMessage(ColorHandler.color(
                    plugin.getMessageManager().getMessage("ANTI-SPAM.COMMANDS.REPETITIVE-CHARACTER-WARN")));
            if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                if ((boolean) plugin.getSettings().getValue("ANTI-SPAM.COMMANDS.ANTI-REPETITIVE-CHARACTERS-SOUND-ENABLED", true)) {
                    SoundUtil.playSound(player, (String) plugin.getSettings().getValue("ANTI-SPAM.COMMANDS.ANTI-REPETITIVE-CHARACTERS-SOUND"), 1.0f, 1.0f);
                }
            } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                XSounds.playSound(player, (String) plugin.getSettings().getValue("ANTI-SPAM.COMMANDS.ANTI-REPETITIVE-CHARACTERS-SOUND"), 1.0f, 1.0f);
            }
            event.setCancelled(true);
            return;
        }

        // Command Cooldown
        long commandDelayMs = (int) plugin.getSettings().getValue("ANTI-SPAM.COMMANDS.COMMAND-DELAY", 1000);
        if (commandDelayMs > 0 && !player.hasPermission(plugin.getPermissionManager().getPermission("BYPASS-COMMAND-DELAY"))) {
            if (plugin.getAntiSpamManager().isOnCommandCooldown(uuid)) {
                long remainingTime = plugin.getAntiSpamManager().getCommandCooldownTimeMs(uuid);
                player.sendMessage(ColorHandler.color(
                        plugin.getMessageManager().getMessage("ANTI-SPAM.COMMANDS.COMMAND-COOLDOWN-WARN")
                                .replace("{Time}", String.format("%.2f", remainingTime / 1000.0))));
                if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                    if ((boolean) plugin.getSettings().getValue("ANTI-SPAM.COMMANDS.COMMAND-DELAY-SOUND-ENABLED", true)) {
                        SoundUtil.playSound(player, (String) plugin.getSettings().getValue("ANTI-SPAM.COMMANDS.CHAT-DELAY-SOUND"), 1.0f, 1.0f);
                    }
                } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                    XSounds.playSound(player, (String) plugin.getSettings().getValue("ANTI-SPAM.COMMANDS.COMMAND-DELAY-SOUND"), 1.0f, 1.0f);
                }
                event.setCancelled(true);
            } else {
                plugin.getAntiSpamManager().startCommandCooldown(uuid, (int) commandDelayMs);
            }
        }
    }

    /**
     * Checks if a string contains any character repeated more than the allowed limit consecutively.
     *
     * @param message The message to check.
     * @param maxRepetitions The maximum allowed repetitions of a character.
     * @return true if the message contains repetitive characters exceeding the limit.
     */
    private boolean hasExcessiveRepetitiveCharacters(String message, int maxRepetitions) {
        char lastChar = '\0';
        int count = 0;

        for (char c : message.toCharArray()) {
            if (c == lastChar) {
                count++;
                if (count > maxRepetitions) {
                    return true;
                }
            } else {
                count = 1;
                lastChar = c;
            }
        }

        return false;
    }

    private boolean isCommandWhitelisted(String command, List<String> whitelist) {
        return whitelist.stream().anyMatch(command::startsWith);
    }
}
