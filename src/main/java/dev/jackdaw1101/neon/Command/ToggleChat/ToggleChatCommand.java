package dev.jackdaw1101.neon.Command.ToggleChat;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.Features.Player.ToggleChat.ChatToggleAPIImpl;
import dev.jackdaw1101.neon.API.Utils.CC;
import dev.jackdaw1101.neon.API.Utils.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class ToggleChatCommand implements CommandExecutor {

    private final Neon plugin;
    private final ChatToggleAPIImpl api;
    private final String toggleOnMessage;
    private final String toggleOffMessage;
    private final String noPermissionMessage;
    private final boolean permissionRequired;
    private final String requiredPermission;
    private final boolean useSound;
    private final String soundName;

    public ToggleChatCommand(Neon plugin) {
        this.plugin = plugin;
        this.api = new ChatToggleAPIImpl(plugin);

        // Load messages
        this.toggleOnMessage = ColorHandler.color(plugin.getMessageManager().getString("CHAT-ON"));
        this.toggleOffMessage = ColorHandler.color(plugin.getMessageManager().getString("CHAT-OFF"));
        this.noPermissionMessage = ColorHandler.color(plugin.getMessageManager().getString("NO-PERMISSION"));

        // Load settings
        this.permissionRequired = plugin.getSettings().getBoolean("CHAT-TOGGLE.REQUIRE-PERMISSION");
        this.requiredPermission = plugin.getPermissionManager().getString("CHAT-TOGGLE-USE");
        this.useSound = plugin.getSettings().getBoolean("NO-PERMISSION.USE-SOUND");
        this.soundName = plugin.getSettings().getString("NO-PERMISSION.SOUND");
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
            playNoPermissionSound(player);
            return true;
        }

        // Get current state before toggling
        boolean wasToggled = api.isChatToggled(player);

        // Toggle and handle the result asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                // Perform the toggle operation
                api.toggleChat(player);

                // Get the new state after toggling
                boolean isNowToggled = api.isChatToggled(player);
                boolean isdebug = plugin.getSettings().getBoolean("DEBUG-MODE");

                // Schedule the message to be sent on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (isNowToggled) {
                        player.sendMessage(toggleOnMessage);
                    } else {
                        player.sendMessage(toggleOffMessage);
                    }

                    if (isdebug) {
                        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "Chat toggle for " + player.getName() +
                            ": Before=" + wasToggled +
                            ", After=" + isNowToggled);
                                 }
                });
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ColorHandler.color("&cError toggling chat state!"));
                    plugin.getLogger().severe("Error toggling chat for " + player.getName() + ": " + e.getMessage());
                });
            }
        });

        return true;
    }

    private void playNoPermissionSound(Player player) {
        if (!useSound) return;

        try {
            if (plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                SoundUtil.playSound(player, soundName, 1.0f, 1.0f);
            } else if (plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                XSounds.playSound(player, soundName, 1.0f, 1.0f);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to play no-permission sound: " + e.getMessage());
        }
    }
}
