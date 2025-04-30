package dev.jackdaw1101.neon.api.modules.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AntiUnicodeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String message;
    private final String detectedUnicode;
    private boolean cancelled;
    private boolean shouldKick;
    private String blockMessage;
    private String kickMessage;
    private String sound;
    private boolean playSound;

    public AntiUnicodeEvent(Player player, String message, String detectedUnicode,
                            boolean shouldKick, String blockMessage, String kickMessage,
                            String sound, boolean playSound) {
        this.player = player;
        this.message = message;
        this.detectedUnicode = detectedUnicode;
        this.shouldKick = shouldKick;
        this.blockMessage = blockMessage;
        this.kickMessage = kickMessage;
        this.sound = sound;
        this.playSound = playSound;
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

    public String getMessage() {
        return message;
    }

    public String getDetectedUnicode() {
        return detectedUnicode;
    }

    public boolean shouldKick() {
        return shouldKick;
    }

    public void setShouldKick(boolean shouldKick) {
        this.shouldKick = shouldKick;
    }

    public String getBlockMessage() {
        return blockMessage;
    }

    public void setBlockMessage(String blockMessage) {
        this.blockMessage = blockMessage;
    }

    public String getKickMessage() {
        return kickMessage;
    }

    public void setKickMessage(String kickMessage) {
        this.kickMessage = kickMessage;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public boolean shouldPlaySound() {
        return playSound;
    }

    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }
}
