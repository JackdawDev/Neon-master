package dev.jackdaw1101.neon.Command.Chat;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.Features.ChatMute.ChatMuteManager;
import dev.jackdaw1101.neon.API.Utils.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MuteChatCommandNormal {
    private final Neon plugin;
    private final ChatMuteManager chatMuteManager;
    private final String muteChatPermission;

    public MuteChatCommandNormal(Neon plugin) {
        this.plugin = plugin;
        this.chatMuteManager = plugin.getChatMuteManager();
        this.muteChatPermission = plugin.getPermissionManager().getString("MUTE-CHAT-USE");
    }

    public void toggleChatMute(CommandSender sender, String state) {
        if (!(boolean) plugin.getSettings().getBoolean("MUTE-CHAT.ENABLED")) {
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("MUTE-CHAT.OFF")));
            return;
        }

        if (!sender.hasPermission(muteChatPermission)) {
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("NO-PERMISSION")));
            if ((boolean) plugin.getSettings().getBoolean("NO-PERMISSION.USE-SOUND")) {
                if ((boolean) plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                    if ((boolean) plugin.getSettings().getBoolean("NO-PERMISSION.USE-SOUND")) {
                        SoundUtil.playSound((Player) sender, (String) plugin.getSettings().getString("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
                    }
                } else if ((boolean) plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                    XSounds.playSound((Player) sender, (String) plugin.getSettings().getString("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
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
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("MUTE-CHAT.INVALID-ARGUMENT")));
            return;
        }

        chatMuteManager.setChatMuted(isMuted);
        sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getString(isMuted ? "MUTE-CHAT.ENABLED" : "MUTE-CHAT.DISABLED")));
    }
}
