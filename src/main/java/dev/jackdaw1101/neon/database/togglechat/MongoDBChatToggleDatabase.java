package dev.jackdaw1101.neon.database.togglechat;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.database.ChatToggleDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoDBChatToggleDatabase implements ChatToggleDatabase {
    private final Neon plugin;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private boolean isConnected = false;

    public MongoDBChatToggleDatabase(Neon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        try {
            String connectionString = plugin.getDatabaseManager().getString("MONGODB.URL");
            String databaseName = plugin.getDatabaseManager().getString("MONGODB.DATABASE");


            mongoClient = MongoClients.create(connectionString);
            database = mongoClient.getDatabase(databaseName);
            collection = database.getCollection("chat_toggle");
            isConnected = true;


            Bson index = new Document("uuid", 1);
            collection.createIndex(index);


            collection.countDocuments();

            plugin.getLogger().info("MongoDB connection established successfully!");
        } catch (Exception e) {
            plugin.getLogger().severe("MongoDB connection failed: " + e.getMessage());
            isConnected = false;
            shutdown();
        }
    }

    @Override
    public boolean isInitialized() {
        try {
            return isConnected &&
                mongoClient != null &&
                database != null &&
                collection != null &&
                mongoClient.listDatabaseNames().first() != null;
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking MongoDB initialization status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void shutdown() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
                plugin.getLogger().info("MongoDB connection closed");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error closing MongoDB connection: " + e.getMessage());
        } finally {
            isConnected = false;
            mongoClient = null;
            database = null;
            collection = null;
        }
    }

    @Override
    public void save() {


        if (!isInitialized()) {
            plugin.getLogger().warning("Attempted to save when MongoDB is not initialized");
            return;
        }
        plugin.getLogger().fine("MongoDB data persisted successfully");
    }

    private void ensureProfile(UUID uuid) {
        if (!isInitialized()) return;

        try {
            Document existing = collection.find(Filters.eq("uuid", uuid.toString())).first();
            if (existing == null) {
                Document doc = new Document("uuid", uuid.toString())
                    .append("toggled", false)
                    .append("lastUpdated", System.currentTimeMillis());
                collection.insertOne(doc);
                plugin.getLogger().fine("Created new profile for " + uuid);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error ensuring profile for " + uuid + ": " + e.getMessage());
        }
    }

    @Override
    public void addToggledPlayer(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            if (!isInitialized()) {
                plugin.getLogger().warning("Cannot add player - MongoDB not initialized");
                return;
            }

            try {
                ensureProfile(uuid);
                collection.updateOne(
                    Filters.eq("uuid", uuid.toString()),
                    Updates.combine(
                        Updates.set("toggled", true),
                        Updates.set("lastUpdated", System.currentTimeMillis())
                    )
                );
                plugin.getLogger().fine("Added toggled player: " + uuid);
            } catch (Exception e) {
                plugin.getLogger().severe("Error adding toggled player " + uuid + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void removeToggledPlayer(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            if (!isInitialized()) {
                plugin.getLogger().warning("Cannot remove player - MongoDB not initialized");
                return;
            }

            try {
                ensureProfile(uuid);
                collection.updateOne(
                    Filters.eq("uuid", uuid.toString()),
                    Updates.combine(
                        Updates.set("toggled", false),
                        Updates.set("lastUpdated", System.currentTimeMillis())
                    )
                );
                plugin.getLogger().fine("Removed toggled player: " + uuid);
            } catch (Exception e) {
                plugin.getLogger().severe("Error removing toggled player " + uuid + ": " + e.getMessage());
            }
        });
    }

    @Override
    public boolean isPlayerToggled(UUID uuid) {
        if (!isInitialized()) {
            plugin.getLogger().warning("Cannot check player - MongoDB not initialized");
            return false;
        }

        try {
            Document doc = collection.find(Filters.eq("uuid", uuid.toString())).first();
            return doc != null && doc.getBoolean("toggled", false);
        } catch (Exception e) {
            plugin.getLogger().severe("Error checking if player is toggled: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Set<UUID> getAllToggledPlayers() {
        Set<UUID> toggled = new HashSet<>();
        if (!isInitialized()) {
            plugin.getLogger().warning("Cannot get players - MongoDB not initialized");
            return toggled;
        }

        try (MongoCursor<Document> cursor = collection.find(Filters.eq("toggled", true)).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                try {
                    toggled.add(UUID.fromString(doc.getString("uuid")));
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid UUID format in database: " + doc.getString("uuid"));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error fetching toggled players: " + e.getMessage());
        }
        return toggled;
    }
}
