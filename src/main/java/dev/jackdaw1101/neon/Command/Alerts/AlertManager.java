package dev.jackdaw1101.neon.Command.Alerts;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("PLAYER-ONLY")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(plugin.getPermissionManager().getPermission("TOGGLE-ALERTS"))) {
            String noPermissionMessage = ColorHandler.color(
                    plugin.getMessageManager().getMessage("NO-PERMISSION"));

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
            player.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("ALERTS-ENABLED")));
        } else {
            this.setAlertsDisabled(player, false);
            player.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("ALERTS-DISABLED")));
        }
    }

    // Private method to play sound when the player lacks permission
    private void playNoPermissionSound(Player player) {
        if ((boolean) plugin.getSettings().getValue("NO-PERMISSION.USE-SOUND", true)) {
            if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                SoundUtil.playSound(player, (String) plugin.getSettings().getValue("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
            } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                XSounds.playSound(player, (String) plugin.getSettings().getValue("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
            }
        }
    }
}
