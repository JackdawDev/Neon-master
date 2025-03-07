package dev.jackdaw1101.neon.Command.ToggleChat;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ToggleChatCommand implements CommandExecutor {

    private final Neon plugin;
    private final Set<UUID> toggledOffPlayers = new HashSet<>();
    private final String toggleOnMessage;
    private final String toggleOffMessage;
    private final String noPermissionMessage;
    private final boolean permissionRequired;
    private final String requiredPermission;

    public ToggleChatCommand(Neon plugin) {
        this.plugin = plugin;

        toggleOnMessage = ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getString("CHAT-ON"));
        toggleOffMessage = ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getString("CHAT-OFF"));
        noPermissionMessage = ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getString("NO-PERMISSION"));
        permissionRequired = (boolean) plugin.getSettings().getBoolean("CHAT-TOGGLE.REQUIRE-PERMISSION");
        requiredPermission = plugin.getPermissionManager().getString("CHAT-TOGGLE-USE");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("PLAYER-ONLY")));
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (permissionRequired && !player.hasPermission(requiredPermission)) {
            player.sendMessage(noPermissionMessage);
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

        // Toggle chat reception
        UUID uuid = player.getUniqueId();
        Player p = player.getPlayer();
        if (toggledOffPlayers.contains(uuid)) {
            toggledOffPlayers.remove(uuid);
            player.sendMessage(toggleOnMessage);
        } else {
            toggledOffPlayers.add(uuid);
            player.sendMessage(toggleOffMessage);
        }

        return true;
    }

    public boolean isChatToggledOff(UUID uuid) {
        return toggledOffPlayers.contains(uuid);
    }
}

