package dev.jackdaw1101.neon.utils.webhooks;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.CC;
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

public class AntiSwearWebhookManager {

    private final Neon plugin;
    private final String defaultWebhookUrl;
    private final boolean isWebhookEnabled;

    public AntiSwearWebhookManager(Neon plugin) {
        this.plugin = plugin;

        this.defaultWebhookUrl = (String) plugin.getDiscordManager().getString("ANTI-SWEAR.URL");

        this.isWebhookEnabled = (boolean) plugin.getDiscordManager().getBoolean("ANTI-SWEAR.ENABLED");
    }

    public void sendWebhook(Player player, String message, String type) {

        boolean debugMode = (boolean) plugin.getSettings().getBoolean("DEBUG-MODE");

        if (!isWebhookEnabled) {
            return;
        }


        String webhookUrl = determineWebhookUrl();


        String title = (String) plugin.getDiscordManager().getString("ANTI-SWEAR.FORMAT.title");
        if (title.equals("ANTI-SWEAR.FORMAT.title")) {
            Bukkit.getConsoleSender().sendMessage(CC.YELLOW +"[Neon] Config for title is missing or not set properly! Defaulting to 'Swear'");
        }


        List<String> descriptionLines = (List<String>) plugin.getDiscordManager().getStringList("ANTI-SWEAR.FORMAT.description");
        if (descriptionLines.isEmpty()) {
                Bukkit.getConsoleSender().sendMessage(CC.YELLOW +"[Neon] Config for description is missing or not set properly! Using default description.");
        }


        descriptionLines = formatDescription(descriptionLines, player, message);

        String description = String.join("\n", descriptionLines);


        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("content", "");
        JSONObject embedObject = new JSONObject();
        embedObject.put("title", title);
        embedObject.put("description", description);
        embedObject.put("color", Integer.parseInt(getColorForType(type).replace("#", ""), 16));
        jsonPayload.put("embeds", new JSONObject[]{embedObject});

        if (debugMode) {
            Bukkit.getConsoleSender().sendMessage(CC.YELLOW +"[Neon-Debug] Sending Webhook Payload: " + CC.AQUA + jsonPayload);
        }


        sendToWebhook(webhookUrl, jsonPayload.toString());
    }


    private String determineWebhookUrl() {
        String webhookUrl = this.defaultWebhookUrl;
        boolean debugMode = (boolean) plugin.getSettings().getBoolean("DEBUG-MODE");


        if (this.defaultWebhookUrl.equalsIgnoreCase("GLOBAL")) {
            webhookUrl = (String) plugin.getDiscordManager().getString("ANTI-SWEAR.URL");
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
        String servername = (String) plugin.getSettings().getString("SERVER-NAME");
        for (int i = 0; i < descriptionLines.size(); i++) {
            String line = descriptionLines.get(i);
            line = line.replace("<swearer_name>", player.getName())
                    .replace("<message>", message)
                    .replace("<server>", servername)
                    .replace("<version>", plugin.getDescription().getVersion())
                    .replace("<time>", formattedTime);

            descriptionLines.set(i, line);
        }
        return descriptionLines;
    }

    private void sendToWebhook(String webhookUrl, String jsonPayload) {

        boolean debugMode = (boolean) plugin.getSettings().getBoolean("DEBUG-MODE");

        new BukkitRunnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL(webhookUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);


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
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private String getColorForType(String type) {

        String colorCode = (String) plugin.getDiscordManager().getString("ANTI-SWEAR.COLOR");
        if (type.equals("censor") || type.equals("silent")) {
            return colorCode;
        }
        return "#ff0000";
    }
}
