package dev.jackdaw1101.neon.API;

import dev.jackdaw1101.neon.API.addons.AddonManager;
import dev.jackdaw1101.neon.API.modules.chat.IChat;
import dev.jackdaw1101.neon.API.modules.grammar.IGrammar;
import dev.jackdaw1101.neon.API.modules.moderation.IAntiSwear;
import dev.jackdaw1101.neon.API.modules.moderation.IAutoResponse;
import dev.jackdaw1101.neon.API.modules.moderation.IChatToggle;
import dev.jackdaw1101.neon.API.modules.moderation.ILogins;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

/**
 * The main API for Neon addon development.
 * Allows developers to create and manage addons with custom configurations.
 */
public class NeonAPI {
    private final AddonManager addonManager;
    private final Neon plugin;

    public NeonAPI(Neon plugin, AddonManager addonManager) {
        this.plugin = plugin;
        this.addonManager = addonManager;
    }

    /**
     * Creates a new addon for Neon
     * @param addonName The name of the addon
     * @param version The version of the addon
     * @param mainClass The main class of the addon
     * @return true if registration was successful
     */
    public boolean registerAddon(String addonName, String version, Class<?> mainClass) {
        if (addonName == null || version == null || mainClass == null) {
            plugin.getLogger().log(Level.WARNING, "Failed to register addon - parameters cannot be null");
            return false;
        }

        return addonManager.registerAddon(addonName, version, mainClass);
    }

    /**
     * Gets a list of all registered addon names
     * @return List of addon names
     */
    public List<String> getRegisteredAddons() {
        return addonManager.getRegisteredAddonNames();
    }

    /**
     * Checks if an addon is registered
     * @param addonName The name of the addon to check
     * @return true if the addon is registered
     */
    public boolean isAddonRegistered(String addonName) {
        return addonManager.isAddonRegistered(addonName);
    }

    /**
     * Creates a configuration file for an addon
     * @param addonName The name of the addon
     * @param fileName The name of the config file (without .yml extension)
     * @return The YamlConfiguration object
     */
    public YamlConfiguration createAddonConfig(String addonName, String fileName) {
        File addonFolder = new File(plugin.getDataFolder(), "Addons" + File.separator + addonName);
        if (!addonFolder.exists()) {
            addonFolder.mkdirs();
        }

        File configFile = new File(addonFolder, fileName + ".yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create config file for addon " + addonName, e);
                return null;
            }
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Gets an existing addon configuration file
     * @param addonName The name of the addon
     * @param fileName The name of the config file (without .yml extension)
     * @return The YamlConfiguration object, or null if not found
     */
    public YamlConfiguration getAddonConfig(String addonName, String fileName) {
        File configFile = new File(plugin.getDataFolder(),
            "Addons" + File.separator + addonName + File.separator + fileName + ".yml");

        if (!configFile.exists()) {
            return null;
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Saves an addon's configuration file
     * @param config The configuration to save
     * @param addonName The name of the addon
     * @param fileName The name of the config file (without .yml extension)
     * @return true if the save was successful
     */
    public boolean saveAddonConfig(YamlConfiguration config, String addonName, String fileName) {
        try {
            File configFile = new File(plugin.getDataFolder(),
                "Addons" + File.separator + addonName + File.separator + fileName + ".yml");
            config.save(configFile);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save config for addon " + addonName, e);
            return false;
        }
    }

    /**
     * Creates a subfolder for an addon
     * @param addonName The name of the addon
     * @param folderName The name of the subfolder to create
     * @return true if the folder was created or already exists
     */
    public boolean createAddonSubfolder(String addonName, String folderName) {
        File folder = new File(plugin.getDataFolder(),
            "Addons" + File.separator + addonName + File.separator + folderName);

        if (!folder.exists()) {
            return folder.mkdirs();
        }
        return true;
    }

    /**
     * Gets a subfolder for an addon
     * @param addonName The name of the addon
     * @param folderName The name of the subfolder
     * @return The File object representing the folder, or null if it doesn't exist
     */
    public File getAddonSubfolder(String addonName, String folderName) {
        File folder = new File(plugin.getDataFolder(),
            "Addons" + File.separator + addonName + File.separator + folderName);

        return folder.exists() ? folder : null;
    }

    /**
     * Gets the version of an addon
     * @param addonName The name of the addon
     * @return The version string, or null if addon isn't registered
     */
    public String getAddonVersion(String addonName) {
        return addonManager.getAddonVersion(addonName);
    }

    /**
     * Gets the main class of an addon
     * @param addonName The name of the addon
     * @return The main class, or null if addon isn't registered
     */
    public Class<?> getAddonMainClass(String addonName) {
        return addonManager.getAddonMainClass(addonName);
    }

    /**
     * Gets The neon prefix
     * @return The string
     */
    public String getNeonPrefix() {
        return ColorHandler.color(Neon.getInstance().getMessageManager().getString("PREFIX"));
    }

    /**
     * Gets The neon main theme
     * @return The string
     */
    public String getNeonMainTheme() {
        return ColorHandler.color(Neon.getInstance().getMessageManager().getString("MAIN-THEME"));
    }

    /**
     * Gets The neon second theme
     * @return The string
     */
    public String getNeonSecondTheme() {
        return ColorHandler.color(Neon.getInstance().getMessageManager().getString("SECOND-THEME"));
    }

    /**
     * Gets The neon third theme
     * @return The string
     */
    public String getNeonThirdTheme() {
        return ColorHandler.color(Neon.getInstance().getMessageManager().getString("THIRD-THEME"));
    }

    /**
     * Shows the player luckperm prefix
     * @return The string
     */
    public String handleLuckPermsPrefixSuffix(Player player, String format) {
        if (!isLuckPermsInstalled()) return format;

        LuckPerms luckPerms = Neon.getInstance().getServer().getServicesManager().getRegistration(LuckPerms.class).getProvider();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            String prefix = user.getCachedData().getMetaData().getPrefix();
            String suffix = user.getCachedData().getMetaData().getSuffix();
            format = format.replace("<lp_prefix>", (prefix != null ? prefix : ""))
                    .replace("<lp_suffix>", (suffix != null ? suffix : ""));
        }
        return ColorHandler.color(format);
    }

    /**
     * Gets if Luckperm is enabled
     * @return true false
     */
    public boolean isLuckPermsInstalled() {
        Plugin LP = Neon.getInstance().getServer().getPluginManager().getPlugin("LuckPerms");
        return LP != null && LP.isEnabled();
    }

    /**
     * Gets The neon Version
     * @return The string
     */
    public String getNeonVersion() {
        return ColorHandler.color(Neon.getInstance().getDescription().getVersion());
    }

    /**
     * Shuts down the API and cleans up resources
     */
    public void shutdown() {
        Bukkit.getConsoleSender().sendMessage("[Neon] Shutting down API...");
        Bukkit.getConsoleSender().sendMessage("[Neon] API shutdown complete.");
    }

    public void debug(String debug){
        if (Neon.getInstance().getSettings().getBoolean("DEBUG-MODE")) {
            Bukkit.getConsoleSender().sendMessage(ColorHandler.color(debug));
        }
    }

    private final IChat NeonChat = new IChat(Neon.getInstance()) {

        public String processMessageColorCodes(Player sender, String message) {
            return sender.hasPermission(Neon.getInstance().getPermissionManager().getString("COLOR-CODES"))
                    ? ChatColor.translateAlternateColorCodes('&', message)
                    : removeColorCodes(message);
        }

        public String removeColorCodes(String message) {
            return message.replaceAll("&[0-9a-fk-or]", "");
        }

        public String getChatFormat(Player sender) {
            String format = ColorHandler.color(Neon.getInstance().getSettings().getString("CHAT-FORMAT").toString());

            if (isLuckPermsInstalled()) {
                format = handleLuckPermsPrefixSuffix(sender, format);
            }

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                format = PlaceholderAPI.setPlaceholders(sender, format);
            }

            return format.replace("<player>", sender.getName()).replace("%message%", "{MESSAGE}");
        }

        public String processHoverLines(Player sender, String message) {
            List<String> hoverLines = (List) Neon.getInstance().getSettings().getStringList("HOVER");
            if (hoverLines == null || hoverLines.isEmpty()) {
                hoverLines = new ArrayList<>(Arrays.asList("&aDefault Hover Line: &7<player>"));
            }

            StringBuilder hoverText = new StringBuilder();
            for (String line : hoverLines) {
                line = line.replace("<player>", sender.getName());
                if (Neon.getInstance().getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    line = PlaceholderAPI.setPlaceholders(sender, line);
                }
                hoverText.append(ColorHandler.color(line)).append("\n");
            }

            return hoverText.toString();
        }

        public void sendFormattedMessage(Player sender, Player viewer, String format, String hoverText,
                                         String clickCommand, boolean isHoverEnabled, boolean isClickEventEnabled,
                                         boolean isRunCommandEnabled, boolean isSuggestCommand) {
            String finalFormat = format;
            boolean shouldSendPopUp = false;
            List<Player> popUpRecipients = new ArrayList<>();

            if (plugin.getSettings().getBoolean("CHAT-RADIUS.ENABLED")) {
                int mainRadius = plugin.getSettings().getInt("CHAT-RADIUS.RADIUS");
                boolean secondaryEnabled = plugin.getSettings().getBoolean("CHAT-RADIUS.SECONDARY-RADIUS.ENABLED");
                int secondaryRadius = plugin.getSettings().getInt("CHAT-RADIUS.SECONDARY-RADIUS.RADIUS");

                double distance = sender.getLocation().distance(viewer.getLocation());

                if (distance <= mainRadius) {
                    finalFormat = format;
                    shouldSendPopUp = true;
                    popUpRecipients.add(viewer);
                }
                else if (secondaryEnabled && distance <= secondaryRadius) {
                    double scramblePercent = calculateScramblePercent(distance, mainRadius, secondaryRadius);
                    finalFormat = scrambleMessage(format, scramblePercent);
                    shouldSendPopUp = false;
                }
                else if (secondaryEnabled && distance > secondaryRadius) {
                    return;
                }
                else if (!secondaryEnabled && distance > mainRadius) {
                    return;
                }
            }

            TextComponent chatMessage = new TextComponent(TextComponent.fromLegacyText(
                    ChatColor.translateAlternateColorCodes('&', finalFormat)));

            if (isHoverEnabled && hoverText != null && !hoverText.isEmpty()) {
                chatMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new TextComponent[]{new TextComponent(ChatColor.translateAlternateColorCodes('&', hoverText))}));
            }

            if (isClickEventEnabled && clickCommand != null && !clickCommand.isEmpty()) {
                if (isRunCommandEnabled) {
                    chatMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand));
                }
                if (isSuggestCommand) {
                    chatMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand));
                }
            }

            viewer.spigot().sendMessage(chatMessage);

            if (shouldSendPopUp && !popUpRecipients.isEmpty() &&
                    plugin.getSettings().getBoolean("POPUP-BUBBLE.ENABLED")) {
                String rawMessage = extractRawMessage(format);
                if (plugin.getSettings().getBoolean("POPUP-BUBBLE.PERMISSION-REQUIRED")) {
                    if (sender.hasPermission(plugin.getPermissionManager().getString("POPUP-BUBBLE.PERMISSION"))) {
                        Neon.getInstance().getPopUpBubbleChat().sendPopUpBubble(sender, rawMessage, popUpRecipients);
                    }
                } else {
                    Neon.getInstance().getPopUpBubbleChat().sendPopUpBubble(sender, rawMessage, popUpRecipients);
                }
            }
        }

        public void broadcastWithPopUp(Player sender, String message, String hoverText, String clickCommand,
                                       boolean isHoverEnabled, boolean isClickEventEnabled,
                                       boolean isRunCommandEnabled, boolean isSuggestCommand) {

            List<Player> mainRadiusRecipients = new ArrayList<>();

            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (plugin.getSettings().getBoolean("CHAT-RADIUS.ENABLED")) {
                    int mainRadius = plugin.getSettings().getInt("CHAT-RADIUS.RADIUS");
                    double distance = sender.getLocation().distance(viewer.getLocation());

                    if (distance <= mainRadius) {
                        mainRadiusRecipients.add(viewer);
                        sendFormattedMessage(sender, viewer, message, hoverText, clickCommand,
                                isHoverEnabled, isClickEventEnabled, isRunCommandEnabled, isSuggestCommand);
                    } else {
                        sendFormattedMessage(sender, viewer, message, hoverText, clickCommand,
                                isHoverEnabled, isClickEventEnabled, isRunCommandEnabled, isSuggestCommand);
                    }
                } else {
                    sendFormattedMessage(sender, viewer, message, hoverText, clickCommand,
                            isHoverEnabled, isClickEventEnabled, isRunCommandEnabled, isSuggestCommand);
                }
            }

            if (plugin.getSettings().getBoolean("POPUP-BUBBLE.ENABLED") && !mainRadiusRecipients.isEmpty()) {
                Neon.getInstance().getPopUpBubbleChat().sendPopUpBubble(sender, message, mainRadiusRecipients);
            }
        }

        private double calculateScramblePercent(double distance, int mainRadius, int secondaryRadius) {
            double secondaryDistance = distance - mainRadius;
            double secondaryRange = secondaryRadius - mainRadius;
            double progress = Math.min(1.0, Math.max(0.0, secondaryDistance / secondaryRange));
            return progress * 100;
        }

        private String scrambleMessage(String message, double scramblePercent) {
            if (scramblePercent <= 0) return message;
            if (scramblePercent >= 100) return ChatColor.MAGIC + message + ChatColor.RESET;

            String cleanMessage = ChatColor.stripColor(message);
            char[] chars = cleanMessage.toCharArray();
            StringBuilder result = new StringBuilder();

            int totalChars = chars.length;
            int charsToScramble = (int) Math.ceil(totalChars * (scramblePercent / 100.0));

            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < totalChars; i++) {
                indices.add(i);
            }
            Collections.shuffle(indices);

            Set<Integer> scrambleIndices = new HashSet<>(indices.subList(0, Math.min(charsToScramble, totalChars)));

            boolean inObfuscated = false;
            for (int i = 0; i < chars.length; i++) {
                if (scrambleIndices.contains(i)) {
                    if (!inObfuscated) {
                        result.append(ChatColor.MAGIC);
                        inObfuscated = true;
                    }
                    result.append(chars[i]);
                } else {
                    if (inObfuscated) {
                        result.append(ChatColor.RESET);
                        inObfuscated = false;
                    }
                    result.append(chars[i]);
                }
            }

            if (inObfuscated) {
                result.append(ChatColor.RESET);
            }

            return result.toString();
        }

        private String extractRawMessage(String formattedMessage) {
            String raw = ChatColor.stripColor(formattedMessage);
            raw = raw.replace("<player>", "").replace("{MESSAGE}", "").replace("%message%", "");
            raw = raw.replace("<lp_prefix>", "").replace("<lp_suffix>", "");
            raw = raw.replaceAll("\\s+", " ").trim();
            return raw.isEmpty() ? "Message" : raw;
        }

        public void sendMessageToConsole(String format) {
            String consoleMessage = format.replace("<player>", "[Console]");
            consoleMessage = ColorHandler.color(consoleMessage);
            if (plugin.getSettings().getBoolean("CHAT-IN-CONSOLE")) {
                Neon.getInstance().getServer().getConsoleSender().sendMessage(consoleMessage);
            }
        }

        private String handleLuckPermsPrefixSuffix(Player player, String format) {
            if (!isLuckPermsInstalled()) return format;

            LuckPerms luckPerms = Neon.getInstance().getServer().getServicesManager().getRegistration(LuckPerms.class).getProvider();
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                String suffix = user.getCachedData().getMetaData().getSuffix();
                format = format.replace("<lp_prefix>", (prefix != null ? prefix : ""))
                        .replace("<lp_suffix>", (suffix != null ? suffix : ""));
            }
            return ColorHandler.color(format);
        }

        private boolean isLuckPermsInstalled() {
            Plugin LP = plugin.getServer().getPluginManager().getPlugin("LuckPerms");
            return LP != null && LP.isEnabled();
        }
    };

    private final IAntiSwear AntiSwear = new IAntiSwear() {
        @Override
        public boolean isSwearWord(String word) {
            return false;
        }

        @Override
        public boolean containsSwear(String message) {
            return false;
        }

        @Override
        public void addToBlacklist(String word) {

        }

        @Override
        public void removeFromBlacklist(String word) {

        }

        @Override
        public void addToWhitelist(String word) {

        }

        @Override
        public void removeFromWhitelist(String word) {

        }

        @Override
        public void addTemporaryBlacklistWord(String word) {

        }

        @Override
        public void removeTemporaryBlacklistWord(String word) {

        }

        @Override
        public void addTemporaryWhitelistWord(String word) {

        }

        @Override
        public void removeTemporaryWhitelistWord(String word) {

        }

        @Override
        public void clearTemporaryBlacklist() {

        }

        @Override
        public void clearTemporaryWhitelist() {

        }

        @Override
        public List<String> getBlacklist() {
            return Collections.emptyList();
        }

        @Override
        public List<String> getWhitelist() {
            return Collections.emptyList();
        }

        @Override
        public Set<String> getTemporaryBlacklist() {
            return Collections.emptySet();
        }

        @Override
        public Set<String> getTemporaryWhitelist() {
            return Collections.emptySet();
        }

        @Override
        public int getSwearStrikes(Player player) {
            return 0;
        }

        @Override
        public void resetSwearStrikes(Player player) {

        }

        @Override
        public void setSwearStrikes(Player player, int strikes) {

        }

        @Override
        public String censorMessage(String message) {
            return "";
        }

        @Override
        public String sanitizeMessage(String message) {
            return "";
        }

        @Override
        public void reloadConfiguration() {

        }
    };

    private final IAutoResponse AutoResponse = new IAutoResponse() {
        @Override
        public void addResponse(String triggerWord, List<String> responses) {

        }

        @Override
        public void removeResponse(String triggerWord) {

        }

        @Override
        public void updateResponse(String triggerWord, List<String> responses) {

        }

        @Override
        public void setGlobalHoverText(List<String> hoverText) {

        }

        @Override
        public void setGlobalSound(String sound) {

        }

        @Override
        public void setGlobalSoundEnabled(boolean enabled) {

        }

        @Override
        public void setGlobalHoverEnabled(boolean enabled) {

        }

        @Override
        public Map<String, List<String>> getAllResponses() {
            return Collections.emptyMap();
        }

        @Override
        public List<String> getResponsesForWord(String triggerWord) {
            return Collections.emptyList();
        }

        @Override
        public List<String> getGlobalHoverText() {
            return Collections.emptyList();
        }

        @Override
        public String getGlobalSound() {
            return "";
        }

        @Override
        public boolean isSoundEnabled() {
            return false;
        }

        @Override
        public boolean isHoverEnabled() {
            return false;
        }

        @Override
        public void reloadResponses() {

        }

        @Override
        public void saveResponses() {

        }
    };

    private final IChatToggle ChatToggle = new IChatToggle() {
        @Override
        public void toggleChat(Player player) {

        }

        @Override
        public void setChatToggled(Player player, boolean toggled) {

        }

        @Override
        public boolean isChatToggled(Player player) {
            return false;
        }

        @Override
        public boolean isChatToggled(UUID uuid) {
            return false;
        }

        @Override
        public Set<UUID> getAllToggledPlayers() {
            return Collections.emptySet();
        }

        @Override
        public void reload() {

        }

        @Override
        public void saveAll() {

        }
    };

    private final ILogins Logins = new ILogins() {
        @Override
        public void sendCustomJoinMessage(Player player, String message, List<String> hoverText, String clickCommand, ClickAction clickAction) {

        }

        @Override
        public void sendCustomLeaveMessage(Player player, String message, List<String> hoverText, String clickCommand, ClickAction clickAction) {

        }

        @Override
        public void setJoinMessageFormat(String format) {

        }

        @Override
        public void setLeaveMessageFormat(String format) {

        }

        @Override
        public void setJoinHoverText(List<String> hoverText) {

        }

        @Override
        public void setLeaveHoverText(List<String> hoverText) {

        }

        @Override
        public void setJoinClickCommand(String command, ClickAction action) {

        }

        @Override
        public void setLeaveClickCommand(String command, ClickAction action) {

        }

        @Override
        public void setJoinHoverEnabled(boolean enabled) {

        }

        @Override
        public void setLeaveHoverEnabled(boolean enabled) {

        }

        @Override
        public void setJoinClickEnabled(boolean enabled) {

        }

        @Override
        public void setLeaveClickEnabled(boolean enabled) {

        }

        @Override
        public void setJoinRequirePermission(boolean require) {

        }

        @Override
        public void setLeaveRequirePermission(boolean require) {

        }

        @Override
        public void setJoinPermission(String permission) {

        }

        @Override
        public void setLeavePermission(String permission) {

        }

        @Override
        public void reloadConfig() {

        }
    };

    private final IGrammar GrammerApi = new IGrammar() {
        @Override
        public void addAutoCorrectWord(String incorrect, String correct) {

        }

        @Override
        public void removeAutoCorrectWord(String incorrect) {

        }

        @Override
        public void setAutoCorrectEnabled(boolean enabled) {

        }

        @Override
        public void setPunctuationCheckEnabled(boolean enabled) {

        }

        @Override
        public void setCapitalizationEnabled(boolean enabled) {

        }

        @Override
        public void setMinMessageLength(int length) {

        }

        @Override
        public Map<String, String> getAutoCorrectWords() {
            return Collections.emptyMap();
        }

        @Override
        public boolean isAutoCorrectEnabled() {
            return false;
        }

        @Override
        public boolean isPunctuationCheckEnabled() {
            return false;
        }

        @Override
        public boolean isCapitalizationEnabled() {
            return false;
        }

        @Override
        public int getMinMessageLength() {
            return 0;
        }

        @Override
        public String processMessage(String message) {
            return "";
        }
    };

    public IChat getNeonChat() {return NeonChat;}
    public IAntiSwear getAntiSwear() {return AntiSwear;}
    public IAutoResponse getAutoResponse() {return AutoResponse;}
    public IChatToggle getChatToggle() {return ChatToggle;}
    public ILogins getLogins() {return Logins;}
    public IGrammar getGrammerApi() {return GrammerApi;}

}
