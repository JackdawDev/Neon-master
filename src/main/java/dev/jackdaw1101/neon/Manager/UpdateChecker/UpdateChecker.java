package dev.jackdaw1101.neon.Manager.UpdateChecker;

import dev.jackdaw1101.neon.Neon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class UpdateChecker {

    private final Neon plugin;
    private final int resourceId;
    private final String spigotApiUrl;
    private final String downloadUrl;

    public UpdateChecker(Neon plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.spigotApiUrl = "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId;
        this.downloadUrl = "https://api.spiget.org/v2/resources/" + resourceId + "/download";
    }

    /**
     * Checks if an update is required by comparing local and remote versions.
     */
    public boolean isUpdateRequired() {
        String latestVersion = getUpdateVersion();
        if (latestVersion == null) return false;
        return !plugin.getDescription().getVersion().equalsIgnoreCase(latestVersion);
    }

    /**
     * Gets the latest version from Spigot.
     */
    public String getUpdateVersion() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(spigotApiUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString().trim();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("Could not fetch latest plugin version: " + e.getMessage());
            return null;
        }
    }


    /**
     * Downloads and replaces the plugin jar automatically.
     */
    public void autoUpdate() {
        try {
            if (plugin.getSettings().getBoolean("UPDATE-SYSTEM.AUTO-UPDATE")) {
                plugin.getLogger().info("§7[Neon] Starting auto-update...");

                File pluginFile = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                File tempFile = new File(pluginFile.getParentFile(), plugin.getName() + "-new.jar");

                HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                InputStream inputStream = connection.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(tempFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                Files.move(tempFile.toPath(), pluginFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                Bukkit.getConsoleSender().sendMessage("§7[Neon] Successfully updated plugin! Restart the server to apply changes.");
            }
            } catch(Exception e){
                Bukkit.getConsoleSender().sendMessage("§c[Neon] Failed to auto-update: " + e.getMessage());
                e.printStackTrace();
            }
    }


    /**
     * Displays a pretty update message.
     */
    public void sendUpdateMessage(String newVersion) {
        Bukkit.getConsoleSender().sendMessage("§7§m----------------------------------------");
        Bukkit.getConsoleSender().sendMessage("§eAn update is available for Neon!");
        Bukkit.getConsoleSender().sendMessage("§fCurrent version: §c" + plugin.getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("§fNew version: §a" + newVersion);
        Bukkit.getConsoleSender().sendMessage("§6Download it from: §bhttps://www.spigotmc.org/resources/" + resourceId);
        Bukkit.getConsoleSender().sendMessage("§7§m----------------------------------------");
    }

    /**
     * Displays a pretty update message to player.
     */
    public void sendUpdateMessagePlayer(String newVersion, Player player) {
        player.sendMessage("§7§m----------------------------------------");
        player.sendMessage("§eAn update is available for Neon!");
        player.sendMessage("§fCurrent version: §a" + plugin.getDescription().getVersion());
        player.sendMessage("§fNew version: §a" + newVersion);
        player.sendMessage("§fDownload it from: §bhttps://www.spigotmc.org/resources/" + resourceId);
        player.sendMessage("§7§m----------------------------------------");
    }
}
