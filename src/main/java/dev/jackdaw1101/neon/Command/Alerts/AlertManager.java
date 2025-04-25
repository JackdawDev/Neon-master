package dev.jackdaw1101.neon.Command.Alerts;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.Utils.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
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

    // Public method to check if a player has alerts disabled
    public boolean isAlertsDisabled(Player player) {
        return alertsDisabled.contains(player.getUniqueId());
    }

    // Public method to enable or disable alerts for a player
    public void setAlertsDisabled(Player player, boolean disabled) {
        if (disabled) {
            alertsDisabled.add(player.getUniqueId());
        } else {
            alertsDisabled.remove(player.getUniqueId());
        }
    }

    // New public method to toggle alerts, usable from other classes
    public void toggleAlerts(Player player) {
        if (alertsDisabled.contains(player.getUniqueId())) {
            this.setAlertsDisabled(player, true);
            player.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("ALERTS-ENABLED")));
        } else {
            this.setAlertsDisabled(player, false);
            player.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("ALERTS-DISABLED")));
        }
    }

    // Private method to play sound when the player lacks permission
    private void playNoPermissionSound(Player player) {
        if ((boolean) plugin.getSettings().getBoolean("NO-PERMISSION.USE-SOUND")) {
            if ((boolean) plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                SoundUtil.playSound(player, (String) plugin.getSettings().getString("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
            } else if ((boolean) plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                XSounds.playSound(player, (String) plugin.getSettings().getString("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
            }
        }
    }
}
