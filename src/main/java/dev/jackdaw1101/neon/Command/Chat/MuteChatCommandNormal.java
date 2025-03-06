package dev.jackdaw1101.neon.Command.Chat;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Chat.Manager.ChatMuteManager;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MuteChatCommandNormal {
    private final Neon plugin;
    private final ChatMuteManager chatMuteManager;
    private final String muteChatPermission;

    public MuteChatCommandNormal(Neon plugin) {
        this.plugin = plugin;
        this.chatMuteManager = plugin.getChatMuteManager();
        this.muteChatPermission = plugin.getPermissionManager().getPermission("MUTE-CHAT-USE");
    }

    public void toggleChatMute(CommandSender sender, String state) {
        if (!(boolean) plugin.getSettings().getValue("MUTE-CHAT.ENABLED", true)) {
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("MUTE-CHAT.OFF")));
            return;
        }

        if (!sender.hasPermission(muteChatPermission)) {
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("NO-PERMISSION")));
            if ((boolean) plugin.getSettings().getValue("NO-PERMISSION.USE-SOUND", true)) {
                if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                    if ((boolean) plugin.getSettings().getValue("NO-PERMISSION.USE-SOUND", true)) {
                        SoundUtil.playSound((Player) sender, (String) plugin.getSettings().getValue("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
                    }
                } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                    XSounds.playSound((Player) sender, (String) plugin.getSettings().getValue("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
                }
            }
            return;
        }

        boolean isMuted = chatMuteManager.isChatMuted();

        if (state == null) {
            isMuted = !isMuted;
        } else if (state.equalsIgnoreCase("on")) {
            isMuted = true;
        } else if (state.equalsIgnoreCase("off")) {
            isMuted = false;
        } else {
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("MUTE-CHAT.INVALID-ARGUMENT")));
            return;
        }

        chatMuteManager.setChatMuted(isMuted);
        sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage(isMuted ? "MUTE-CHAT.ENABLED" : "MUTE-CHAT.DISABLED")));
    }
}
