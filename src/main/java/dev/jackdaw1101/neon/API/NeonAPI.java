package dev.jackdaw1101.neon.API;

import dev.jackdaw1101.neon.AddonHandler.AddonManager;
import dev.jackdaw1101.neon.Configurations.Messages;
import dev.jackdaw1101.neon.Configurations.Settings;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Pattern;

public class NeonAPI {

    private final Settings settings;
    private final AddonManager addonManager;
    private final Messages messages;
    private final Neon plugin;
    private final Pattern ignorePattern;


    public NeonAPI(Neon plugin, Settings settings, AddonManager addonManager, Messages messages) {
        this.settings = settings;
        this.addonManager = addonManager;
        this.messages = messages;
        this.plugin = plugin;
        this.ignorePattern = Pattern.compile("[^a-zA-Z]"); // Only handle alphabetic characters
    }

        /**
         * Triggered when a player swears.
         *
         * @param player  The player who swore.
         * @param message The message containing the swear.
         */
        public void onPlayerSwear(Player player, String message) {
            // Notify admins
            notifyAdmins(player, message);

            // Log the event
            logSwearEvent(player, message);

            // Add a strike to the player
            int strikes = plugin.getSwearManager().addSwear(player);
            handlePunishment(player, strikes, message);
        }

    /**
     * Handles punishment logic based on the player's strike count.
     *
     * end the api.
     *
     */
    public void stopAPI() {
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Stopping API...");
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Successfully Stopped API.");
    }

        /**
         * Handles punishment logic based on the player's strike count.
         *
         * @param player  The player to punish.
         * @param strikes The player's current strike count.
         * @param message The offending message.
         */
        public void handlePunishment(Player player, int strikes, String message) {
            int punishLimit = (int) plugin.getSettings().getValue("PUNISH.LIMIT", 3);
            boolean punishEnabled = (boolean) plugin.getSettings().getValue("PUNISH.ENABLED", true);

            if (strikes >= punishLimit && punishEnabled) {
                String command = (String) plugin.getSettings().getValue("PUNISH.COMMAND", "kick %player%");
                plugin.getServer().dispatchCommand(
                        plugin.getServer().getConsoleSender(),
                        command.replace("%player%", player.getName())
                );
                sendPunishmentNotification(player, message, strikes);
            }
        }

        /**
         * Sends a notification to admins when a player swears.
         *
         * @param player  The player who swore.
         * @param message The swear message.
         */
        public void notifyAdmins(Player player, String message) {
            String alert = plugin.getMessageManager().getMessage("ADMIN_ALERT");
            String formattedAlert = alert.replace("<player>", player.getName()).replace("%message%", message);
            String permission = plugin.getPermissionManager().getPermission("ADMIN-ALERT");

            plugin.getServer().getOnlinePlayers().forEach(admin -> {
                if (admin.hasPermission(permission)) {
                    admin.sendMessage(formattedAlert);
                }
            });
        }

        /**
         * Logs a swear event.
         *
         * @param player  The player who swore.
         * @param message The offending message.
         */
        public void logSwearEvent(Player player, String message) {
            boolean logEnabled = (boolean) plugin.getSettings().getValue("ANTI-SWEAR.LOG", true);
            if (logEnabled) {
                // Example logger, customize as needed
                plugin.getLogger().info(player.getName() + " swore: " + message);
            }
        }

        /**
         * Sends a punishment notification to the player.
         *
         * @param player  The player being punished.
         * @param message The swear message.
         * @param strikes The player's strike count.
         */
        public void sendPunishmentNotification(Player player, String message, int strikes) {
            String punishMessage = plugin.getMessageManager().getMessage("PUNISH_NOTIFY");
            if (punishMessage != null) {
                player.sendMessage(
                        punishMessage
                                .replace("<strikes>", String.valueOf(strikes))
                                .replace("%message%", message)
                );
            }
        }

        /**
         * Checks if a word is blacklisted.
         *
         * @param word The word to check.
         * @return True if the word is blacklisted, false otherwise.
         */
        public boolean isWordBlacklisted(String word) {
            List<String> blacklist = (List<String>) plugin.getSettings().getValue("ANTI-SWEAR.BLACKLIST");
            return blacklist.stream().anyMatch(word::equalsIgnoreCase);
        }

        /**
         * Censors a message based on a blacklist.
         *
         * @param message      The message to censor.
         * @param censorSymbol The symbol to replace blacklisted words with.
         * @return The censored message.
         */
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


    /**
     * Registers an addon by name and version.
     *
     * @param addonName    The name of the addon.
     * @param addonVersion The version of the addon.
     * @return True if registration was successful, false otherwise.
     */
    public boolean registerAddon(String addonName, String addonVersion) {
        return addonManager.registerAddon(addonName, addonVersion);
    }

    /**
     * Fetches a message from the messages.yml file.
     *
     * @param key          The key of the message.
     * @param placeholders Optional placeholders to replace in the message.
     *                     Example: getMessage("welcome", "{player}", "Jack") replaces "{player}" with "Jack".
     * @return The formatted message, or the key if the message doesn't exist.
     */
    public String getMessage(String key, Object... placeholders) {
        return messages.getMessage(key, placeholders);
    }

    /**
     * Reloads the messages.yml configuration file.
     *
     * @return True if the reload was successful, false otherwise.
     */
    public boolean reloadMessages() {
        return messages.reloadMessages();
    }

    /**
     * Retrieves a configuration value for an addon.
     *
     * @param addonName The name of the addon.
     * @param key       The key to retrieve.
     * @param <T>       The type of the value.
     * @return The value if it exists, null otherwise.
     */
    public <T> T getAddonSetting(String addonName, String key, Class<T> type) {
        String path = "ADDONS." + addonName + "." + key;
        return getSettingAPI(path, type);
    }

    /**
     * Retrieves a general configuration value.
     *
     * @param path The path in the settings file.
     * @param <T>  The type of the value.
     * @return The value at the path, or null if it doesn't exist.
     */
    public <T> T getSettingAPI(String path, Class<T> type) {
        Object value = settings.getSettingsConfig().get(path);

        if (value == null) {
            return null;
        }

        if (type.isInstance(value)) {
            return type.cast(value);
        } else {
            throw new IllegalArgumentException("The value at " + path + " is not of type " + type.getName());
        }
    }

    /**
     * @return The AddonManager instance.
     */
    public AddonManager getAddonManager() {
        return addonManager;
    }
}
