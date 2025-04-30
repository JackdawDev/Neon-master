package dev.jackdaw1101.neon.api.modules.events;

import org.bukkit.entity.Player;

public class SwearDetectEvent extends AntiSwearEvent {
    private final String detectedWord;
    private String censoredMessage;
    private boolean notifyAdmins;
    private boolean logToConsole;

    public SwearDetectEvent(Player player, String message, String detectedWord, String censoredMessage) {
        super(player, message);
        this.detectedWord = detectedWord;
        this.censoredMessage = censoredMessage;
        this.notifyAdmins = true;
        this.logToConsole = true;
    }

    public String getDetectedWord() {
        return detectedWord;
    }

    public String getCensoredMessage() {
        return censoredMessage;
    }

    public void setCensoredMessage(String censoredMessage) {
        this.censoredMessage = censoredMessage;
    }

    public boolean shouldNotifyAdmins() {
        return notifyAdmins;
    }

    public void setNotifyAdmins(boolean notifyAdmins) {
        this.notifyAdmins = notifyAdmins;
    }

    public boolean shouldLogToConsole() {
        return logToConsole;
    }

    public void setLogToConsole(boolean logToConsole) {
        this.logToConsole = logToConsole;
    }
}
