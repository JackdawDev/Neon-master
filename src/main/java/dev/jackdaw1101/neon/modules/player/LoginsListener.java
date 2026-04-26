package dev.jackdaw1101.neon.modules.player;

import dev.jackdaw1101.neon.API.player.NeonPlayerJoinEvent;
import dev.jackdaw1101.neon.API.player.NeonPlayerLeaveEvent;
import dev.jackdaw1101.neon.utils.DebugUtil;
import dev.jackdaw1101.neon.utils.configs.ConfigFile;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoginsListener implements Listener {

    private final Neon plugin;
    private final ConfigFile settings;

    public LoginsListener(Neon plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage("");
        if (!settings.getBoolean("JOIN.ENABLED")) return;

        boolean delayEnabled = settings.getBoolean("ASYNC.ENABLED");
        int delayTicks = settings.getInt("ASYNC.DELAY-TICKS");
        boolean normal = settings.getBoolean("NORMAL-JOIN-MESSAGE");
        boolean pergroup = settings.getBoolean("PER-GROUP-JOIN");

        if (normal) {
            if (delayEnabled) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    processJoinEvent(event);
                }, delayTicks); 
            } else {
                processJoinEvent(event);
                DebugUtil.debugChecked("&7"+event.getPlayer().getName()+ " logged in using Neon Join Event!");
            }
        }

        if (pergroup) {
            if (delayEnabled) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getPerGroupChat().sendGroupJoin(event.getPlayer());
                }, delayTicks); 
            } else {
                plugin.getPerGroupChat().sendGroupJoin(event.getPlayer());
            }
        }
    }

    private void processJoinEvent(PlayerJoinEvent event) {
        String message = settings.getString("JOIN.FORMAT");
        if (message == null) return;

        message = message.replace("<player>", event.getPlayer().getName());
        message = apply(ColorHandler.color(message), event.getPlayer());

        boolean requirePermission = settings.getBoolean("JOIN.REQUIRE-PERMISSION");
        if (requirePermission && !event.getPlayer().hasPermission(plugin.getPermissionManager().getString("JOIN-MESSAGE-PERMISSION"))) {
            return;
        }

        boolean isHoverEnabled = settings.getBoolean("HOVER-SUPPORT");
        boolean isClickEnabled = settings.getBoolean("CLICK-SUPPORT");
        String clickCommand = settings.getString("JOIN.CLICK-COMMAND");
        String clickActionString = settings.getString("JOIN.CLICK-ACTION");
        NeonPlayerJoinEvent.ClickAction clickAction;

        if (clickActionString != null) {
            try {
                clickAction = NeonPlayerJoinEvent.ClickAction.valueOf(clickActionString);
            } catch (IllegalArgumentException e) {
                clickAction = NeonPlayerJoinEvent.ClickAction.RUN_COMMAND;
            }
        } else {
            clickAction = NeonPlayerJoinEvent.ClickAction.RUN_COMMAND;
        }

        List<String> hoverText = settings.getStringList("JOIN.HOVER.TEXT");

        if (clickCommand != null) {
            clickCommand = clickCommand.replace("<player>", event.getPlayer().getName());
        }

        NeonPlayerJoinEvent neonEvent = new NeonPlayerJoinEvent(
                event.getPlayer(),
                message,
                isHoverEnabled,
                isClickEnabled,
                clickCommand,
                clickAction,
                hoverText.isEmpty() ? Arrays.asList(getDefaultHoverText(event.getPlayer(), true)) : hoverText
        );

        Bukkit.getPluginManager().callEvent(neonEvent);

        if (neonEvent.isCancelled()) return;

        sendJoinLeaveMessage(
                event.getPlayer(),
                neonEvent.getJoinMessage(),
                neonEvent.isHoverEnabled(),
                neonEvent.isClickEnabled(),
                neonEvent.getClickCommand(),
                neonEvent.getClickAction(),
                neonEvent.getHoverText()
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST) 
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage("");
        if (!settings.getBoolean("LEAVE.ENABLED")) return;

        boolean delayEnabled = settings.getBoolean("ASYNC.ENABLED");
        int delayTicks = settings.getInt("ASYNC.DELAY-TICKS");
        boolean normal = settings.getBoolean("NORMAL-LEAVE-MESSAGE");
        boolean pergroup = settings.getBoolean("PER-GROUP-LEAVE");

        Player player = event.getPlayer();
        String playerName = player.getName();

        if (normal) {
            if (delayEnabled) {

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    processLeaveEvent(event, player, playerName);
                }, Math.max(1, delayTicks));
            } else {
                processLeaveEvent(event, player, playerName);
                DebugUtil.debugChecked("&7"+event.getPlayer().getName()+ " logged in using Neon Join Event!");
            }
        }

        if (pergroup) {
            if (delayEnabled) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getPerGroupLeave().sendGroupJoin(event.getPlayer());
                }, delayTicks);
            } else {
                plugin.getPerGroupLeave().sendGroupJoin(event.getPlayer());
            }
        }
    }

    private void processLeaveEvent(PlayerQuitEvent event, Player player, String playerName) {
        String message = settings.getString("LEAVE.FORMAT");
        if (message == null) return;

        message = message.replace("<player>", playerName);

        if (player != null && player.isOnline()) {
            message = apply(ColorHandler.color(message), player);
        } else {
            message = ColorHandler.color(message);
        }

        boolean requirePermission = settings.getBoolean("LEAVE.REQUIRE-PERMISSION");
        if (requirePermission && player != null && !player.hasPermission(plugin.getPermissionManager().getString("LEAVE-MESSAGE-PERMISSION"))) {
            return;
        }

        boolean isHoverEnabled = settings.getBoolean("HOVER-SUPPORT");
        boolean isClickEnabled = settings.getBoolean("CLICK-SUPPORT");
        String clickCommand = settings.getString("LEAVE.CLICK-COMMAND");
        List<String> hoverText = settings.getStringList("LEAVE.HOVER.TEXT");

        if (clickCommand != null) {
            clickCommand = clickCommand.replace("<player>", playerName);
        }

        NeonPlayerLeaveEvent.ClickAction clickAction;
        try {
            clickAction = NeonPlayerLeaveEvent.ClickAction.valueOf(
                    settings.getString("LEAVE.CLICK-ACTION", "RUN_COMMAND")
            );
        } catch (IllegalArgumentException e) {
            clickAction = NeonPlayerLeaveEvent.ClickAction.RUN_COMMAND;
        }

        NeonPlayerLeaveEvent neonEvent = new NeonPlayerLeaveEvent(
                player,
                message,
                isHoverEnabled,
                isClickEnabled,
                clickCommand,
                clickAction,
                hoverText.isEmpty() ? Arrays.asList(getDefaultHoverText(player, false)) : hoverText
        );

        Bukkit.getPluginManager().callEvent(neonEvent);

        if (neonEvent.isCancelled()) return;

        for (Player viewer : plugin.getServer().getOnlinePlayers()) {
            if (viewer.equals(player)) continue;
            sendLeaveMessageToPlayer(
                    viewer,
                    neonEvent.getLeaveMessage(),
                    neonEvent.isHoverEnabled(),
                    neonEvent.isClickEnabled(),
                    neonEvent.getClickCommand(),
                    neonEvent.getClickAction(),
                    neonEvent.getHoverText(),
                    player 
            );
        }
    }

    private void sendLeaveMessageToPlayer(Player viewer, String message, boolean isHoverEnabled,
                                          boolean isClickEnabled, String command,
                                          NeonPlayerLeaveEvent.ClickAction clickAction,
                                          List<String> hoverText, Player leavingPlayer) {

        if (leavingPlayer != null) {
            message = apply(message, leavingPlayer);
            message = ColorHandler.color(message);
        }

        TextComponent messageComponent = new TextComponent(message);

        if (isHoverEnabled && hoverText != null && !hoverText.isEmpty()) {
            hoverText = Collections.singletonList(ColorHandler.color(String.valueOf(hoverText)));
            List<String> updatedHoverText = new ArrayList<>();
            for (String line : hoverText) {
                if (leavingPlayer != null) {
                    line = apply(line, leavingPlayer);
                }
                updatedHoverText.add(ColorHandler.color(line));
            }

            String joinedHoverText = String.join("\n", updatedHoverText);
            joinedHoverText = handleLuckPermsPrefixSuffix(leavingPlayer, joinedHoverText);
            messageComponent.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new BaseComponent[]{new TextComponent(joinedHoverText)}
            ));
        }

        if (isClickEnabled && command != null) {
            ClickEvent.Action action;
            switch (clickAction != null ? clickAction.name() : "RUN_COMMAND") {
                case "RUN_COMMAND":
                    action = ClickEvent.Action.RUN_COMMAND;
                    break;
                case "SUGGEST_COMMAND":
                    action = ClickEvent.Action.SUGGEST_COMMAND;
                    break;
                case "OPEN_URL":
                    action = ClickEvent.Action.OPEN_URL;
                    break;
                default:
                    action = ClickEvent.Action.RUN_COMMAND;
            }
            messageComponent.setClickEvent(new ClickEvent(action, command));
        }

        viewer.spigot().sendMessage(messageComponent);
    }

    public String apply(String text, Player target) {
        if (target == null || !target.isOnline()) return ColorHandler.color(text);
        try {
            return PlaceholderAPI.setPlaceholders(target, text);
        } catch (Exception e) {
            return ColorHandler.color(text);
        }
    }

    private void sendJoinLeaveMessage(Player player, String message, boolean isHoverEnabled,
                                      boolean isClickEnabled, String command,
                                      NeonPlayerJoinEvent.ClickAction clickAction, List<String> hoverText) {

        TextComponent messageComponent = new TextComponent(message);

        if (isHoverEnabled && hoverText != null && !hoverText.isEmpty()) {
            hoverText = Collections.singletonList(ColorHandler.color(String.valueOf(hoverText)));
            List<String> updatedHoverText = replacePlaceholdersInHoverText(hoverText, player);
            updatedHoverText = applyColorToHoverText(updatedHoverText);
            updatedHoverText = Collections.singletonList(handleLuckPermsPrefixSuffix(player, String.valueOf(updatedHoverText)));

            String joinedHoverText = String.join("\n", updatedHoverText);
            messageComponent.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new BaseComponent[]{new TextComponent(joinedHoverText)}
            ));
        }

        if (isClickEnabled && command != null) {
            ClickEvent.Action action;
            switch (clickAction.name()) {
                case "RUN_COMMAND":
                    action = ClickEvent.Action.RUN_COMMAND;
                    break;
                case "SUGGEST_COMMAND":
                    action = ClickEvent.Action.SUGGEST_COMMAND;
                    break;
                case "OPEN_URL":
                    action = ClickEvent.Action.OPEN_URL;
                    break;
                default:
                    action = ClickEvent.Action.RUN_COMMAND;
            }
            messageComponent.setClickEvent(new ClickEvent(action, command));
        }

        for (Player viewer : plugin.getServer().getOnlinePlayers()) {
            viewer.spigot().sendMessage(messageComponent);
        }
    }

    private String[] getDefaultHoverText(Player player, boolean isJoin) {
        if (player == null) {
            return new String[]{isJoin ? "Welcome to the server!" : "Goodbye!"};
        }
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