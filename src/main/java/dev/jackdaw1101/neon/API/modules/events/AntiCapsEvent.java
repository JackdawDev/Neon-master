package dev.jackdaw1101.neon.API.modules.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AntiCapsEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final String message;
    private final double capsPercentage;
    private final int upperChars;
    private final int lowerChars;
    private final int minLength;
    private final int requiredPercentage;
    private final boolean isCommand;
    private boolean cancelled;
    private boolean shouldCancel;
    private String warningMessage;
    private String sound;
    private boolean playSound;

    public AntiCapsEvent(Player player, String message, double capsPercentage,
                         int upperChars, int lowerChars, int minLength,
                         int requiredPercentage, boolean isCommand,
                         boolean shouldCancel, String warningMessage,
                         String sound, boolean playSound) {
        this.player = player;
        this.message = message;
        this.capsPercentage = capsPercentage;
        this.upperChars = upperChars;
        this.lowerChars = lowerChars;
        this.minLength = minLength;
        this.requiredPercentage = requiredPercentage;
        this.isCommand = isCommand;
        this.shouldCancel = shouldCancel;
        this.warningMessage = warningMessage;
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

    public double getCapsPercentage() {
        return capsPercentage;
    }

    public int getUpperChars() {
        return upperChars;
    }

    public int getLowerChars() {
        return lowerChars;
    }

    public int getMinLength() {
        return minLength;
    }

    public int getRequiredPercentage() {
        return requiredPercentage;
    }

    public boolean isCommand() {
        return isCommand;
    }

    public boolean shouldCancel() {
        return shouldCancel;
    }

    public void setShouldCancel(boolean shouldCancel) {
        this.shouldCancel = shouldCancel;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
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
