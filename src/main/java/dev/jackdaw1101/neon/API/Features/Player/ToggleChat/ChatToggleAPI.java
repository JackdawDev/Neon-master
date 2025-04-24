package dev.jackdaw1101.neon.API.Features.Player.ToggleChat;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public interface ChatToggleAPI {
    void toggleChat(Player player);
    void setChatToggled(Player player, boolean toggled);
    boolean isChatToggled(Player player);
    boolean isChatToggled(UUID uuid);
    Set<UUID> getAllToggledPlayers();

    void reload();
    void saveAll();
}
