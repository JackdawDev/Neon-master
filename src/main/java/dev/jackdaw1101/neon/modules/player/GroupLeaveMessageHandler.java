package dev.jackdaw1101.neon.modules.player;

import dev.jackdaw1101.neon.API.player.NeonPlayerLeaveEvent;
import dev.jackdaw1101.neon.API.modules.moderation.NeonJoinLeaveAPI;
import dev.jackdaw1101.neon.Neon;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.md_5.bungee.api.ChatColor.translateAlternateColorCodes;

public class GroupLeaveMessageHandler {

    private final Map<String, GroupJoinMessage> groupMessages = new HashMap<>();

    public GroupLeaveMessageHandler() {
        loadGroupMessages();
    }

    private void loadGroupMessages() {
        groupMessages.clear();

        ConfigurationSection groupsSection = Neon.getInstance().getLocales().getConfig().getConfigurationSection("LEAVE-GROUPS");
        if (groupsSection == null) return;

        for (String key : groupsSection.getKeys(false)) {
            ConfigurationSection section = groupsSection.getConfigurationSection(key);
            if (section == null) continue;

            String permission = section.getString("PERMISSION");
            int priority = section.getInt("PRIORITY", 0);
            String format = section.getString("FORMAT", "&f%player_name%");
            boolean hoverEnabled = section.getBoolean("HOVER-ENABLED", false);
            List<String> hoverText = section.getStringList("HOVER");
            boolean clickEnabled = section.getBoolean("CLICK-EVENT", false);
            boolean suggestCommand = section.getBoolean("SUGGEST-COMMAND", false);
            boolean runCommand = section.getBoolean("RUN-COMMAND", false);
            String clickCommand = section.getString("CLICK-COMMAND", "");

            groupMessages.put(key, new GroupJoinMessage(
                permission,
                priority,
                format,
                hoverEnabled,
                hoverText,
                clickEnabled,
                suggestCommand,
                runCommand,
                clickCommand
            ));
        }
    }

    public void sendGroupJoin(Player player) {
        GroupJoinMessage bestGroup = null;

        for (GroupJoinMessage group : groupMessages.values()) {
            if (group.getPermission() == null || player.hasPermission(group.getPermission())) {
                if (bestGroup == null || group.getPriority() > bestGroup.getPriority()) {
                    bestGroup = group;
                }
            }
        }

        if (bestGroup == null) {
            return;
        }

        String message = translateAlternateColorCodes('&', bestGroup.getFormat().replace("%player_name%", player.getName()));
        boolean hoverEnabled = bestGroup.isHoverEnabled();
        boolean clickEnabled = bestGroup.isClickEnabled();
        List<String> hover = null;
        if (hoverEnabled && bestGroup.getHoverText() != null) {
            hover = new ArrayList<>();
            for (String line : bestGroup.getHoverText()) {
                hover.add(translateAlternateColorCodes('&', line));
            }
        }

        String clickCommand = null;
        NeonPlayerLeaveEvent.ClickAction clickAction = null;

        if (clickEnabled) {
            clickCommand = bestGroup.getClickCommand().replace("%player_name%", player.getName());
            if (bestGroup.isSuggestCommand()) {
                clickAction = NeonPlayerLeaveEvent.ClickAction.SUGGEST_COMMAND;
            } else if (bestGroup.isRunCommand()) {
                clickAction = NeonPlayerLeaveEvent.ClickAction.RUN_COMMAND;
            } else {
                clickAction = NeonPlayerLeaveEvent.ClickAction.OPEN_URL;
            }
        }

        NeonPlayerLeaveEvent event = new NeonPlayerLeaveEvent(
            player,
            message,
            hoverEnabled,
            clickEnabled,
            clickCommand,
            clickAction,
            hover
        );
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        sendJoinMessage(player, event);
    }

    private void sendJoinMessage(Player player, NeonPlayerLeaveEvent event) {
        if (event.isCancelled()) {
            return;
        }

        NeonJoinLeaveAPI neonJoinLeaveAPI = Bukkit.getServicesManager().load(NeonJoinLeaveAPI.class);
        if (neonJoinLeaveAPI == null) {
            Bukkit.getLogger().warning("[Neon] NeonJoinLeaveAPI not found! Cannot send join message for " + player.getName());
            return;
        }

        String joinMessage = event.getLeaveMessage();
        List<String> hoverText = event.isHoverEnabled() ? event.getHoverText() : null;
        String clickCommand = event.isClickEnabled() ? event.getClickCommand() : null;
        NeonJoinLeaveAPI.ClickAction clickAction = event.isClickEnabled() ? convertClickAction(event.getClickAction()) : NeonJoinLeaveAPI.ClickAction.RUN_COMMAND;

        neonJoinLeaveAPI.sendCustomJoinMessage(
            player,
            joinMessage,
            hoverText,
            clickCommand,
            clickAction
        );
    }


    private NeonJoinLeaveAPI.ClickAction convertClickAction(NeonPlayerLeaveEvent.ClickAction clickAction) {
        if (clickAction == null) {
            return NeonJoinLeaveAPI.ClickAction.RUN_COMMAND;
        }
        switch (clickAction) {
            case RUN_COMMAND:
                return NeonJoinLeaveAPI.ClickAction.RUN_COMMAND;
            case SUGGEST_COMMAND:
                return NeonJoinLeaveAPI.ClickAction.SUGGEST_COMMAND;
            case OPEN_URL:
                return NeonJoinLeaveAPI.ClickAction.OPEN_URL;
            default:
                return NeonJoinLeaveAPI.ClickAction.RUN_COMMAND;
        }
    }

    public void reload() {
        loadGroupMessages();
    }

    private static class GroupJoinMessage {
        private final String permission;
        private final int priority;
        private final String format;
        private final boolean hoverEnabled;
        private final List<String> hoverText;
        private final boolean clickEnabled;
        private final boolean suggestCommand;
        private final boolean runCommand;
        private final String clickCommand;

        public GroupJoinMessage(String permission, int priority, String format, boolean hoverEnabled, List<String> hoverText,
                                boolean clickEnabled, boolean suggestCommand, boolean runCommand, String clickCommand) {
            this.permission = permission;
            this.priority = priority;
            this.format = format;
            this.hoverEnabled = hoverEnabled;
            this.hoverText = hoverText;
            this.clickEnabled = clickEnabled;
            this.suggestCommand = suggestCommand;
            this.runCommand = runCommand;
            this.clickCommand = clickCommand;
        }

        public String getPermission() {
            return permission;
        }

        public int getPriority() {
            return priority;
        }

        public String getFormat() {
            return format;
        }

        public boolean isHoverEnabled() {
            return hoverEnabled;
        }

        public List<String> getHoverText() {
            return hoverText;
        }

        public boolean isClickEnabled() {
            return clickEnabled;
        }

        public boolean isSuggestCommand() {
            return suggestCommand;
        }

        public boolean isRunCommand() {
            return runCommand;
        }

        public String getClickCommand() {
            return clickCommand;
        }
    }
}
