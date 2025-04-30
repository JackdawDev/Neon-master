package dev.jackdaw1101.neon.api.grammer.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GrammarCheckEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private String originalMessage;
    private String correctedMessage;
    private boolean autoCorrectEnabled;
    private boolean punctuationCheckEnabled;
    private boolean capitalizationEnabled;
    private boolean cancelled;

    public GrammarCheckEvent(Player player, String originalMessage, String correctedMessage,
                             boolean autoCorrectEnabled, boolean punctuationCheckEnabled,
                             boolean capitalizationEnabled) {
        this.player = player;
        this.originalMessage = originalMessage;
        this.correctedMessage = correctedMessage;
        this.autoCorrectEnabled = autoCorrectEnabled;
        this.punctuationCheckEnabled = punctuationCheckEnabled;
        this.capitalizationEnabled = capitalizationEnabled;
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

    public String getOriginalMessage() {
        return originalMessage;
    }

    public String getCorrectedMessage() {
        return correctedMessage;
    }

    public void setCorrectedMessage(String correctedMessage) {
        this.correctedMessage = correctedMessage;
    }

    public boolean isAutoCorrectEnabled() {
        return autoCorrectEnabled;
    }

    public void setAutoCorrectEnabled(boolean autoCorrectEnabled) {
        this.autoCorrectEnabled = autoCorrectEnabled;
    }

    public boolean isPunctuationCheckEnabled() {
        return punctuationCheckEnabled;
    }

    public void setPunctuationCheckEnabled(boolean punctuationCheckEnabled) {
        this.punctuationCheckEnabled = punctuationCheckEnabled;
    }

    public boolean isCapitalizationEnabled() {
        return capitalizationEnabled;
    }

    public void setCapitalizationEnabled(boolean capitalizationEnabled) {
        this.capitalizationEnabled = capitalizationEnabled;
    }
}
