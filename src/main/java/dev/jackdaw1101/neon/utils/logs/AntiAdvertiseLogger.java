package dev.jackdaw1101.neon.utils.logs;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AntiAdvertiseLogger {

    private final Neon plugin;
    private static final String LOG_FOLDER = "plugins/Neon/Logs/AntiAdvertise";
    private final Player player;
    private final String message;
    private boolean loggedOnce = false;

    public AntiAdvertiseLogger(Player player, String message, Neon plugin) {
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

        boolean debugMode = plugin.getSettings().getBoolean("DEBUG-MODE");
        if (!folder.exists() && !folder.mkdirs()) {
            if (debugMode && !loggedOnce) {
                Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Failed to create log folder at " + CC.D_RED + LOG_FOLDER);
                loggedOnce = true;
            }
            return;
        }


        String logFileName = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log";
        File logFile = new File(folder, logFileName);


        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String logEntry = String.format("[%s] <%s>: %s%n", timestamp, player.getName(), message);


        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(logEntry);
        } catch (IOException e) {
            if (debugMode && !loggedOnce) {
                Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Failed to write log for player " + player.getName() + ": " + CC.D_RED + e.getMessage());
                loggedOnce = true;
            }
        }
    }
}
