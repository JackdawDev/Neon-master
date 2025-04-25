package dev.jackdaw1101.neon.Database;

import dev.jackdaw1101.neon.API.Features.Player.ToggleChat.ChatToggleDatabase;
import dev.jackdaw1101.neon.Neon;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQLChatToggleDatabase implements ChatToggleDatabase {
    private final Neon plugin;
    private Connection connection;
    private boolean isInitialized = false;

    public MySQLChatToggleDatabase(Neon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        try {

            Class.forName("com.mysql.jdbc.Driver");

            String host = plugin.getDatabaseManager().getString("MYSQL.HOST");
            int port = plugin.getDatabaseManager().getInt("MYSQL.PORT");
            String database = plugin.getDatabaseManager().getString("MYSQL.DATABASE");
            String username = plugin.getDatabaseManager().getString("MYSQL.USERNAME");
            String password = plugin.getDatabaseManager().getString("MYSQL.PASSWORD");
            boolean useSSL = plugin.getDatabaseManager().getBoolean("USE-SSL");
            boolean autoReconnect = plugin.getDatabaseManager().getBoolean("AUTO-RECONNECT");
            boolean failover = plugin.getDatabaseManager().getBoolean("FAIL-OVER-RED-ONLY");
            int maxReconnect = plugin.getDatabaseManager().getInt("MAX-RECONNECTS");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?useSSL=" + useSSL +
                "&autoReconnect=" + autoReconnect +
                "&failOverReadOnly=" + failover +
                "&maxReconnects=" + maxReconnect;

            connection = DriverManager.getConnection(url, username, password);


            if (!tableExists()) {
                createTable();
            } else {

                verifyTableStructure();
            }


            if (connection.isValid(5)) {
                isInitialized = true;
                plugin.getLogger().info("MySQL connection established successfully!");
            } else {
                throw new SQLException("Connection validation failed");
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("MySQL JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            plugin.getLogger().severe("MySQL connection failed: " + e.getMessage());
            shutdown();
        }
    }

    private boolean tableExists() throws SQLException {
        try (ResultSet rs = connection.getMetaData().getTables(null, null, "chat_toggle", null)) {
            return rs.next();
        }
    }

    private void verifyTableStructure() throws SQLException {
        try (ResultSet columns = connection.getMetaData().getColumns(null, null, "chat_toggle", "toggled")) {
            if (!columns.next()) {

                try (Statement stmt = connection.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE chat_toggle ADD COLUMN toggled BOOLEAN NOT NULL DEFAULT FALSE");
                }
            }
        }
    }

    private void createTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS chat_toggle (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "toggled BOOLEAN NOT NULL DEFAULT FALSE, " +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");


            try {
                stmt.executeUpdate("CREATE INDEX idx_toggled ON chat_toggle (toggled)");
            } catch (SQLException e) {
                plugin.getLogger().warning("Could not create index on toggled column: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean isInitialized() {
        try {
            return isInitialized &&
                connection != null &&
                !connection.isClosed() &&
                connection.isValid(2);
        } catch (SQLException e) {
            plugin.getLogger().warning("Error checking MySQL initialization status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("MySQL connection closed");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error closing MySQL connection: " + e.getMessage());
        } finally {
            isInitialized = false;
            connection = null;
        }
    }

    @Override
    public void save() {


        if (!isInitialized()) {
            plugin.getLogger().warning("Attempted to save when MySQL is not initialized");
            return;
        }

        try {

            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            plugin.getLogger().fine("MySQL data persisted successfully");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error saving MySQL database: " + e.getMessage());
        }
    }

    @Override
    public void addToggledPlayer(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            if (!isInitialized()) {
                plugin.getLogger().warning("Cannot add player - MySQL not initialized");
                return;
            }

            try {
                String query = "INSERT INTO chat_toggle (uuid, toggled) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE toggled = ?";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, uuid.toString());
                    stmt.setBoolean(2, true);
                    stmt.setBoolean(3, true);
                    stmt.executeUpdate();
                    plugin.getLogger().fine("Added toggled player: " + uuid);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error adding toggled player " + uuid + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void removeToggledPlayer(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            if (!isInitialized()) {
                plugin.getLogger().warning("Cannot remove player - MySQL not initialized");
                return;
            }

            try {
                String query = "INSERT INTO chat_toggle (uuid, toggled) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE toggled = ?";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, uuid.toString());
                    stmt.setBoolean(2, false);
                    stmt.setBoolean(3, false);
                    stmt.executeUpdate();
                    plugin.getLogger().fine("Removed toggled player: " + uuid);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error removing toggled player " + uuid + ": " + e.getMessage());
            }
        });
    }

    @Override
    public boolean isPlayerToggled(UUID uuid) {
        if (!isInitialized()) {
            plugin.getLogger().warning("Cannot check player - MySQL not initialized");
            return false;
        }

        try {
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
        if (!isInitialized()) {
            plugin.getLogger().warning("Cannot get players - MySQL not initialized");
            return toggledPlayers;
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uuid FROM chat_toggle WHERE toggled = TRUE")) {
            while (rs.next()) {
                try {
                    toggledPlayers.add(UUID.fromString(rs.getString("uuid")));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID format in database: " + rs.getString("uuid"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error fetching toggled players: " + e.getMessage());
        }
        return toggledPlayers;
    }
}
