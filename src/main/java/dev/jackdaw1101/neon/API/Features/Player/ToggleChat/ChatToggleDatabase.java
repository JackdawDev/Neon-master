package dev.jackdaw1101.neon.api.features.player.togglechat;

import java.util.Set;
import java.util.UUID;

public interface ChatToggleDatabase {
    void initialize();

    void shutdown();

    void addToggledPlayer(UUID uuid);

    void removeToggledPlayer(UUID uuid);

    boolean isPlayerToggled(UUID uuid);

    Set<UUID> getAllToggledPlayers();

    boolean isInitialized();

    void save();
}
