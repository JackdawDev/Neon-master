package dev.jackdaw1101.neon.AntiAdvertise.Discord;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.Utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WebhookManager {

    private final Neon plugin;
    private final String defaultWebhookUrl;
    private final boolean isWebhookEnabled;

    public WebhookManager(Neon plugin) {
        this.plugin = plugin;
        // Get webhook URL from config, "GLOBAL" or specific URL
        this.defaultWebhookUrl = (String) plugin.getDiscordManager().getString("ANTI-LINK.URL");
        // Enable/disable the webhook from config
        this.isWebhookEnabled = (boolean) plugin.getDiscordManager().getBoolean("ANTI-LINK.ENABLED");
    }

    public void sendWebhook(Player player, String message, String type) {
        // Fetch the debug mode setting
        boolean debugMode = plugin.getSettings().getBoolean("DEBUG-MODE");  // Default to true if not set

        if (!isWebhookEnabled) {
            return; // If webhook is disabled, do nothing
        }

        // Determine the appropriate URL (global or specific)
        String webhookUrl = determineWebhookUrl();

        // Get the formatted message data
        String title = (String) plugin.getDiscordManager().getString("ANTI-LINK.FORMAT.title");
        if (title.equals("ANTI-LINK.FORMAT.title")) {
            Bukkit.getConsoleSender().sendMessage(CC.YELLOW +"[Neon] Config for title is missing or not set properly! Defaulting to 'Swear'");
        }

        // Get description lines from config, expecting a list
        List<String> descriptionLines = plugin.getDiscordManager().getStringList("ANTI-LINK.FORMAT.description");
        if (descriptionLines.isEmpty()) {
                Bukkit.getConsoleSender().sendMessage(CC.YELLOW +"[Neon] Config for description is missing or not set properly! Using default description.");
        }

        // Replace placeholders in the description
        descriptionLines = formatDescription(descriptionLines, player, message);

        String description = String.join("\n", descriptionLines);  // Join description lines with newline characters

        // Build the JSON payload
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("content", "");
        JSONObject embedObject = new JSONObject();
        embedObject.put("title", title);
        embedObject.put("description", description);
        embedObject.put("color", Integer.parseInt(getColorForType(type).replace("#", ""), 16));
        jsonPayload.put("embeds", new JSONObject[]{embedObject});

        if (debugMode) {
            Bukkit.getConsoleSender().sendMessage(CC.YELLOW +"[Neon-Debug] Sending Webhook Payload: " + CC.AQUA + jsonPayload);  // Log the payload
        }

        // Send data to Discord webhook asynchronously
        sendToWebhook(webhookUrl, jsonPayload.toString());
    }


    private String determineWebhookUrl() {
        String webhookUrl = this.defaultWebhookUrl;
        boolean debugMode = plugin.getSettings().getBoolean("DEBUG-MODE");  // Default to true if not set

        // If it's a global setting, get the global URL
        if (this.defaultWebhookUrl.equalsIgnoreCase("GLOBAL")) {
            webhookUrl = (String) plugin.getDiscordManager().getString("ANTI-LINK.URL"); // Global URL fallback
        }

        if (webhookUrl == null || webhookUrl.isEmpty()) {
            if (debugMode) {
                Bukkit.getConsoleSender().sendMessage(CC.D_RED + "[Neon-Debug] Webhook URL is empty or null!");
            }
        } else {
            if (debugMode) {
                Bukkit.getConsoleSender().sendMessage(CC.GREEN + "[Neon-Debug] Using Webhook URL: " + CC.YELLOW + webhookUrl);
        }}
        return webhookUrl;
    }

    private List<String> formatDescription(List<String> descriptionLines, Player player, String message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        String formattedTime = dateFormat.format(new Date(System.currentTimeMillis()));
        String servername = plugin.getSettings().getString("SERVER-NAME");
        for (int i = 0; i < descriptionLines.size(); i++) {
            String line = descriptionLines.get(i);
            line = line.replace("<advertiser_name>", player.getName())
                    .replace("<message>", message)
                    .replace("<server>", servername)
                    .replace("<version>", plugin.getDescription().getVersion())
                    .replace("<time>", formattedTime);

            descriptionLines.set(i, line);
        }
        return descriptionLines;
    }

    private void sendToWebhook(String webhookUrl, String jsonPayload) {
        // Asynchronously send the message to Discord webhook using a BukkitRunnable
        boolean debugMode = plugin.getSettings().getBoolean("DEBUG-MODE");  // Default to true if not set

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // Create an HTTP connection to send the POST request
                    URL url = new URL(webhookUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    // Write the payload to the request
                    try (OutputStream os = connection.getOutputStream()) {
                        os.write(jsonPayload.getBytes());
                    }

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        if (debugMode) {
                            Bukkit.getConsoleSender().sendMessage(CC.GREEN + "[Neon-Debug] Webhook sent successfully to " + webhookUrl);}
                    } else {
                        if (debugMode) {
                            Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon-Debug] Failed to send webhook to " + CC.DARK_RED+ webhookUrl + CC.RED + ". Response code: " + CC.D_RED + responseCode);}
                    }
                    connection.disconnect();
                } catch (Exception e) {
                    if (debugMode) {
                        Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon-Debug] Error sending webhook: " + CC.D_RED + e.getMessage());}
                    e.printStackTrace();  // Print stack trace for debugging
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private String getColorForType(String type) {
        // Get the color code based on the type (e.g., Censor or Silent)
        String colorCode = (String) plugin.getDiscordManager().getString("ANTI-LINK.COLOR"); // Default to red if not set
        if (type.equals("silent")) {
            return colorCode; // Censorship message color
        }
        return "#ff0000"; // Default color for other types
    }
}
