package dev.jackdaw1101.neon.command.togglechat.listener;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.api.features.player.togglechat.ChatToggleAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final ChatToggleAPI chatToggleAPI;

    public ChatListener(Neon plugin) {
        this.chatToggleAPI = plugin.getChatToggleAPI();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.getRecipients().removeIf(player -> chatToggleAPI.isChatToggled(player.getUniqueId()));
    }
}
