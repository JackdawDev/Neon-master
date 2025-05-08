package dev.jackdaw1101.neon.modules.automated.ai.listener;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.modules.events.AiSwearDetectEvent;
import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.utils.sounds.ISound;
import dev.jackdaw1101.neon.utils.sounds.XSounds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class ChatListener implements Listener {
    private final Neon plugin;

    public ChatListener(Neon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (!plugin.getSettings().getBoolean("AI.ENABLED")) return;

        if (player.hasPermission(plugin.getPermissionManager().getString("AI-BYPASS"))) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> violations = plugin.getModerationManager().moderate(message);

            if (!violations.isEmpty()) {
                AiSwearDetectEvent chatFlagEvent = new AiSwearDetectEvent(player, message, violations);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(chatFlagEvent);

                    if (plugin.getSettings().getBoolean("ACTIONS.CANCEL-MESSAGE")) {
                        event.setCancelled(true);
                    }

                    if (plugin.getSettings().getBoolean("AI.PLAY-SOUND")) {
                        if ((boolean) plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                            if ((boolean) plugin.getSettings().getBoolean("MENTION.ENABLE-SOUND")) {
                                ISound.playSound(player, (String) plugin.getSettings().getString("AI.SOUND"), 1.0f, 1.0f);
                            }
                        } else if ((boolean) plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                            XSounds.playSound(player, (String) plugin.getSettings().getString("AI.SOUND"), 1.0f, 1.0f);
                        }
                    }

                    if (plugin.getSettings().getBoolean("ACTIONS.NOTIFY-PLAYER")) {
                        player.sendMessage(ColorHandler.color(
                                plugin.getMessageManager().getString("AI.FLAGGED"))
                            .replace("<categories>", String.join(", ", violations)));
                    }

                    if (plugin.getSettings().getBoolean("ACTIONS.NOTIFY-STAFF")) {
                        String msg = plugin.getMessageManager().getString("AI.STAFF-ALERT")
                            .replace("<player>", player.getName())
                            .replace("<message>", message)
                            .replace("<categories>", String.join(", ", violations));
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.hasPermission(plugin.getPermissionManager().getString("AI-NOTIFY"))) {
                                p.sendMessage(ColorHandler.color(msg));
                            }
                        }
                    }
                });
            }
        });
    }
}
