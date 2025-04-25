package dev.jackdaw1101.neon.Command.Chat;

import dev.jackdaw1101.neon.Command.API.NeonCommand;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.Features.ChatMute.ChatMuteManager;
import dev.jackdaw1101.neon.API.Utils.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class MuteChatCommand extends NeonCommand {
    private final Neon plugin;
    private final ChatMuteManager chatMuteManager;
    private final String muteChatPermission;

    public MuteChatCommand(Neon plugin) {
        super("mutechat", "Toggles global chat mute", "/mutechat", Arrays.asList("chatmute"));
        this.plugin = plugin;
        this.chatMuteManager = plugin.getChatMuteManager();
        this.muteChatPermission = plugin.getPermissionManager().getString("MUTE-CHAT-USE");
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(boolean) plugin.getSettings().getBoolean("MUTE-CHAT.ENABLED")) {
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("MUTE-CHAT.OFF")));
            return true;
        }

        if (!sender.hasPermission(muteChatPermission)) {
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("NO-PERMISSION")));
            playNoPermissionSound(sender);
            return true;
        }

        //boolean isMuted = chatMuteManager.isChatMuted();
        boolean isMuted = plugin.getChatMuteManager().isChatMuted();
        if (args.length == 0) {
            isMuted = !isMuted;
        } else if (args[0].equalsIgnoreCase("on")) {
            isMuted = true;
        } else if (args[0].equalsIgnoreCase("off")) {
            isMuted = false;
        } else {
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("MUTE-CHAT.INVALID-ARGUMENT")));
            return true;
        }

        plugin.getChatMuteManager().setChatMuted(isMuted);
        sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getString(isMuted ? "MUTE-CHAT.ENABLED" : "MUTE-CHAT.DISABLED")));
        return true;
    }

    private void playNoPermissionSound(CommandSender sender) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        if (!(boolean) plugin.getSettings().getBoolean("NO-PERMISSION.USE-SOUND")) return;
        String sound = (String) plugin.getSettings().getString("NO-PERMISSION.SOUND");

        if ((boolean) plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
            SoundUtil.playSound(player, sound, 1.0f, 1.0f);
        } else if ((boolean) plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
            XSounds.playSound(player, sound, 1.0f, 1.0f);
        }
    }
}
