package dev.jackdaw1101.neon.API.modules.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class AiSwearDetectEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String message;
    private final List<String> categories;

    public AiSwearDetectEvent(Player player, String message, List<String> categories) {
        this.player = player;
        this.message = message;
        this.categories = categories;
    }

    public Player getPlayer() { return player; }
    public String getMessage() { return message; }
    public List<String> getCategories() { return categories; }

    public static HandlerList getHandlerList() { return handlers; }
    @Override public HandlerList getHandlers() { return handlers; }
}
