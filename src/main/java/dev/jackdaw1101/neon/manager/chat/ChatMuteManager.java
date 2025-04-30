package dev.jackdaw1101.neon.manager.chat;

import dev.jackdaw1101.neon.Neon;

public class ChatMuteManager {
    private final Neon plugin;
    private boolean chatMuted = false;

    public ChatMuteManager(Neon plugin) {
        this.plugin = plugin;
    }

    public boolean isChatMuted() {
        return chatMuted;
    }

    public void setChatMuted(boolean muted) {
        this.chatMuted = muted;
    }
}
