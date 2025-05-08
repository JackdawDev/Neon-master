package dev.jackdaw1101.neon.modules.moderation;

import dev.jackdaw1101.neon.API.modules.events.AntiSpamEvent;
import dev.jackdaw1101.neon.manager.moderation.SwearManager;
import dev.jackdaw1101.neon.manager.commands.AlertManager;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.utils.sounds.ISound;
import dev.jackdaw1101.neon.utils.sounds.XSounds;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.UUID;

public class AntiSpamSystem implements Listener {
    private final Neon plugin;
    private final AntiSwearSystem antiSwearSystem;

    public AntiSpamSystem(Neon plugin) {
        this.plugin = plugin;
        this.antiSwearSystem = new AntiSwearSystem(plugin, new AlertManager(plugin), new SwearManager(plugin));
    }

    @EventHandler(ignoreCancelled = true)
    public void antiSpamChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String message = event.getMessage();
        UUID uuid = player.getUniqueId();


        if (plugin.getSettings().getBoolean("ANTI-SPAM.CHAT.BLOCK-SIMILAR-MESSAGES") &&
            !player.hasPermission(plugin.getPermissionManager().getString("SIMILARITY-BYPASS"))) {

            int similarityThreshold = plugin.getSettings().getInt("ANTI-SPAM.CHAT.SIMILARITY-PERCENTAGE");
            int expireTimeMs = plugin.getSettings().getInt("ANTI-SPAM.CHAT.EXPIRE") * 1000;

            SimilarityResult result = checkMessageSimilarity(uuid, message, similarityThreshold, expireTimeMs);
            if (result.isSimilar()) {
                AntiSpamEvent spamEvent = createSpamEvent(
                    player, message, result.getPreviousMessage(),
                    result.getSimilarity(), AntiSpamEvent.SpamType.SIMILAR_MESSAGE,
                    "ANTI-SPAM.CHAT.SIMILARITY-MESSAGE-WARN",
                    "ANTI-SPAM.CHAT.SIMILARITY-SOUND",
                    "ANTI-SPAM.CHAT.SIMILARITY-SOUND-ENABLED"
                );

                handleSpamEvent(event, spamEvent);
                if (event.isCancelled()) return;
            }
        }


        if (plugin.getSettings().getBoolean("ANTI-SPAM.CHAT.BLOCK-REPETITIVE-MESSAGE") &&
            !player.hasPermission(plugin.getPermissionManager().getString("ANTI-SPAM-BYPASS"))) {

            boolean isDuplicate = checkRepetitiveMessage(uuid, message,
                plugin.getSettings().getBoolean("ANTI-SPAM.CHAT.EXPIRE-ENABLED"),
                plugin.getSettings().getInt("ANTI-SPAM.CHAT.EXPIRE") * 1000);

            if (isDuplicate) {
                AntiSpamEvent spamEvent = createSpamEvent(
                    player, message, plugin.getAntiSpamManager().getLastMessage(uuid),
                    100.0, AntiSpamEvent.SpamType.REPETITIVE_MESSAGE,
                    "ANTI-SPAM.CHAT.REPETITIVE-MESSAGE-WARN",
                    "ANTI-SPAM.CHAT.REPETITIVE-SOUND",
                    "ANTI-SPAM.CHAT.REPETITIVE-SOUND-ENABLED"
                );

                handleSpamEvent(event, spamEvent);
                if (event.isCancelled()) return;
            }
        }


        int maxRepetitions = plugin.getSettings().getInt("ANTI-SPAM.CHAT.ANTI-REPETITIVE-CHARACTERS");
        if (maxRepetitions > 0 &&
            hasExcessiveRepetitiveCharacters(message, maxRepetitions) &&
            !player.hasPermission(plugin.getPermissionManager().getString("BYPASS-REPETITIVE-CHARACTER-CHAT"))) {

            AntiSpamEvent spamEvent = createSpamEvent(
                player, message, null, 0.0,
                AntiSpamEvent.SpamType.REPETITIVE_CHARACTERS,
                "ANTI-SPAM.CHAT.REPETITIVE-CHARACTER-WARN",
                "ANTI-SPAM.CHAT.ANTI-REPETITIVE-CHARACTERS-SOUND",
                "ANTI-SPAM.CHAT.ANTI-REPETITIVE-CHARACTERS-SOUND-ENABLED"
            );

            handleSpamEvent(event, spamEvent);
            if (event.isCancelled()) return;
        }


        checkChatDelay(event, player, message, uuid);
    }

    @EventHandler
    public void commandSpamProtection(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String command = event.getMessage();
        UUID uuid = player.getUniqueId();


        if (plugin.getSettings().getBoolean("ANTI-SPAM.COMMANDS.BLOCK-REPETITIVE-COMMANDS") &&
            !player.hasPermission(plugin.getPermissionManager().getString("BYPASS-DUP-COMMAND"))) {

            List<String> whitelist = plugin.getSettings().getStringList("ANTI-SPAM.COMMANDS.WHITELIST");
            if (!isCommandWhitelisted(command, whitelist)) {
                boolean isDuplicate = checkRepetitiveCommand(uuid, command,
                    plugin.getSettings().getBoolean("ANTI-SPAM.COMMANDS.EXPIRE-ENABLED"),
                    plugin.getSettings().getInt("ANTI-SPAM.COMMANDS.EXPIRE") * 1000);

                if (isDuplicate) {
                    AntiSpamEvent spamEvent = createSpamEvent(
                        player, command, String.valueOf(plugin.getAntiSpamManager().getCommandCooldownTimeMs(uuid)),
                        100.0, AntiSpamEvent.SpamType.COMMAND_REPETITIVE,
                        "ANTI-SPAM.COMMANDS.REPETITIVE-COMMAND-WARN",
                        "ANTI-SPAM.COMMANDS.SPAM-SOUND",
                        "ANTI-SPAM.COMMANDS.SPAM-SOUND-ENABLED"
                    );

                    handleSpamEvent(event, spamEvent);
                    if (event.isCancelled()) return;
                }
            }
        }


        int maxRepetitions = plugin.getSettings().getInt("ANTI-SPAM.COMMANDS.ANTI-REPETITIVE-CHARACTERS");
        if (maxRepetitions > 0 &&
            hasExcessiveRepetitiveCharacters(command, maxRepetitions) &&
            !player.hasPermission(plugin.getPermissionManager().getString("BYPASS-REPETITIVE-CHARACTER-COMMAND"))) {

            AntiSpamEvent spamEvent = createSpamEvent(
                player, command, null, 0.0,
                AntiSpamEvent.SpamType.COMMAND_REPETITIVE_CHARACTERS,
                "ANTI-SPAM.COMMANDS.REPETITIVE-CHARACTER-WARN",
                "ANTI-SPAM.COMMANDS.ANTI-REPETITIVE-CHARACTERS-SOUND",
                "ANTI-SPAM.COMMANDS.ANTI-REPETITIVE-CHARACTERS-SOUND-ENABLED"
            );

            handleSpamEvent(event, spamEvent);
            if (event.isCancelled()) return;
        }


        checkCommandDelay(event, player, uuid);
    }

    private SimilarityResult checkMessageSimilarity(UUID uuid, String newMessage, int similarityThreshold, int expireTimeMs) {
        String lastMessage = plugin.getAntiSpamManager().getLastMessage(uuid);
        long lastMessageTime = plugin.getAntiSpamManager().getLastMessageTime(uuid);

        if (System.currentTimeMillis() - lastMessageTime > expireTimeMs) {
            plugin.getAntiSpamManager().forgetLastMessage(uuid);
            return new SimilarityResult(false, 0.0, null);
        }

        if (lastMessage == null) {
            return new SimilarityResult(false, 0.0, null);
        }

        double similarity = calculateMessageSimilarity(lastMessage, newMessage);
        return new SimilarityResult(
            similarity >= (similarityThreshold / 100.0),
            similarity * 100,
            lastMessage
        );
    }

    private boolean checkRepetitiveMessage(UUID uuid, String message, boolean expireEnabled, int expireTimeMs) {
        if (plugin.getAntiSpamManager().isDuplicateMessage(uuid, message)) {
            if (expireEnabled && plugin.getAntiSpamManager().isExpiredMessage(uuid, message, expireTimeMs)) {
                plugin.getAntiSpamManager().forgetLastMessage(uuid);
                plugin.getAntiSpamManager().storeLastMessage(uuid, message);
                return false;
            }
            return true;
        }
        plugin.getAntiSpamManager().storeLastMessage(uuid, message);
        return false;
    }

    private boolean checkRepetitiveCommand(UUID uuid, String command, boolean expireEnabled, int expireTimeMs) {
        if (plugin.getAntiSpamManager().isDuplicateCommand(uuid, command)) {
            if (expireEnabled && plugin.getAntiSpamManager().isExpiredCommand(uuid, command, expireTimeMs)) {
                plugin.getAntiSpamManager().forgetLastCommand(uuid);
                plugin.getAntiSpamManager().storeLastCommand(uuid, command);
                return false;
            }
            return true;
        }
        plugin.getAntiSpamManager().storeLastCommand(uuid, command);
        return false;
    }

    private void checkChatDelay(AsyncPlayerChatEvent event, Player player, String message, UUID uuid) {
        long chatDelayMs = plugin.getSettings().getInt("ANTI-SPAM.CHAT.CHAT-DELAY");
        if (chatDelayMs > 0 && !player.hasPermission(plugin.getPermissionManager().getString("CHAT-DELAY-BYPASS"))) {
            if (plugin.getSettings().getBoolean("ANTI-SWEAR.ENABLED") &&
                this.antiSwearSystem.checkForSwear(player, message)) {
                return;
            }

            if (plugin.getAntiSpamManager().isOnCooldown(uuid)) {
                long remainingTime = plugin.getAntiSpamManager().getChatCooldownTimeMs(uuid);

                AntiSpamEvent spamEvent = createSpamEvent(
                    player, message, null, 0.0,
                    AntiSpamEvent.SpamType.CHAT_DELAY,
                    "ANTI-SPAM.CHAT.CHAT-COOLDOWN-WARN",
                    "ANTI-SPAM.CHAT.CHAT-DELAY-SOUND",
                    "ANTI-SPAM.CHAT.CHAT-DELAY-SOUND-ENABLED"
                );
                spamEvent.setWarningMessage(spamEvent.getWarningMessage()
                    .replace("{Time}", String.format("%.2f", remainingTime / 1000.0)));

                handleSpamEvent(event, spamEvent);
            } else {
                plugin.getAntiSpamManager().startCooldown(uuid, (int) chatDelayMs);
            }
        }
    }

    private void checkCommandDelay(PlayerCommandPreprocessEvent event, Player player, UUID uuid) {
        long commandDelayMs = plugin.getSettings().getInt("ANTI-SPAM.COMMANDS.COMMAND-DELAY");
        if (commandDelayMs > 0 && !player.hasPermission(plugin.getPermissionManager().getString("BYPASS-COMMAND-DELAY"))) {
            if (plugin.getAntiSpamManager().isOnCommandCooldown(uuid)) {
                long remainingTime = plugin.getAntiSpamManager().getCommandCooldownTimeMs(uuid);

                AntiSpamEvent spamEvent = createSpamEvent(
                    player, event.getMessage(), null, 0.0,
                    AntiSpamEvent.SpamType.COMMAND_DELAY,
                    "ANTI-SPAM.COMMANDS.COMMAND-COOLDOWN-WARN",
                    "ANTI-SPAM.COMMANDS.COMMAND-DELAY-SOUND",
                    "ANTI-SPAM.COMMANDS.COMMAND-DELAY-SOUND-ENABLED"
                );
                spamEvent.setWarningMessage(spamEvent.getWarningMessage()
                    .replace("{Time}", String.format("%.2f", remainingTime / 1000.0)));

                handleSpamEvent(event, spamEvent);
            } else {
                plugin.getAntiSpamManager().startCommandCooldown(uuid, (int) commandDelayMs);
            }
        }
    }

    private AntiSpamEvent createSpamEvent(Player player, String message, String previousMessage,
                                          double similarity, AntiSpamEvent.SpamType type,
                                          String warnMessagePath, String soundPath,
                                          String soundEnabledPath) {
        return new AntiSpamEvent(
            player,
            message,
            previousMessage,
            similarity,
            type,
            true,
            ColorHandler.color(plugin.getMessageManager().getString(warnMessagePath)),
            plugin.getSettings().getString(soundPath),
            plugin.getSettings().getBoolean(soundEnabledPath),
            false,
            null
        );
    }

    private void handleSpamEvent(org.bukkit.event.Cancellable event, AntiSpamEvent spamEvent) {
        plugin.getServer().getPluginManager().callEvent(spamEvent);

        if (spamEvent.isCancelled()) {
            return;
        }

        if (spamEvent.shouldCancel()) {
            event.setCancelled(true);
            spamEvent.getPlayer().sendMessage(spamEvent.getWarningMessage());

            if (spamEvent.shouldPlaySound() && spamEvent.getSound() != null) {
                if (plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                    ISound.playSound(spamEvent.getPlayer(), spamEvent.getSound(), 1.0f, 1.0f);
                } else if (plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                    XSounds.playSound(spamEvent.getPlayer(), spamEvent.getSound(), 1.0f, 1.0f);
                }
            }
        }
    }

    private static class SimilarityResult {
        private final boolean similar;
        private final double similarity;
        private final String previousMessage;

        public SimilarityResult(boolean similar, double similarity, String previousMessage) {
            this.similar = similar;
            this.similarity = similarity;
            this.previousMessage = previousMessage;
        }

        public boolean isSimilar() {
            return similar;
        }

        public double getSimilarity() {
            return similarity;
        }

        public String getPreviousMessage() {
            return previousMessage;
        }
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

        return (double) commonWords / Math.max(lastWords.length, newWords.length);
    }

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
