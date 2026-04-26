package dev.jackdaw1101.neon.modules.chat.listeners;

import dev.jackdaw1101.neon.API.modules.events.NeonPlayerChatEvent;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.manager.chat.ChatMuteManager;
import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.utils.DebugUtil;
import dev.jackdaw1101.neon.utils.sounds.ISound;
import dev.jackdaw1101.neon.utils.sounds.XSounds;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class ChatMuteListener implements Listener {
    private final Neon plugin;
    private final ChatMuteManager chatMuteManager;

    public ChatMuteListener(Neon plugin) {
        this.plugin = plugin;
        this.chatMuteManager = plugin.getChatMuteManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void muteNeonChat(NeonPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getSettings().getBoolean("MUTE-CHAT.MUTE-NEON-CHAT")) return;

        if (player.hasPermission(plugin.getPermissionManager().getString("MUTE-CHAT-BYPASS")) ||
                !chatMuteManager.isChatMuted()) return;

        player.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("MUTE-CHAT.DENIED-MESSAGE")));
        if (plugin.getSettings().getBoolean("MUTE-CHAT.USE-SOUND-FOR-BLOCKED-MESSAGES")) {
            if (plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                if (plugin.getSettings().getBoolean("MUTE-CHAT.USE-SOUND-FOR-BLOCKED-MESSAGES")) {
                    ISound.playSound(player, (String) plugin.getSettings().getString("MUTE-CHAT.DENIED-MESSAGE-SOUND"), 1.0f, 1.0f);
                }
            } else if ((boolean) plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                XSounds.playSound(player, (String) plugin.getSettings().getString("MUTE-CHAT.DENIED-MESSAGE-SOUND"), 1.0f, 1.0f);
            }
        }

        DebugUtil.debugChecked("&7" + player + " tried to send message while chat was muted! message: " + event.getMessage());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void muteAsyncChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getSettings().getBoolean("MUTE-CHAT.MUTE-ASYNC-CHAT")) return;

        if (player.hasPermission(plugin.getPermissionManager().getString("MUTE-CHAT-BYPASS")) ||
                !chatMuteManager.isChatMuted()) return;

        player.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("MUTE-CHAT.DENIED-MESSAGE")));
        if (plugin.getSettings().getBoolean("MUTE-CHAT.USE-SOUND-FOR-BLOCKED-MESSAGES")) {
            if (plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                if (plugin.getSettings().getBoolean("MUTE-CHAT.USE-SOUND-FOR-BLOCKED-MESSAGES")) {
                    ISound.playSound(player, (String) plugin.getSettings().getString("MUTE-CHAT.DENIED-MESSAGE-SOUND"), 1.0f, 1.0f);
                }
            } else if ((boolean) plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                XSounds.playSound(player, (String) plugin.getSettings().getString("MUTE-CHAT.DENIED-MESSAGE-SOUND"), 1.0f, 1.0f);
            }
        }

        DebugUtil.debugChecked("&7" + player + " tried to send message while chat was muted! message: " + event.getMessage());
        event.setCancelled(true);
    }


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
                            ISound.playSound(player, (String) plugin.getSettings().getString("MUTE-CHAT.BLOCKED-COMMAND-SOUND"), 1.0f, 1.0f);
                        }
                    } else if ((boolean) plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                        XSounds.playSound(player, (String) plugin.getSettings().getString("MUTE-CHAT.BLOCKED-COMMAND-SOUND"), 1.0f, 1.0f);
                    }
                }

                DebugUtil.debugChecked("&7" + player + " tried to use a blocked command while chat was muted! message: " + event.getMessage());
                event.setCancelled(true);
                return;
            }
        }
    }
}
