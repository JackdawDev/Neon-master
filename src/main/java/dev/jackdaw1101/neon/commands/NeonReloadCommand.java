package dev.jackdaw1101.neon.commands;

import dev.jackdaw1101.neon.utils.configs.ConfigFile;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.utils.sounds.ISound;
import dev.jackdaw1101.neon.utils.sounds.XSounds;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                        ISound.playSound((Player) sender, (String) plugin.getSettings().getString("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
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

        plugin.getLocales().reloadLocales();
        plugin.getSettings().reload();
        plugin.getMessageManager().reload();
        plugin.getPermissionManager().reload();
        plugin.getDiscordManager().reload();

        sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("RELOADED-SUCCESSFULLY")));
        }


    }
