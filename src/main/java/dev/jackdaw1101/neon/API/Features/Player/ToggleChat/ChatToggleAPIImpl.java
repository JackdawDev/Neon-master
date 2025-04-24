package dev.jackdaw1101.neon.API.Features.Player.ToggleChat;

import dev.jackdaw1101.neon.API.Features.Player.ToggleChat.Event.ToggleChatEvent;
import dev.jackdaw1101.neon.Database.MemoryChatToggleDatabase;
import dev.jackdaw1101.neon.Database.MongoDBChatToggleDatabase;
import dev.jackdaw1101.neon.Database.MySQLChatToggleDatabase;
import dev.jackdaw1101.neon.Database.SQLiteChatToggleDatabase;
import dev.jackdaw1101.neon.Neon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ChatToggleAPIImpl implements ChatToggleAPI {
    private final Neon plugin;
    private ChatToggleDatabase database;

    public ChatToggleAPIImpl(Neon plugin) {
        this.plugin = plugin;
        initializeDatabase();
    }

    private void initializeDatabase() {
        this.database = createDatabase();
        database.initialize();

        // Verify initialization
        if (!database.isInitialized()) {
            plugin.getLogger().severe("Failed to initialize database! Using memory fallback");
            this.database = new MemoryChatToggleDatabase(plugin); // Fallback implementation
        }
    }

    private ChatToggleDatabase createDatabase() {
        String type = plugin.getDatabaseManager().getString("DATABASE.TYPE").toLowerCase();
        plugin.getLogger().info("Initializing database type: " + type);

        switch (type) {
            case "mysql":
                MySQLChatToggleDatabase mysqlDb = new MySQLChatToggleDatabase(plugin);
                mysqlDb.initialize();
                return mysqlDb;
            case "mongodb":
                MongoDBChatToggleDatabase mongoDb = new MongoDBChatToggleDatabase(plugin);
                mongoDb.initialize();
                return mongoDb;
            case "sqlite":
            default:
                SQLiteChatToggleDatabase sqliteDb = new SQLiteChatToggleDatabase(plugin);
                sqliteDb.initialize();
                return sqliteDb;
        }
    }

    @Override
    public void toggleChat(Player player) {
        if (player == null) return;

        boolean newState = !isChatToggled(player);
        boolean isdebug = plugin.getSettings().getBoolean("DEBUG-MODE");

        if (isdebug) {
            plugin.getLogger().info("Toggling chat for " + player.getName() + " to " + newState);
        }

        ToggleChatEvent event = new ToggleChatEvent(player, newState);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            if (isdebug) {
                plugin.getLogger().info("ToggleChatEvent was cancelled for " + player.getName());
            }
            return;
        }

        setChatToggled(player, event.getNewState());
    }

    @Override
    public void setChatToggled(Player player, boolean toggled) {
        CompletableFuture.runAsync(() -> {
            try {
                if (toggled) {
                    database.addToggledPlayer(player.getUniqueId());
                } else {
                    database.removeToggledPlayer(player.getUniqueId());
                }
                boolean isdebug = plugin.getSettings().getBoolean("DEBUG-MODE");

                if (isdebug) {
                    plugin.getLogger().info("Successfully set chat toggled=" + toggled + " for " + player.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error toggling chat for " + player.getName() + ": " + e.getMessage());
            }
        });
    }

    @Override
    public boolean isChatToggled(Player player) {
        return player != null && isChatToggled(player.getUniqueId());
    }

    @Override
    public boolean isChatToggled(UUID uuid) {
        try {
            return database.isPlayerToggled(uuid);
        } catch (Exception e) {
            plugin.getLogger().severe("Error checking chat toggle status for " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public Set<UUID> getAllToggledPlayers() {
        try {
            return database.getAllToggledPlayers();
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting all toggled players: " + e.getMessage());
            return new HashSet<>(); // Return empty set on error
        }
    }

    @Override
    public void reload() {
        database.shutdown();
        initializeDatabase();
    }

    @Override
    public void saveAll() {
        try {
            database.save();
        } catch (Exception e) {
            plugin.getLogger().severe("Error saving database: " + e.getMessage());
        }
    }
}
