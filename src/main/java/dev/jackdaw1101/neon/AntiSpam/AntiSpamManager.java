package dev.jackdaw1101.neon.AntiSpam;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class AntiSpamManager {

    private final Map<UUID, String> lastMessages = new HashMap<>();
    private final Map<UUID, Long> lastMessageTimestamps = new HashMap<>();
    private final Map<UUID, String> lastCommands = new HashMap<>();
    private final Map<UUID, Long> lastCommandTimestamps = new HashMap<>();
    private final Map<UUID, Long> chatCooldowns = new HashMap<>();
    private final Map<UUID, Long> commandCooldowns = new HashMap<>();

    /**
     * Checks if the player's message is a duplicate of their last message.
     */
    public boolean isDuplicateMessage(UUID uuid, String message) {
        return lastMessages.containsKey(uuid) && lastMessages.get(uuid).equalsIgnoreCase(message);
    }

    /**
     * Forgets the player's last command by removing it from storage.
     */
    public void forgetLastCommand(UUID uuid) {
        lastCommands.remove(uuid);
        lastCommandTimestamps.remove(uuid);
    }

    /**
     * Forgets the player's last message by removing it from storage.
     */
    public void forgetLastMessage(UUID uuid) {
        lastMessages.remove(uuid);
        lastMessageTimestamps.remove(uuid);
    }

    /**
     * Stores the player's last message for duplicate checking.
     */
    public void storeLastMessage(UUID uuid, String message) {
        lastMessages.put(uuid, message);
        lastMessageTimestamps.put(uuid, System.currentTimeMillis());
    }

    /**
     * Retrieves the player's last message.
     *
     * @param uuid The UUID of the player.
     * @return The last message sent by the player, or null if none exists.
     */
    public String getLastMessage(UUID uuid) {
        return lastMessages.get(uuid);
    }


    /**
     * Checks if the player's message has expired.
     */
    public boolean isExpiredMessage(UUID uuid, String message, int expireTimeMs) {
        return System.currentTimeMillis() - getLastMessageTime(uuid) > expireTimeMs;
    }

    /**
     * Gets the last message timestamp for a player.
     */
    public long getLastMessageTime(UUID uuid) {
        return lastMessageTimestamps.getOrDefault(uuid, 0L);
    }

    /**
     * Checks if the player is on a chat cooldown.
     */
    public boolean isOnCooldown(UUID uuid) {
        return chatCooldowns.containsKey(uuid) && chatCooldowns.get(uuid) > System.currentTimeMillis();
    }

    /**
     * Starts a chat cooldown for the player.
     */
    public void startCooldown(UUID uuid, int millis) {
        chatCooldowns.put(uuid, System.currentTimeMillis() + millis);
    }

    /**
     * Gets the remaining time on the player's chat cooldown in milliseconds.
     */
    public long getChatCooldownTimeMs(UUID uuid) {
        if (!isOnCooldown(uuid)) return 0L;
        return chatCooldowns.get(uuid) - System.currentTimeMillis();
    }

    /**
     * Checks if the player's command is a duplicate of their last command.
     */
    public boolean isDuplicateCommand(UUID uuid, String command) {
        return lastCommands.containsKey(uuid) && lastCommands.get(uuid).equalsIgnoreCase(command);
    }

    /**
     * Stores the player's last command for duplicate checking.
     */
    public void storeLastCommand(UUID uuid, String command) {
        lastCommands.put(uuid, command);
        lastCommandTimestamps.put(uuid, System.currentTimeMillis());
    }

    /**
     * Checks if the player's command has expired.
     */
    public boolean isExpiredCommand(UUID uuid, String command, int expireTimeMs) {
        return System.currentTimeMillis() - getLastCommandTime(uuid) > expireTimeMs;
    }

    /**
     * Gets the last command timestamp for a player.
     */
    public long getLastCommandTime(UUID uuid) {
        return lastCommandTimestamps.getOrDefault(uuid, 0L);
    }

    /**
     * Checks if the player is on a command cooldown.
     */
    public boolean isOnCommandCooldown(UUID uuid) {
        return commandCooldowns.containsKey(uuid) && commandCooldowns.get(uuid) > System.currentTimeMillis();
    }

    /**
     * Starts a command cooldown for the player.
     */
    public void startCommandCooldown(UUID uuid, int millis) {
        commandCooldowns.put(uuid, System.currentTimeMillis() + millis);
    }

    /**
     * Gets the remaining time on the player's command cooldown in milliseconds.
     */
    public long getCommandCooldownTimeMs(UUID uuid) {
        if (!isOnCommandCooldown(uuid)) return 0L;
        return commandCooldowns.get(uuid) - System.currentTimeMillis();
    }

    /**
     * Clears the stored message for a player after expiration.
     */
    public void clearExpiredMessage(UUID uuid) {
        lastMessages.remove(uuid);
        lastMessageTimestamps.remove(uuid);
    }

    /**
     * Clears the stored command for a player after expiration.
     */
    public void clearExpiredCommand(UUID uuid) {
        lastCommands.remove(uuid);
        lastCommandTimestamps.remove(uuid);
    }

    /**
     * Checks if the player's last message or command data exists and is expired.
     */
    public boolean hasExpiredData(UUID uuid, int expireTimeMs) {
        return (lastMessageTimestamps.containsKey(uuid) &&
                System.currentTimeMillis() - lastMessageTimestamps.get(uuid) > expireTimeMs) ||
                (lastCommandTimestamps.containsKey(uuid) &&
                        System.currentTimeMillis() - lastCommandTimestamps.get(uuid) > expireTimeMs);
    }
}
