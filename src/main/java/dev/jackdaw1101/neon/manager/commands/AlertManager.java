package dev.jackdaw1101.neon.manager.commands;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.utils.sounds.ISound;
import dev.jackdaw1101.neon.utils.sounds.XSounds;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AlertManager implements CommandExecutor {
    private final Neon plugin;
    private final Set<UUID> alertsDisabled = new HashSet<>();

    public AlertManager(Neon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("PLAYER-ONLY")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(plugin.getPermissionManager().getString("TOGGLE-ALERTS"))) {
            String noPermissionMessage = ColorHandler.color(
                    plugin.getMessageManager().getString("NO-PERMISSION"));

            player.sendMessage(noPermissionMessage);
            playNoPermissionSound(player);
            return true;
        }

        toggleAlerts(player);
        return true;
    }


    public boolean isAlertsDisabled(Player player) {
        return alertsDisabled.contains(player.getUniqueId());
    }


    public void setAlertsDisabled(Player player, boolean disabled) {
        if (disabled) {
            alertsDisabled.add(player.getUniqueId());
        } else {
            alertsDisabled.remove(player.getUniqueId());
        }
    }


    public void toggleAlerts(Player player) {
        if (alertsDisabled.contains(player.getUniqueId())) {
            this.setAlertsDisabled(player, true);
            player.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("ALERTS-ENABLED")));
        } else {
            this.setAlertsDisabled(player, false);
            player.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("ALERTS-DISABLED")));
        }
    }


    private void playNoPermissionSound(Player player) {
        if ((boolean) plugin.getSettings().getBoolean("NO-PERMISSION.USE-SOUND")) {
            if ((boolean) plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                ISound.playSound(player, (String) plugin.getSettings().getString("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
            } else if ((boolean) plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                XSounds.playSound(player, (String) plugin.getSettings().getString("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
            }
        }
    }
}
