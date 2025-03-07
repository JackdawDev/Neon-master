package dev.jackdaw1101.neon.AntiCaps;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Chat.CC;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class AntiCapsSystem implements Listener {
    private final Neon plugin;

    public AntiCapsSystem(Neon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        if (plugin.getSettings().getBoolean("ANTI-CAPS.ENABLED")) {
            handleCapsCheck(event.getPlayer(), event.getMessage(), event);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;

        if (plugin.getSettings().getBoolean("ANTI-CAPS.CHECK-COMMANDS")) {
            handleCapsCheck(event.getPlayer(), event.getMessage(), event);
        }
    }

    private void handleCapsCheck(Player player, String message, Cancellable cancellable) {
        if (player.hasPermission(this.plugin.getPermissionManager().getString("ANTI-CAPS-BYPASS"))) {
            return;
        }

        int upperChar = 0;
        int lowerChar = 0;
        int minLength = plugin.getSettings().getInt("ANTI-CAPS.MIN-MESSAGE-LENGTH");
        int requiredPercentage = plugin.getSettings().getInt("ANTI-CAPS.REQUIRED-PERCENTAGE");

        if (message.length() >= minLength) {
            for (char c : message.toCharArray()) {
                if (Character.isLetter(c)) {
                    if (Character.isUpperCase(c)) {
                        upperChar++;
                    } else {
                        lowerChar++;
                    }
                }
            }

            if (upperChar + lowerChar > 0) {
                double capsPercentage = (double) upperChar / (upperChar + lowerChar) * 100;
                if (capsPercentage >= requiredPercentage) {
                    cancellable.setCancelled(true);
                    player.sendMessage(ColorHandler.color((String) this.plugin.getMessageManager().getString("ANTI-CAPS-WARNING")));
                    if (plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                        if (plugin.getSettings().getBoolean("ANTI-CAPS.SOUND-ENABLED")) {
                            SoundUtil.playSound(player, plugin.getSettings().getString("ANTI-CAPS.SOUND"), 1.0f, 1.0f);
                        }
                    } else if (plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                        XSounds.playSound(player, plugin.getSettings().getString("ANTI-CAPS.SOUND"), 1.0f, 1.0f);
                    }
                }
            }
        }
    }
}
