package dev.jackdaw1101.neon.manager.commands.loggers;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.utils.logs.CommandLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandLoggerManager implements Listener {
    private final Neon plugin;

    public CommandLoggerManager(Neon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String command = event.getMessage();

        boolean commandLoggerEnabled = (Boolean) this.plugin.getSettings().getBoolean("LOG-COMMANDS");

        if (commandLoggerEnabled) {
            new CommandLogger(player, command, plugin);
        }
    }
}
