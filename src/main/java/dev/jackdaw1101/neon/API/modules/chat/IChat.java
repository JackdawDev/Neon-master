package dev.jackdaw1101.neon.API.modules.chat;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.modules.chat.bubblechat.PopUpBubbleChat;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class IChat {
    private final Neon plugin;
    private final PopUpBubbleChat popUpBubbleChat;

    public IChat(Neon plugin) {
        this.plugin = plugin;
        this.popUpBubbleChat = plugin.getPopUpBubbleChat();
    }

    public String processMessageColorCodes(Player sender, String message) {
        return sender.hasPermission(this.plugin.getPermissionManager().getString("COLOR-CODES"))
                ? ChatColor.translateAlternateColorCodes('&', message)
                : removeColorCodes(message);
    }

    public String removeColorCodes(String message) {
        return message.replaceAll("&[0-9a-fk-or]", "");
    }

    public String getChatFormat(Player sender) {
        String format = ColorHandler.color(this.plugin.getSettings().getString("CHAT-FORMAT").toString());

        if (isLuckPermsInstalled()) {
            format = handleLuckPermsPrefixSuffix(sender, format);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(sender, format);
        }

        return format.replace("<player>", sender.getName()).replace("%message%", "{MESSAGE}");
    }

    public String processHoverLines(Player sender, String message) {
        List<String> hoverLines = (List) this.plugin.getSettings().getStringList("HOVER");
        if (hoverLines == null || hoverLines.isEmpty()) {
            hoverLines = new ArrayList<>(Arrays.asList("&aDefault Hover Line: &7<player>"));
        }

        StringBuilder hoverText = new StringBuilder();
        for (String line : hoverLines) {
            line = line.replace("<player>", sender.getName());
            if (this.plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
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
                    popUpBubbleChat.sendPopUpBubble(sender, rawMessage, popUpRecipients);
                }
            } else {
                popUpBubbleChat.sendPopUpBubble(sender, rawMessage, popUpRecipients);
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
            popUpBubbleChat.sendPopUpBubble(sender, message, mainRadiusRecipients);
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
            this.plugin.getServer().getConsoleSender().sendMessage(consoleMessage);
        }
    }

    private String handleLuckPermsPrefixSuffix(Player player, String format) {
        if (!isLuckPermsInstalled()) return format;

        LuckPerms luckPerms = this.plugin.getServer().getServicesManager().getRegistration(LuckPerms.class).getProvider();
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
}