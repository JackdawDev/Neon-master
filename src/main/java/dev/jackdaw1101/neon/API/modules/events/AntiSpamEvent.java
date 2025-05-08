package dev.jackdaw1101.neon.API.modules.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AntiSpamEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final String message;
    private final String previousMessage;
    private final double similarityPercentage;
    private final SpamType spamType;
    private boolean cancelled;
    private boolean shouldCancel;
    private String warningMessage;
    private String sound;
    private boolean playSound;
    private boolean shouldKick;
    private String kickMessage;

    public AntiSpamEvent(Player player, String message, String previousMessage,
                         double similarityPercentage, SpamType spamType,
                         boolean shouldCancel, String warningMessage,
                         String sound, boolean playSound,
                         boolean shouldKick, String kickMessage) {
        this.player = player;
        this.message = message;
        this.previousMessage = previousMessage;
        this.similarityPercentage = similarityPercentage;
        this.spamType = spamType;
        this.shouldCancel = shouldCancel;
        this.warningMessage = warningMessage;
        this.sound = sound;
        this.playSound = playSound;
        this.shouldKick = shouldKick;
        this.kickMessage = kickMessage;
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

    public String getPreviousMessage() {
        return previousMessage;
    }

    public double getSimilarityPercentage() {
        return similarityPercentage;
    }

    public SpamType getSpamType() {
        return spamType;
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

    public boolean shouldKick() {
        return shouldKick;
    }

    public void setShouldKick(boolean shouldKick) {
        this.shouldKick = shouldKick;
    }

    public String getKickMessage() {
        return kickMessage;
    }

    public void setKickMessage(String kickMessage) {
        this.kickMessage = kickMessage;
    }

    public enum SpamType {
        SIMILAR_MESSAGE,
        REPETITIVE_MESSAGE,
        REPETITIVE_CHARACTERS,
        COMMAND_SPAM,
        COMMAND_REPETITIVE,
        COMMAND_REPETITIVE_CHARACTERS,
        CHAT_DELAY,
        COMMAND_DELAY
    }
}
