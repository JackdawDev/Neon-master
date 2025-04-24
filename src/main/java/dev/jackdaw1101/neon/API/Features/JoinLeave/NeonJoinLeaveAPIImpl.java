package dev.jackdaw1101.neon.API.Features.JoinLeave;

import dev.jackdaw1101.neon.API.Features.JoinLeave.Events.NeonPlayerJoinEvent;
import dev.jackdaw1101.neon.API.Features.JoinLeave.Events.NeonPlayerLeaveEvent;
import dev.jackdaw1101.neon.Configurations.ConfigFile;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NeonJoinLeaveAPIImpl implements NeonJoinLeaveAPI {
    private final Neon plugin;
    private final ConfigFile settings;

    public NeonJoinLeaveAPIImpl(Neon plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();
    }

    @Override
    public void sendCustomJoinMessage(Player player, String message, List<String> hoverText, String clickCommand, ClickAction clickAction) {
        // Process the message first
        String processedMessage = ColorHandler.color(message.replace("<player>", player.getName()));
        processedMessage = PlaceholderAPI.setPlaceholders(player, processedMessage);

        // Process hover text if exists
        List<String> processedHoverText = null;
        if (hoverText != null && !hoverText.isEmpty()) {
            processedHoverText = new ArrayList<>();
            for (String line : hoverText) {
                String processedLine = ColorHandler.color(line.replace("<player>", player.getName()));
                processedHoverText.add(PlaceholderAPI.setPlaceholders(player, processedLine));
            }
        }

        // Process click command if exists
        String processedClickCommand = null;
        if (clickCommand != null && !clickCommand.isEmpty()) {
            processedClickCommand = clickCommand.replace("<player>", player.getName());
            processedClickCommand = PlaceholderAPI.setPlaceholders(player, processedClickCommand);
        }

        // Create and send the message component directly
        TextComponent messageComponent = new TextComponent(processedMessage);

        // Add hover event if enabled
        if (processedHoverText != null) {
            String joinedHoverText = String.join("\n", processedHoverText);
            messageComponent.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new BaseComponent[]{new TextComponent(joinedHoverText)}
            ));
        }

        // Add click event if enabled
        if (processedClickCommand != null && clickAction != null) {
            ClickEvent.Action action;
            switch (clickAction) {
                case RUN_COMMAND:
                    action = ClickEvent.Action.RUN_COMMAND;
                    break;
                case SUGGEST_COMMAND:
                    action = ClickEvent.Action.SUGGEST_COMMAND;
                    break;
                case OPEN_URL:
                    action = ClickEvent.Action.OPEN_URL;
                    break;
                default:
                    action = ClickEvent.Action.RUN_COMMAND;
            }
            messageComponent.setClickEvent(new ClickEvent(action, processedClickCommand));
        }

        // Send to all online players
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            onlinePlayer.spigot().sendMessage(messageComponent);
        }
    }

    @Override
    public void sendCustomLeaveMessage(Player player, String message, List<String> hoverText, String clickCommand, ClickAction clickAction) {
        // Same implementation as sendCustomJoinMessage but for leave messages
        String processedMessage = ColorHandler.color(message.replace("<player>", player.getName()));
        processedMessage = PlaceholderAPI.setPlaceholders(player, processedMessage);

        List<String> processedHoverText = null;
        if (hoverText != null && !hoverText.isEmpty()) {
            processedHoverText = new ArrayList<>();
            for (String line : hoverText) {
                String processedLine = ColorHandler.color(line.replace("<player>", player.getName()));
                processedHoverText.add(PlaceholderAPI.setPlaceholders(player, processedLine));
            }
        }

        String processedClickCommand = null;
        if (clickCommand != null && !clickCommand.isEmpty()) {
            processedClickCommand = clickCommand.replace("<player>", player.getName());
            processedClickCommand = PlaceholderAPI.setPlaceholders(player, processedClickCommand);
        }

        TextComponent messageComponent = new TextComponent(processedMessage);

        if (processedHoverText != null) {
            String joinedHoverText = String.join("\n", processedHoverText);
            messageComponent.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new BaseComponent[]{new TextComponent(joinedHoverText)}
            ));
        }

        if (processedClickCommand != null && clickAction != null) {
            ClickEvent.Action action;
            switch (clickAction) {
                case RUN_COMMAND:
                    action = ClickEvent.Action.RUN_COMMAND;
                    break;
                case SUGGEST_COMMAND:
                    action = ClickEvent.Action.SUGGEST_COMMAND;
                    break;
                case OPEN_URL:
                    action = ClickEvent.Action.OPEN_URL;
                    break;
                default:
                    action = ClickEvent.Action.RUN_COMMAND;
            }
            messageComponent.setClickEvent(new ClickEvent(action, processedClickCommand));
        }

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            onlinePlayer.spigot().sendMessage(messageComponent);
        }
    }

    @Override
    public void setJoinMessageFormat(String format) {
        settings.set("JOIN.FORMAT", format);
    }

    @Override
    public void setLeaveMessageFormat(String format) {
        settings.set("LEAVE.FORMAT", format);
    }

    @Override
    public void setJoinHoverText(List<String> hoverText) {
        settings.set("JOIN.HOVER.TEXT", hoverText);
    }

    @Override
    public void setLeaveHoverText(List<String> hoverText) {
        settings.set("LEAVE.HOVER.TEXT", hoverText);
    }

    @Override
    public void setJoinClickCommand(String command, ClickAction action) {
        settings.set("JOIN.CLICK-COMMAND", command);
        settings.set("JOIN.CLICK-ACTION", action.name());
    }

    @Override
    public void setLeaveClickCommand(String command, ClickAction action) {
        settings.set("LEAVE.CLICK-COMMAND", command);
        settings.set("LEAVE.CLICK-ACTION", action.name());
    }

    @Override
    public void setJoinHoverEnabled(boolean enabled) {
        settings.set("HOVER-SUPPORT", enabled);
    }

    @Override
    public void setLeaveHoverEnabled(boolean enabled) {
        settings.set("HOVER-SUPPORT", enabled);
    }

    @Override
    public void setJoinClickEnabled(boolean enabled) {
        settings.set("CLICK-SUPPORT", enabled);
    }

    @Override
    public void setLeaveClickEnabled(boolean enabled) {
        settings.set("CLICK-SUPPORT", enabled);
    }

    @Override
    public void setJoinRequirePermission(boolean require) {
        settings.set("JOIN.REQUIRE-PERMISSION", require);
    }

    @Override
    public void setLeaveRequirePermission(boolean require) {
        settings.set("LEAVE.REQUIRE-PERMISSION", require);
    }

    @Override
    public void setJoinPermission(String permission) {
        plugin.getPermissionManager().set("JOIN-MESSAGE-PERMISSION", permission);
    }

    @Override
    public void setLeavePermission(String permission) {
        plugin.getPermissionManager().set("LEAVE-MESSAGE-PERMISSION", permission);
    }

    @Override
    public void reloadConfig() {
        settings.reload();
    }
}
