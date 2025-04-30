package dev.jackdaw1101.neon.api.features.chatmute;

import dev.jackdaw1101.neon.Neon;
import lombok.Getter;
import lombok.Setter;

public class ChatMuteManager {
    private final Neon plugin;
    @Setter
    @Getter
    private boolean chatMuted = false;

    public ChatMuteManager(Neon plugin) {
        this.plugin = plugin;
    }

}
