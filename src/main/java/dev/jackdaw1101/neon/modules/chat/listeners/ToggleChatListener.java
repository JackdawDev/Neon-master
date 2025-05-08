package dev.jackdaw1101.neon.modules.chat.listeners;

import dev.jackdaw1101.neon.API.modules.moderation.ChatToggleAPI;
import dev.jackdaw1101.neon.Neon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ToggleChatListener implements Listener {

    private final ChatToggleAPI chatToggleAPI;

    public ToggleChatListener(Neon plugin) {
        this.chatToggleAPI = plugin.getChatToggleAPI();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.getRecipients().removeIf(player -> chatToggleAPI.isChatToggled(player.getUniqueId()));
    }
}
