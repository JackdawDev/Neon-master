package dev.jackdaw1101.neon.Manager.JoinLeave;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Configurations.Settings;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JoinLeaveListener implements Listener {

    private final Neon plugin;
    private final Settings settings;

    public JoinLeaveListener(Neon plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage("");
        if (!(boolean) settings.getValue("JOIN.ENABLED")) return;

        boolean delayEnabled = (boolean) settings.getValue("ASYNC.ENABLED", false);
        int delayTicks = (int) settings.getValue("ASYNC.DELAY-TICKS", 0);

        processJoinEvent(event);
    }

    private void processJoinEvent(PlayerJoinEvent event) {
        String message = (String) settings.getValue("JOIN.FORMAT");
        if (message != null) {
            message = message.replace("<player>", event.getPlayer().getName());
            message = apply(ColorHandler.color(message), event.getPlayer());

            boolean requirePermission = (boolean) settings.getValue("JOIN.REQUIRE-PERMISSION", false);
            if (requirePermission && !event.getPlayer().hasPermission(plugin.getPermissionManager().getPermission("JOIN-MESSAGE-PERMISSION"))) {
                return;
            }

            sendJoinLeaveMessage(event.getPlayer(), ColorHandler.color(message), true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage("");
        if (!(boolean) settings.getValue("LEAVE.ENABLED")) return;

        boolean delayEnabled = (boolean) settings.getValue("ASYNC.ENABLED", false);
        int delayTicks = (int) settings.getValue("ASYNC.DELAY-TICKS", 15);
        processLeaveEvent(event);
    }

    private void processLeaveEvent(PlayerQuitEvent event) {
        String message = (String) settings.getValue("LEAVE.FORMAT");
        if (message != null) {
            message = message.replace("<player>", event.getPlayer().getName());
            message = apply(ColorHandler.color(message), event.getPlayer());

            boolean requirePermission = (boolean) settings.getValue("LEAVE.REQUIRE-PERMISSION", false);
            if (requirePermission && !event.getPlayer().hasPermission(plugin.getPermissionManager().getPermission("LEAVE-MESSAGE-PERMISSION"))) {
                return;
            }

            sendJoinLeaveMessage(event.getPlayer(), ColorHandler.color(message), false);
        }
    }

    public String apply(String text, Player target) {
        return PlaceholderAPI.setPlaceholders(target, text);
    }

    private void sendJoinLeaveMessage(Player player, String message, boolean isJoin) {
        boolean isHoverEnabled = (boolean) settings.getValue("HOVER-SUPPORT", true);
        boolean isClickEnabled = (boolean) settings.getValue("CLICK-SUPPORT", true);
        boolean isRunCommand = (boolean) settings.getValue("JOIN.RUN-COMMAND", true);
        boolean isSuggestCommand = (boolean) settings.getValue("LEAVE.SUGGEST-COMMAND", true);

        String commandKey = isJoin ? "JOIN.CLICK-COMMAND" : "LEAVE.CLICK-COMMAND";
        String command = (String) settings.getValue(commandKey);
        if (command != null) {
            command = command.replace("<player>", player.getName());
        }

        List<String> hoverTextList = getHoverText(player, isJoin);
        if (hoverTextList == null || hoverTextList.isEmpty()) {
            hoverTextList = Arrays.asList(getDefaultHoverText(player, isJoin));
        }

        hoverTextList = replacePlaceholdersInHoverText(hoverTextList, player);
        hoverTextList = applyColorToHoverText(hoverTextList);

        TextComponent messageComponent = new TextComponent(message);

        if (isHoverEnabled) {
            String joinedHoverText = String.join("\n", hoverTextList);
            messageComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(joinedHoverText)}));
        }

        if (isClickEnabled && command != null) {
            if (isSuggestCommand) {
                messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
            } else if (isRunCommand) {
                messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
            }
        }

        for (Player viewer : plugin.getServer().getOnlinePlayers()) {
            viewer.spigot().sendMessage(messageComponent);
        }
    }

    private List<String> getHoverText(Player player, boolean isJoin) {
        String path = isJoin ? "JOIN.HOVER.TEXT" : "LEAVE.HOVER.TEXT";
        return (List<String>) settings.getValue(path);
    }

    private String[] getDefaultHoverText(Player player, boolean isJoin) {
        return new String[]{isJoin ? "Welcome to the server, " + player.getName() : "Goodbye, " + player.getName()};
    }

    private List<String> replacePlaceholdersInHoverText(List<String> hoverTextList, Player target) {
        List<String> updatedHoverText = new ArrayList<>();
        for (String line : hoverTextList) {
            updatedHoverText.add(apply(line, target));
        }
        return updatedHoverText;
    }

    private List<String> applyColorToHoverText(List<String> hoverTextList) {
        List<String> coloredHoverText = new ArrayList<>();
        for (String line : hoverTextList) {
            coloredHoverText.add(ColorHandler.color(line));
        }
        return coloredHoverText;
    }
}
