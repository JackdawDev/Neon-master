package dev.jackdaw1101.neon.AntiSwear.Logger;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.Utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AntiSwearLogger {

    private final Neon plugin;
    private static final String LOG_FOLDER = "plugins/Neon/Logs/AntiSwear"; // Path to log folder
    private final Player player;
    private final String message;
    private boolean loggedOnce = false;  // To prevent multiple messages being logged for the same error

    public AntiSwearLogger(Player player, String message, Neon plugin) {
        this.player = player;
        this.plugin = plugin;
        this.message = message;
        logToFile();
    }

    /**
     * Logs the player's message to a file in the log folder.
     */
    private void logToFile() {
        File folder = new File(LOG_FOLDER);

        boolean debugMode = (boolean) plugin.getSettings().getBoolean("DEBUG-MODE");  // Default to true if not set
        if (!folder.exists() && !folder.mkdirs()) {
            if (debugMode && !loggedOnce) {
                Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Failed to create log folder at " + CC.D_RED + LOG_FOLDER);
                loggedOnce = true;  // Set the flag to prevent multiple debug messages
            }
            return;
        }

        // Generate log file name based on the date
        String logFileName = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log";
        File logFile = new File(folder, logFileName);

        // Log format: [time_date] <player>: <message>
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String logEntry = String.format("[%s] <%s>: %s%n", timestamp, player.getName(), message);

        // Write to the log file
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(logEntry);
        } catch (IOException e) {
            if (debugMode && !loggedOnce) {
                Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Failed to write log for player " + player.getName() + ": " + CC.D_RED + e.getMessage());
                loggedOnce = true;  // Set the flag to prevent multiple debug messages
            }
        }
    }
}
