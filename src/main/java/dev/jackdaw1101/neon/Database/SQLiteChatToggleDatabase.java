package dev.jackdaw1101.neon.Database;

import dev.jackdaw1101.neon.API.Features.Player.ToggleChat.ChatToggleDatabase;
import dev.jackdaw1101.neon.Neon;

import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLiteChatToggleDatabase implements ChatToggleDatabase {
    private final Neon plugin;
    private Connection connection;
    private boolean isInitialized = false;

    public SQLiteChatToggleDatabase(Neon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        try {
            // Ensure SQLite driver is loaded
            Class.forName("org.sqlite.JDBC");

            File dataFolder = new File(plugin.getDataFolder(), "data");
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                throw new SQLException("Failed to create data directory");
            }

            String name = plugin.getDatabaseManager().getString("SQLITE-CHATTOGGLE.NAME");
            File dbFile = new File(dataFolder, name + ".db");

            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);

            // Set connection properties
            connection.setAutoCommit(true);

            createTable();
            isInitialized = true;
            plugin.getLogger().info("SQLite database connected successfully!");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("SQLite JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            plugin.getLogger().severe("SQLite connection failed: " + e.getMessage());
        }
    }

    private void createTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS chat_toggle (" +
                "uuid TEXT PRIMARY KEY, " +
                "toggled BOOLEAN NOT NULL DEFAULT FALSE, " +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    private void checkInitialized() throws SQLException {
        if (!isInitialized || connection == null || connection.isClosed()) {
            throw new SQLException("Database not initialized or connection closed");
        }
    }

    @Override
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            isInitialized = false;
        } catch (SQLException e) {
            plugin.getLogger().warning("Error closing SQLite connection: " + e.getMessage());
        }
    }

    @Override
    public void addToggledPlayer(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            try {
                checkInitialized();
                String query = "INSERT OR REPLACE INTO chat_toggle (uuid, toggled) VALUES (?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, uuid.toString());
                    stmt.setBoolean(2, true);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error adding toggled player: " + e.getMessage());
            }
        });
    }

    @Override
    public void removeToggledPlayer(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            try {
                checkInitialized();
                String query = "INSERT OR REPLACE INTO chat_toggle (uuid, toggled) VALUES (?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, uuid.toString());
                    stmt.setBoolean(2, false);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error removing toggled player: " + e.getMessage());
            }
        });
    }

    @Override
    public boolean isPlayerToggled(UUID uuid) {
        try {
            checkInitialized();
            String query = "SELECT toggled FROM chat_toggle WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("toggled");
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error checking player toggle status: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Set<UUID> getAllToggledPlayers() {
        Set<UUID> toggledPlayers = new HashSet<>();
        try {
            checkInitialized();
            String query = "SELECT uuid FROM chat_toggle WHERE toggled = TRUE";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    try {
                        toggledPlayers.add(UUID.fromString(rs.getString("uuid")));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID format in database: " + rs.getString("uuid"));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error fetching toggled players: " + e.getMessage());
        }
        return toggledPlayers;
    }

    @Override
    public boolean isInitialized() {
        try {
            // For SQLite, we'll use a simple query to test the connection
            if (isInitialized && connection != null && !connection.isClosed()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeQuery("SELECT 1");
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            plugin.getLogger().warning("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void save() {
        if (!isInitialized()) {
            plugin.getLogger().warning("Attempted to save when database is not initialized");
            return;
        }

        try {
            // Commit any pending transactions
            if (!connection.getAutoCommit()) {
                connection.commit();
            }

            // For SQLite, we can optimize the database
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA wal_checkpoint(FULL)");
                stmt.execute("PRAGMA optimize");
            }
            plugin.getLogger().fine("SQLite database saved and optimized");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error saving SQLite database: " + e.getMessage());
        }
    }
}
