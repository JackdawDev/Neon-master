package dev.jackdaw1101.neon.Chat;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.Features.ChatMute.ChatMuteManager;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class ListenerMuteChat implements Listener {
    private final Neon plugin;
    private final ChatMuteManager chatMuteManager;

    public ListenerMuteChat(Neon plugin) {
        this.plugin = plugin;
        this.chatMuteManager = plugin.getChatMuteManager(); // Get instance from main class
    }

    @EventHandler(ignoreCancelled = true)
    public void muteChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission(plugin.getPermissionManager().getString("MUTE-CHAT-BYPASS")) ||
                !chatMuteManager.isChatMuted()) return;

        player.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("MUTE-CHAT.DENIED-MESSAGE")));
        if (plugin.getSettings().getBoolean("MUTE-CHAT.USE-SOUND-FOR-BLOCKED-MESSAGES")) {
            if (plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                if (plugin.getSettings().getBoolean("MUTE-CHAT.USE-SOUND-FOR-BLOCKED-MESSAGES")) {
                    SoundUtil.playSound(player, (String) plugin.getSettings().getString("MUTE-CHAT.DENIED-MESSAGE-SOUND"), 1.0f, 1.0f);
                }
            } else if ((boolean) plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                XSounds.playSound(player, (String) plugin.getSettings().getString("MUTE-CHAT.DENIED-MESSAGE-SOUND"), 1.0f, 1.0f);
            }
        }
        event.setCancelled(true);
    }

    //BLOCKED-COMMAND-SOUND
    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getSettings().getBoolean("MUTE-CHAT.DISABLE-COMMANDS-ENABLED") ||
                player.hasPermission(plugin.getPermissionManager().getString("MUTE-CHAT-BYPASS")) ||
                !chatMuteManager.isChatMuted()) return;

        List<String> disabledCommands = (List<String>) plugin.getSettings().getStringList("MUTE-CHAT.DISABLED-COMMANDS");

        for (String command : disabledCommands) {
            if (event.getMessage().toLowerCase().startsWith("/" + command.toLowerCase())) {
                player.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("MUTE-CHAT.BLOCKED-COMMANDS-MESSAGE")));
                if ((boolean) plugin.getSettings().getBoolean("MUTE-CHAT.USE-SOUND-FOR-BLOCKED-COMMANDS")) {
                    if ((boolean) plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                        if ((boolean) plugin.getSettings().getBoolean("MUTE-CHAT.USE-SOUND-FOR-BLOCKED-COMMANDS")) {
                            SoundUtil.playSound(player, (String) plugin.getSettings().getString("MUTE-CHAT.BLOCKED-COMMAND-SOUND"), 1.0f, 1.0f);
                        }
                    } else if ((boolean) plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                        XSounds.playSound(player, (String) plugin.getSettings().getString("MUTE-CHAT.BLOCKED-COMMAND-SOUND"), 1.0f, 1.0f);
                    }
                }
                event.setCancelled(true);
                return;
            }
        }
    }
}
