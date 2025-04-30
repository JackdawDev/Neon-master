package dev.jackdaw1101.neon.manager.JoinLeave;

import dev.jackdaw1101.neon.api.player.NeonPlayerJoinEvent;
import dev.jackdaw1101.neon.api.player.NeonPlayerLeaveEvent;
import dev.jackdaw1101.neon.implementions.NeonJoinLeaveAPIImpl;
import dev.jackdaw1101.neon.utils.configs.ConfigFile;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.api.utilities.ColorHandler;
import me.clip.placeholderapi.PlaceholderAPI;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JoinLeaveListener implements Listener {

    private final Neon plugin;
    private final ConfigFile settings;
    private final NeonJoinLeaveAPIImpl api;


    public JoinLeaveListener(Neon plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();
        this.api = new NeonJoinLeaveAPIImpl(plugin);
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
                }, 1L * delayTicks);
            } else {
                processJoinEvent(event);
            }
        }

        if (pergroup) {
            if (delayEnabled) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getPerGroupChat().sendGroupJoin(event.getPlayer());
                }, 1L * delayTicks);
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

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage("");
        if (!settings.getBoolean("LEAVE.ENABLED")) return;

        boolean delayEnabled = settings.getBoolean("ASYNC.ENABLED");
        int delayTicks = settings.getInt("ASYNC.DELAY-TICKS");
        boolean normal = settings.getBoolean("NORMAL-LEAVE-MESSAGE");
        boolean pergroup = settings.getBoolean("PER-GROUP-LEAVE");

        if (normal) {
            if (delayEnabled) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    processLeaveEvent(event);
                }, 1L * delayTicks);
            } else {
                processLeaveEvent(event);
            }
        }

        if (pergroup) {
            if (delayEnabled) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getPerGroupLeave().sendGroupJoin(event.getPlayer());
                }, 1L * delayTicks);
            } else {
                plugin.getPerGroupLeave().sendGroupJoin(event.getPlayer());
            }
        }
    }

    private void processLeaveEvent(PlayerQuitEvent event) {
        String message = settings.getString("LEAVE.FORMAT");
        if (message == null) return;

        message = message.replace("<player>", event.getPlayer().getName());
        message = apply(ColorHandler.color(message), event.getPlayer());

        boolean requirePermission = settings.getBoolean("LEAVE.REQUIRE-PERMISSION");
        if (requirePermission && !event.getPlayer().hasPermission(plugin.getPermissionManager().getString("LEAVE-MESSAGE-PERMISSION"))) {
            return;
        }

        boolean isHoverEnabled = settings.getBoolean("HOVER-SUPPORT");
        boolean isClickEnabled = settings.getBoolean("CLICK-SUPPORT");
        String clickCommand = settings.getString("LEAVE.CLICK-COMMAND");
        List<String> hoverText = settings.getStringList("LEAVE.HOVER.TEXT");

        if (clickCommand != null) {
            clickCommand = clickCommand.replace("<player>", event.getPlayer().getName());
        }

        NeonPlayerLeaveEvent.ClickAction clickAction = NeonPlayerLeaveEvent.ClickAction.valueOf(
            settings.getString("LEAVE.CLICK-ACTION")
        );

        NeonPlayerLeaveEvent neonEvent = new NeonPlayerLeaveEvent(
            event.getPlayer(),
            message,
            isHoverEnabled,
            isClickEnabled,
            clickCommand,
            clickAction,
            hoverText.isEmpty() ? Arrays.asList(getDefaultHoverText(event.getPlayer(), false)) : hoverText
        );
    }

        public String apply(String text, Player target) {
        return PlaceholderAPI.setPlaceholders(target, text);
    }

    private void sendJoinLeaveMessage(Player player, String message, boolean isHoverEnabled,
                                      boolean isClickEnabled, String command,
                                      NeonPlayerJoinEvent.ClickAction clickAction, List<String> hoverText) {

        TextComponent messageComponent = new TextComponent(message);

        if (isHoverEnabled && hoverText != null && !hoverText.isEmpty()) {
            List<String> updatedHoverText = replacePlaceholdersInHoverText(hoverText, player);
            updatedHoverText = applyColorToHoverText(updatedHoverText);

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
