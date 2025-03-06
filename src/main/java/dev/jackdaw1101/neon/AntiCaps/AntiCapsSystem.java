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

        if ((Boolean) this.plugin.getSettings().getValue("ANTI-CAPS.ENABLED", true)) {
            handleCapsCheck(event.getPlayer(), event.getMessage(), event);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;

        if ((Boolean) this.plugin.getSettings().getValue("ANTI-CAPS.CHECK-COMMANDS", false)) {
            handleCapsCheck(event.getPlayer(), event.getMessage(), event);
        }
    }

    private void handleCapsCheck(Player player, String message, Cancellable cancellable) {
        if (player.hasPermission(this.plugin.getPermissionManager().getPermission("ANTI-CAPS-BYPASS"))) {
            return;
        }

        int upperChar = 0;
        int lowerChar = 0;
        int minLength = (Integer) this.plugin.getSettings().getValue("ANTI-CAPS.MIN-MESSAGE-LENGTH", 5);
        int requiredPercentage = (Integer) this.plugin.getSettings().getValue("ANTI-CAPS.REQUIRED-PERCENTAGE", 70);

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
                    player.sendMessage(ColorHandler.color((String) this.plugin.getMessageManager().getMessage("ANTI-CAPS-WARNING")));
                    if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                        if ((boolean) plugin.getSettings().getValue("ANTI-CAPS.SOUND-ENABLED", true)) {
                            SoundUtil.playSound(player, (String) plugin.getSettings().getValue("ANTI-CAPS.SOUND"), 1.0f, 1.0f);
                        }
                    } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                        XSounds.playSound(player, (String) plugin.getSettings().getValue("ANTI-CAPS.SOUND"), 1.0f, 1.0f);
                    }
                }
            }
        }
    }
}
