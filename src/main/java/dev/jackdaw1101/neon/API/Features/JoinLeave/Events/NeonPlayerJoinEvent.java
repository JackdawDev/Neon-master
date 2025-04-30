package dev.jackdaw1101.neon.api.features.joinleave.events;

import dev.jackdaw1101.neon.api.features.joinleave.NeonJoinLeaveAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NeonPlayerJoinEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    public static NeonPlayerJoinEvent.ClickAction ClickAction;
    private final Player player;
    private String joinMessage;
    private boolean cancelled;
    private boolean hoverEnabled;
    private boolean clickEnabled;
    private String clickCommand;
    private ClickAction clickAction;
    private List<String> hoverText;

    public NeonPlayerJoinEvent(Player player, String joinMessage, boolean hoverEnabled, boolean clickEnabled,
                               String clickCommand, ClickAction clickAction, List<String> hoverText) {
        this.player = player;
        this.joinMessage = joinMessage;
        this.hoverEnabled = hoverEnabled;
        this.clickEnabled = clickEnabled;
        this.clickCommand = clickCommand;
        this.clickAction = clickAction;
        this.hoverText = hoverText;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public Player getPlayer() {
        return player;
    }

    public String getJoinMessage() {
        return joinMessage;
    }

    public void setJoinMessage(String joinMessage) {
        this.joinMessage = joinMessage;
    }

    public boolean isHoverEnabled() {
        return hoverEnabled;
    }

    public void setHoverEnabled(boolean hoverEnabled) {
        this.hoverEnabled = hoverEnabled;
    }

    public boolean isClickEnabled() {
        return clickEnabled;
    }

    public void setClickEnabled(boolean clickEnabled) {
        this.clickEnabled = clickEnabled;
    }

    public String getClickCommand() {
        return clickCommand;
    }

    public void setClickCommand(String clickCommand) {
        this.clickCommand = clickCommand;
    }

    public ClickAction getClickAction() {
        return clickAction;
    }

    public void setClickAction(ClickAction clickAction) {
        this.clickAction = clickAction;
    }

    public List<String> getHoverText() {
        return hoverText;
    }

    public void setHoverText(List<String> hoverText) {
        this.hoverText = hoverText;
    }

    public enum ClickAction {
        RUN_COMMAND,
        SUGGEST_COMMAND,
        OPEN_URL
    }
}
