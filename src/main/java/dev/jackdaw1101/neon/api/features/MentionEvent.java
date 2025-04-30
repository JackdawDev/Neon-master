package dev.jackdaw1101.neon.API.Features;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MentionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player sender;
    private final Player mentioned;
    private final String message;
    private final boolean isMentionedBySymbol;
    private final String mentionSymbol;
    private final boolean isEveryoneMention;
    private final boolean isCooldownActive;

    public MentionEvent(Player sender, Player mentioned, String message, boolean isMentionedBySymbol, String mentionSymbol, boolean isEveryoneMention, boolean isCooldownActive) {
        this.sender = sender;
        this.mentioned = mentioned;
        this.message = message;
        this.isMentionedBySymbol = isMentionedBySymbol;
        this.mentionSymbol = mentionSymbol;
        this.isEveryoneMention = isEveryoneMention;
        this.isCooldownActive = isCooldownActive;
    }

    public Player getSender() {
        return sender;
    }

    public Player getMentioned() {
        return mentioned;
    }

    public String getMessage() {
        return message;
    }

    public boolean isMentionedBySymbol() {
        return isMentionedBySymbol;
    }

    public String getMentionSymbol() {
        return mentionSymbol;
    }

    public boolean isEveryoneMention() {
        return isEveryoneMention;
    }

    public boolean isCooldownActive() {
        return isCooldownActive;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
