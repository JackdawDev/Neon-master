package dev.jackdaw1101.neon.api.features.joinleave;

import org.bukkit.entity.Player;

import java.util.List;

public interface NeonJoinLeaveAPI {
    void sendCustomJoinMessage(Player player, String message, List<String> hoverText, String clickCommand, ClickAction clickAction);

    void sendCustomLeaveMessage(Player player, String message, List<String> hoverText, String clickCommand, ClickAction clickAction);

    void setJoinMessageFormat(String format);

    void setLeaveMessageFormat(String format);

    void setJoinHoverText(List<String> hoverText);

    void setLeaveHoverText(List<String> hoverText);

    void setJoinClickCommand(String command, ClickAction action);

    void setLeaveClickCommand(String command, ClickAction action);

    void setJoinHoverEnabled(boolean enabled);

    void setLeaveHoverEnabled(boolean enabled);

    void setJoinClickEnabled(boolean enabled);

    void setLeaveClickEnabled(boolean enabled);

    void setJoinRequirePermission(boolean require);

    void setLeaveRequirePermission(boolean require);

    void setJoinPermission(String permission);

    void setLeavePermission(String permission);

    void reloadConfig();

    enum ClickAction {
        RUN_COMMAND,
        SUGGEST_COMMAND,
        OPEN_URL
    }
}
