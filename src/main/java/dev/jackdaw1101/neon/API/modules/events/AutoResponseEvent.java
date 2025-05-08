package dev.jackdaw1101.neon.API.modules.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AutoResponseEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String triggerWord;
    private List<String> responses;
    private List<String> hoverText;
    private String sound;
    private boolean cancelled;
    private boolean playSound;
    private boolean useHover;

    public AutoResponseEvent(Player player, String triggerWord, List<String> responses,
                             List<String> hoverText, String sound, boolean playSound,
                             boolean useHover) {
        this.player = player;
        this.triggerWord = triggerWord;
        this.responses = responses;
        this.hoverText = hoverText;
        this.sound = sound;
        this.playSound = playSound;
        this.useHover = useHover;
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

    public String getTriggerWord() {
        return triggerWord;
    }

    public List<String> getResponses() {
        return responses;
    }

    public void setResponses(List<String> responses) {
        this.responses = responses;
    }

    public List<String> getHoverText() {
        return hoverText;
    }

    public void setHoverText(List<String> hoverText) {
        this.hoverText = hoverText;
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

    public boolean shouldUseHover() {
        return useHover;
    }

    public void setUseHover(boolean useHover) {
        this.useHover = useHover;
    }
}
