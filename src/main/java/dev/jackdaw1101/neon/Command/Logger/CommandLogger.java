package dev.jackdaw1101.neon.Command.Logger;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.Utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommandLogger {
    private final Neon plugin;
    private static final String LOG_FOLDER = "plugins/Neon/Logs/Commands"; // Path to log folder
    private final Player player;
    private final String command;

    public CommandLogger(Player player, String command, Neon plugin) {
        this.player = player;
        this.plugin = plugin;
        this.command = command;
        logToFile();
    }

    /**
     * Logs the player's executed command to a file in the log folder.
     */
    private void logToFile() {
        File folder = new File(LOG_FOLDER);

        boolean debugMode = (boolean) plugin.getSettings().getBoolean("DEBUG-MODE");  // Default to true if not set
        if (!folder.exists() && !folder.mkdirs()) {
            if (debugMode) {
                Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Failed to create log folder at " + CC.D_RED + LOG_FOLDER);
            }
            return;
        }

        // Generate log file name based on the date
        String logFileName = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log";
        File logFile = new File(folder, logFileName);

        // Log format: [time_date] <player> executed: <command>
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String logEntry = String.format("[%s] <%s> executed: %s%n", timestamp, player.getName(), command);

        // Write to the log file
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(logEntry);
        } catch (IOException e) {
            if (debugMode) {
                Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Failed to write log for player " + player.getName() + ": " + CC.D_RED + e.getMessage());
            }
        }
    }
}
