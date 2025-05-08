package dev.jackdaw1101.neon.API.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.List;

public class WelcomeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private List<String> messageLines;
    private boolean isCancelled;
    private String sound;
    private List<String> hoverMessages;
    private BaseComponent[] hoverComponents;
    private String clickCommand;
    private String openUrl;
    private boolean openUrlEnabled;
    private boolean clickCommandEnabled;
    private boolean suggestCommandEnabled;
    private String suggestCommand;

    public WelcomeEvent(Player player, List<String> messageLines) {
        this.player = player;
        this.messageLines = messageLines;
        this.isCancelled = false;
        this.sound = "";
        this.hoverMessages = null;
        this.hoverComponents = null;
        this.clickCommand = "";
        this.openUrl = "";
        this.openUrlEnabled = false;
        this.clickCommandEnabled = false;
        this.suggestCommandEnabled = false;
        this.suggestCommand = "";
    }

    public Player getPlayer() {
        return player;
    }

    public List<String> getMessageLines() {
        return messageLines;
    }

    public void setMessageLines(List<String> messageLines) {
        this.messageLines = messageLines;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public List<String> getHoverMessages() {
        return hoverMessages;
    }

    public void setHoverMessages(List<String> hoverMessages) {
        this.hoverMessages = hoverMessages;
    }

    public BaseComponent[] getHoverComponents() {
        return hoverComponents;
    }

    public void setHoverComponents(BaseComponent[] hoverComponents) {
        this.hoverComponents = hoverComponents;
    }

    public String getClickCommand() {
        return clickCommand;
    }

    public void setClickCommand(String clickCommand) {
        this.clickCommand = clickCommand;
    }

    public String getOpenUrl() {
        return openUrl;
    }

    public void setOpenUrl(String openUrl) {
        this.openUrl = openUrl;
    }

    public boolean isOpenUrlEnabled() {
        return openUrlEnabled;
    }

    public void setOpenUrlEnabled(boolean openUrlEnabled) {
        this.openUrlEnabled = openUrlEnabled;
    }

    public boolean isClickCommandEnabled() {
        return clickCommandEnabled;
    }

    public void setClickCommandEnabled(boolean clickCommandEnabled) {
        this.clickCommandEnabled = clickCommandEnabled;
    }

    public boolean isSuggestCommandEnabled() {
        return suggestCommandEnabled;
    }

    public void setSuggestCommandEnabled(boolean suggestCommandEnabled) {
        this.suggestCommandEnabled = suggestCommandEnabled;
    }

    public String getSuggestCommand() {
        return suggestCommand;
    }

    public void setSuggestCommand(String suggestCommand) {
        this.suggestCommand = suggestCommand;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}

