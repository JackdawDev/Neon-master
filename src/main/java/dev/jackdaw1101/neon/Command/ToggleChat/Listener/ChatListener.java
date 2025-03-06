package dev.jackdaw1101.neon.Command.ToggleChat.Listener;

import dev.jackdaw1101.neon.Command.ToggleChat.ToggleChatCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class ChatListener implements Listener {

    private final ToggleChatCommand toggleChatCommand;

    public ChatListener(ToggleChatCommand toggleChatCommand) {
        this.toggleChatCommand = toggleChatCommand;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.getRecipients().removeIf(player -> toggleChatCommand.isChatToggledOff(player.getUniqueId()));
    }
}

