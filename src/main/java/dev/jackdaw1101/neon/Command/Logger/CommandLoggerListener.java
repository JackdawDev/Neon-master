package dev.jackdaw1101.neon.Command.Logger;

import dev.jackdaw1101.neon.Neon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandLoggerListener implements Listener {
    private final Neon plugin;

    public CommandLoggerListener(Neon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String command = event.getMessage(); // Get the full command

        boolean commandLoggerEnabled = (Boolean) this.plugin.getSettings().getValue("LOG-COMMANDS", true);

        if (commandLoggerEnabled) {
            new CommandLogger(player, command, plugin);
        }
    }
}
