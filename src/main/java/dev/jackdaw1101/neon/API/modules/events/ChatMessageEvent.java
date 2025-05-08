package dev.jackdaw1101.neon.API.modules.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChatMessageEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player sender;
    private String message;
    private String hoverText;
    private String clickCommand;
    private boolean isCancelled;

    public ChatMessageEvent(Player sender, String message, String hoverText, String clickCommand) {
        this.sender = sender;
        this.message = message;
        this.hoverText = hoverText;
        this.clickCommand = clickCommand;
        this.isCancelled = false;
    }

    public Player getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getHoverText() {
        return hoverText;
    }

    public void setHoverText(String hoverText) {
        this.hoverText = hoverText;
    }

    public String getClickCommand() {
        return clickCommand;
    }

    public void setClickCommand(String clickCommand) {
        this.clickCommand = clickCommand;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

