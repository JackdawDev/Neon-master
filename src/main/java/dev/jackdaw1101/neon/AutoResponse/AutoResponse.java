package dev.jackdaw1101.neon.AutoResponse;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class AutoResponse implements Listener {

    private final Neon plugin;

    public AutoResponse(Neon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check if the auto-response system is enabled
        if (!plugin.getSettings().getBoolean("AUTO-RESPONSE-ENABLED")) return;

        // Get the auto-response configuration section
        ConfigurationSection autoResponsesSection = plugin.getLocales().getConfig().getConfigurationSection("AUTO-RESPONSES");
        if (autoResponsesSection == null) return;

        // Iterate through the keys in the AUTO-RESPONSES section
        for (String key : autoResponsesSection.getKeys(false)) {
            if (message.toLowerCase().contains(key.toLowerCase())) {
                // Get and format the response
                List<String> responses = autoResponsesSection.getStringList(key);
                if (responses.isEmpty()) continue;

                // Retrieve the response format from the config
                String format = plugin.getMessageManager().getString("FORMAT");
                format = ColorHandler.color(format);

                // Prepare hover text if enabled
                TextComponent hoverComponent = null;
                if ((boolean) plugin.getSettings().getBoolean("AUTO-RESPONSE-HOVER-ENABLED")) {
                    List<String> hoverLines = plugin.getSettings().getConfig().getStringList("AUTO-RESPONSE-HOVER");
                    if (!hoverLines.isEmpty()) {
                        StringBuilder hoverText = new StringBuilder();
                        for (String line : hoverLines) {
                            hoverText.append(PlaceholderAPI.setPlaceholders(player, ColorHandler.color(line))).append("\n");
                        }
                        hoverComponent = new TextComponent(hoverText.toString().trim());
                    }
                }

                // Process each response line and send to the player
                for (String responseLine : responses) {
                    String formattedResponse = PlaceholderAPI.setPlaceholders(player, ColorHandler.color(responseLine));

                    // Apply the format and replace placeholders
                    String finalMessage = format.replace("%answer%", formattedResponse);

                    TextComponent textComponent = new TextComponent(finalMessage);

                    // Add hover event if available
                    if (hoverComponent != null) {
                        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverComponent}));
                    }

                    // Send the formatted line to the player
                    Bukkit.getScheduler().runTask(plugin, () ->
                            player.spigot().sendMessage(textComponent));
                    if (plugin.getSettings().getBoolean("AUTO-RESPONSE-USE-SOUND")) {
                        if (plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                            if (plugin.getSettings().getBoolean("AUTO-RESPONSE-USE-SOUND")) {
                                SoundUtil.playSound(player, (String) plugin.getSettings().getString("AUTO-RESPONSE-SOUND"), 1.0f, 1.0f);
                            }
                        } else if (plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                            XSounds.playSound(player, (String) plugin.getSettings().getString("AUTO-RESPONSE-SOUND"), 1.0f, 1.0f);
                        }
                    }
                }
                return;
            }
        }
    }
}
