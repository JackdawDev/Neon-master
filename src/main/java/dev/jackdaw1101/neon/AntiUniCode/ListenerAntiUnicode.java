package dev.jackdaw1101.neon.AntiUniCode;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ListenerAntiUnicode implements Listener {
    private final Neon plugin;

    public ListenerAntiUnicode(Neon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String message = event.getMessage();

        if (!plugin.getSettings().getBoolean("ANTI-UNICODE.ENABLED")) return;
        if (player.hasPermission((String) plugin.getPermissionManager().getString("ANTI-UNICODE-BYPASS"))) return;

        if (containsUnicode(message)) {
            event.setCancelled(true);

            String blockMessage = ColorHandler.color(plugin.getMessageManager().getString("ANTI-UNICODE.BLOCK-MESSAGE"));
            if (plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                if (plugin.getSettings().getBoolean("ANTI-UNICODE.USE-SOUND")) {
                    SoundUtil.playSound(player, (String) plugin.getSettings().getString("ANTI-UNICODE.SOUND"), 1.0f, 1.0f);
                }
            } else if (plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                XSounds.playSound(player, (String) plugin.getSettings().getString("ANTI-UNICODE.SOUND"), 1.0f, 1.0f);
            }
            player.sendMessage(blockMessage);

            if (plugin.getSettings().getBoolean("ANTI-UNICODE.KICK-ENABLED")) {
                String kickMessage = ColorHandler.color(plugin.getMessageManager().getString("ANTI-UNICODE.KICK-MESSAGE"));
                player.kickPlayer(kickMessage);
            }
        }
    }

    private boolean containsUnicode(String message) {
        for (char c : message.toCharArray()) {
            if (c > 127) return true; // Detects non-ASCII characters
        }
        return false;
    }
}
