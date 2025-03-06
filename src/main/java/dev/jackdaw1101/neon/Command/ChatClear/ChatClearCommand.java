package dev.jackdaw1101.neon.Command.ChatClear;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatClearCommand {

    private final Neon plugin;

    public ChatClearCommand(Neon plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("PLAYER-ONLY")));
            return true;
        }

        Player player = (Player) sender;
        String permission = (String) plugin.getSettings().getValue("CHAT-CLEAR.PERMISSION");
        if (!player.hasPermission(permission)) {
            player.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("NO-PERMISSION")));
            if ((boolean) plugin.getSettings().getValue("NO-PERMISSION.USE-SOUND", true)) {
                if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                    if ((boolean) plugin.getSettings().getValue("NO-PERMISSION.USE-SOUND", true)) {
                        SoundUtil.playSound((Player) sender, (String) plugin.getSettings().getValue("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
                    }
                } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                    XSounds.playSound((Player) sender, (String) plugin.getSettings().getValue("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
                }
            }
            return false;
        }

        clearChat(player);

        return true;
    }

    public void clearChat(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("PLAYER-ONLY")));
            return;
        }

        Player player = (Player) sender;
        String hasBypass = (String) plugin.getPermissionManager().getPermission("CHAT-CLEAR.BYPASS-PERMISSION");
        String permission = (String) plugin.getPermissionManager().getPermission("CHAT-CLEAR.PERMISSION");
        if (!player.hasPermission(permission)) {
            player.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("NO-PERMISSION")));
            return;
        }
        int emptyLines = (int) plugin.getSettings().getValue("CHAT-CLEAR.EMPTY-LINES", 100); // Default to 100 lines if not set
        boolean broadcast = (boolean) plugin.getSettings().getValue("CHAT-CLEAR.BROADCAST-TO-PLAYER", true); // Default to true if not set

        for (int i = 0; i < emptyLines; i++) {
            Bukkit.broadcastMessage("");
        }

        if (broadcast) {
            if (!player.hasPermission(hasBypass)) {
                Bukkit.broadcastMessage(ColorHandler.color(plugin.getMessageManager().getMessage("CHAT-CLEAR.BYPASS-MESSAGE").replace("%clearer%", player.getName())));
            } else {
                Bukkit.broadcastMessage(ColorHandler.color(plugin.getMessageManager().getMessage("CHAT-CLEAR.CLEARER-MESSAGE").replace("%clearer%", player.getName())));
            }
        } else {
            player.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("CHAT-CLEAR.SUCCESS-MESSAGE")));
        }

        if (!player.hasPermission(hasBypass)) {
            player.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("CHAT-CLEAR.BYPASS-MESSAGE").replace("%clearer%", player.getName())));
        }
    }
}