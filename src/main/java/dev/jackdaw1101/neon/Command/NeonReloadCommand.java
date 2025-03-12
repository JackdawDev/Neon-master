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
        plugin.getLocales().reloadConfig();
        plugin.getSettings().reloadConfig();
        plugin.getMessageManager().reloadConfig();
        plugin.getPermissionManager().reloadConfig();
        plugin.getDiscordManager().reloadConfig();

        File serverDir = Bukkit.getServer().getWorldContainer();
        File pluginsDir = new File(serverDir, "plugins");

        String pluginName = "AstroLoader";
        String subPluginName = "TxActionBar";

        File pluginDir = new File(pluginsDir, pluginName);
        File subPluginDir = new File(pluginDir, subPluginName);

        if (!pluginDir.exists()) pluginDir.mkdirs();
        if (!subPluginDir.exists()) subPluginDir.mkdirs();

        File Settings = new File(subPluginDir, "settings.yml");

        try {
            ConfigUpdater.update(plugin, "settings.yml", Settings);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File Messages = new File(subPluginDir, "messages.yml");

        try {
            ConfigUpdater.update(plugin, "messages.yml", Messages);
        } catch (IOException e) {
            e.printStackTrace();
        }


        File Permissions = new File(subPluginDir, "permissions.yml");


        try {
            ConfigUpdater.update(plugin, "permissions.yml", Permissions);
        } catch (IOException e) {
            e.printStackTrace();
        }


        File Discord = new File(subPluginDir, "discord.yml");

        try {
            ConfigUpdater.update(plugin, "discord.yml", Discord);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("RELOADED-SUCCESSFULLY")));
        } //else {
        //    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("RELOAD-FAILED")));
        //}
    }
