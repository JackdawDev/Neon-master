package dev.jackdaw1101.neon.Command;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class NeonReloadCommand implements CommandExecutor {
    private final Neon plugin;

    public NeonReloadCommand(Neon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(plugin.getPermissionManager().getPermission("RELOAD"))) {
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("NO-PERMISSION")));
            if ((boolean) plugin.getSettings().getValue("NO-PERMISSION.USE-SOUND", true)) {
                if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                    if ((boolean) plugin.getSettings().getValue("MENTION.ENABLE-SOUND", true)) {
                        SoundUtil.playSound((Player) sender, (String) plugin.getSettings().getValue("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
                    }
                } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                    XSounds.playSound((Player) sender, (String) plugin.getSettings().getValue("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
                }
            }
            return true;
        }

        executeReload(sender);
        return true;
    }

    public void executeReload(CommandSender sender) {
        boolean messagesReloaded = plugin.getMessageManager().reloadMessages();
        boolean settingsReloaded = plugin.getSettings().reloadSettings();
        boolean discordReloaded = plugin.getMessageManager().reloadMessages();
        boolean permsReloaded = plugin.getPermissionManager().reloadPermissions();
        boolean localesReloaded = plugin.getLocales().reloadLocales();

        if (messagesReloaded && settingsReloaded && permsReloaded && discordReloaded && localesReloaded) {
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("RELOADED-SUCCESSFULLY")));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("RELOAD-FAILED")));
        }
    }
}
