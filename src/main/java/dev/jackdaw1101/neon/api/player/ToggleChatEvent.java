package dev.jackdaw1101.neon.api.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ToggleChatEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private boolean newState;
    private boolean cancelled;

    public ToggleChatEvent(Player player, boolean newState) {
        this.player = player;
        this.newState = newState;
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

    public boolean getNewState() {
        return newState;
    }

    public void setNewState(boolean newState) {
        this.newState = newState;
    }
}
