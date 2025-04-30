package dev.jackdaw1101.neon.antiuniCode;

import dev.jackdaw1101.neon.api.features.antiunicode.AntiUnicodeEvent;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.api.utils.ColorHandler;
import dev.jackdaw1101.neon.utils.isounds.SoundUtil;
import dev.jackdaw1101.neon.utils.isounds.XSounds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class ListenerAntiUnicode implements Listener {
    private final Neon plugin;
    private List<String> allowedUnicodes;

    public ListenerAntiUnicode(Neon plugin) {
        this.plugin = plugin;
        this.allowedUnicodes = plugin.getSettings().getStringList("ANTI-UNICODE.BYPASSED-UNICODES");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String message = event.getMessage();

        if (!plugin.getSettings().getBoolean("ANTI-UNICODE.ENABLED")) return;
        if (player.hasPermission(plugin.getPermissionManager().getString("ANTI-UNICODE-BYPASS"))) return;

        String detectedUnicode = findUnicodeCharacters(message);
        if (detectedUnicode != null) {
            AntiUnicodeEvent unicodeEvent = new AntiUnicodeEvent(
                player,
                message,
                detectedUnicode,
                plugin.getSettings().getBoolean("ANTI-UNICODE.KICK-ENABLED"),
                ColorHandler.color(plugin.getMessageManager().getString("ANTI-UNICODE.BLOCK-MESSAGE")),
                ColorHandler.color(plugin.getMessageManager().getString("ANTI-UNICODE.KICK-MESSAGE")),
                plugin.getSettings().getString("ANTI-UNICODE.SOUND"),
                plugin.getSettings().getBoolean("ANTI-UNICODE.USE-SOUND")
            );

            plugin.getServer().getPluginManager().callEvent(unicodeEvent);

            if (unicodeEvent.isCancelled()) {
                return;
            }

            event.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, () -> handleUnicodeViolation(player, unicodeEvent));
        }
    }

    private String findUnicodeCharacters(String message) {
        StringBuilder unicodeChars = new StringBuilder();
        for (char c : message.toCharArray()) {
            if (c > 127 && !isAllowedUnicode(c)) {
                unicodeChars.append(c);
            }
        }
        return unicodeChars.length() > 0 ? unicodeChars.toString() : null;
    }

    private boolean isAllowedUnicode(char c) {
        String charStr = String.valueOf(c);
        return allowedUnicodes.contains(charStr);
    }

    private void handleUnicodeViolation(Player player, AntiUnicodeEvent event) {
        player.sendMessage(event.getBlockMessage());

        if (event.shouldPlaySound() && event.getSound() != null) {
            if (plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                SoundUtil.playSound(player, event.getSound(), 1.0f, 1.0f);
            } else if (plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                XSounds.playSound(player, event.getSound(), 1.0f, 1.0f);
            }
        }

        if (event.shouldKick()) {
            player.kickPlayer(event.getKickMessage());
        }
    }
}
