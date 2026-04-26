package dev.jackdaw1101.neon.modules.chat.listeners;

import dev.jackdaw1101.neon.API.modules.moderation.IChatToggle;
import dev.jackdaw1101.neon.Neon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ToggleChatListener implements Listener {

    private final IChatToggle IChatToggle;

    public ToggleChatListener(Neon plugin) {
        this.IChatToggle = plugin.getChatToggleAPI();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.getRecipients().removeIf(player -> IChatToggle.isChatToggled(player.getUniqueId()));
    }
}
