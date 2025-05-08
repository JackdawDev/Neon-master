package dev.jackdaw1101.neon.API.modules.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AntiLinkTriggerEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String originalMessage;
    private final String sanitizedMessage;
    private final String detectedLink;
    private final String cancelType;
    private boolean cancelled;
    private boolean alertAdmins;
    private boolean logToConsole;
    private boolean sendWebhook;
    private String warnSound;
    private String alertSound;
    private String warnMessage;
    private String alertMessage;

    public AntiLinkTriggerEvent(Player player, String originalMessage, String sanitizedMessage,
                                String detectedLink, String cancelType, boolean alertAdmins,
                                boolean logToConsole, boolean sendWebhook, String warnSound,
                                String alertSound, String warnMessage, String alertMessage) {
        this.player = player;
        this.originalMessage = originalMessage;
        this.sanitizedMessage = sanitizedMessage;
        this.detectedLink = detectedLink;
        this.cancelType = cancelType;
        this.alertAdmins = alertAdmins;
        this.logToConsole = logToConsole;
        this.sendWebhook = sendWebhook;
        this.warnSound = warnSound;
        this.alertSound = alertSound;
        this.warnMessage = warnMessage;
        this.alertMessage = alertMessage;
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

    public String getSanitizedMessage() {
        return sanitizedMessage;
    }

    public String getDetectedLink() {
        return detectedLink;
    }

    public String getCancelType() {
        return cancelType;
    }

    public boolean shouldAlertAdmins() {
        return alertAdmins;
    }

    public void setAlertAdmins(boolean alertAdmins) {
        this.alertAdmins = alertAdmins;
    }

    public boolean shouldLogToConsole() {
        return logToConsole;
    }

    public void setLogToConsole(boolean logToConsole) {
        this.logToConsole = logToConsole;
    }

    public boolean shouldSendWebhook() {
        return sendWebhook;
    }

    public void setSendWebhook(boolean sendWebhook) {
        this.sendWebhook = sendWebhook;
    }

    public String getWarnSound() {
        return warnSound;
    }

    public void setWarnSound(String warnSound) {
        this.warnSound = warnSound;
    }

    public String getAlertSound() {
        return alertSound;
    }

    public void setAlertSound(String alertSound) {
        this.alertSound = alertSound;
    }

    public String getWarnMessage() {
        return warnMessage;
    }

    public void setWarnMessage(String warnMessage) {
        this.warnMessage = warnMessage;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }
}
