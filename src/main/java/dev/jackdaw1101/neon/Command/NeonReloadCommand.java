package dev.jackdaw1101.neon.Command;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.jackdaw1101.neon.Configurations.ConfigFile;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class NeonReloadCommand implements CommandExecutor {
    private final Neon plugin;
    private ConfigFile configAPI;

    public NeonReloadCommand(Neon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(plugin.getPermissionManager().getString("RELOAD"))) {
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
            return true;
        }

        executeReload(sender);
        return true;
    }

    public void executeReload(CommandSender sender) {
        //boolean messagesReloaded = plugin.getMessageManager().reloadMessages();
        //boolean settingsReloaded = plugin.getSettings().reloadSettings();
        //boolean discordReloaded = plugin.getMessageManager().reloadMessages();
        //boolean permsReloaded = plugin.getPermissionManager().reloadPermissions();
        //boolean localesReloaded = plugin.getLocales().reloadLocales();
        //configAPI.reloadAllConfigs();
        plugin.getLocales().reload();
        plugin.getSettings().reload();
        plugin.getMessageManager().reload();
        plugin.getPermissionManager().reload();
        plugin.getDiscordManager().reload();

        sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("RELOADED-SUCCESSFULLY")));
        } //else {
        //    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("RELOAD-FAILED")));
        //}
    }
