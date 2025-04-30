package dev.jackdaw1101.neon.database.togglechat;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.database.ChatToggleDatabase;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MemoryChatToggleDatabase implements ChatToggleDatabase {
    private final Set<UUID> toggledPlayers = new HashSet<>();
    private final Neon plugin;

    public MemoryChatToggleDatabase(Neon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        plugin.getLogger().warning("Using in-memory fallback database!");
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void shutdown() {
        toggledPlayers.clear();
    }

    @Override
    public void addToggledPlayer(UUID uuid) {
        toggledPlayers.add(uuid);
    }

    @Override
    public void removeToggledPlayer(UUID uuid) {
        toggledPlayers.remove(uuid);
    }

    @Override
    public boolean isPlayerToggled(UUID uuid) {
        return toggledPlayers.contains(uuid);
    }

    @Override
    public Set<UUID> getAllToggledPlayers() {
        return new HashSet<>(toggledPlayers);
    }

    @Override
    public void save() {

    }
}
