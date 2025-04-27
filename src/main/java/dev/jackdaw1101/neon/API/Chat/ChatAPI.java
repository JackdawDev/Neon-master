package dev.jackdaw1101.neon.API.Chat;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.Utils.ColorHandler;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatAPI {
    private final Neon plugin;

    public ChatAPI(Neon plugin) {
        this.plugin = plugin;
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

    public void sendFormattedMessage(Player viewer, String format, String hoverText, String clickCommand, boolean isHoverEnabled, boolean isClickEventEnabled, boolean isRunCommandEnabled, boolean isSuggestCommand) {
        TextComponent chatMessage = new TextComponent(TextComponent.fromLegacyText(format));

        if (isHoverEnabled) {
            chatMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(hoverText)}));
        }

        if (isClickEventEnabled) {
            if (isRunCommandEnabled) {
                chatMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand));
            }
            if (isSuggestCommand) {
                chatMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand));
            }
        }

        viewer.spigot().sendMessage(chatMessage);
    }

    public void sendMessageToConsole(String format) {
        String consoleMessage = format.replace("<player>", "[Console]");
        consoleMessage = ColorHandler.color(consoleMessage);
        this.plugin.getServer().getConsoleSender().sendMessage(consoleMessage);
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
        return format;
    }

    private boolean isLuckPermsInstalled() {
        Plugin LP = plugin.getServer().getPluginManager().getPlugin("LuckPerms");
        return LP != null && LP.isEnabled();
    }
}

