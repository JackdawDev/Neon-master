package dev.jackdaw1101.neon.command.togglechat;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.api.features.player.togglechat.ChatToggleAPIImpl;
import dev.jackdaw1101.neon.api.utils.CC;
import dev.jackdaw1101.neon.api.utils.ColorHandler;
import dev.jackdaw1101.neon.utils.isounds.SoundUtil;
import dev.jackdaw1101.neon.utils.isounds.XSounds;
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


        this.toggleOnMessage = ColorHandler.color(plugin.getMessageManager().getString("CHAT-ON"));
        this.toggleOffMessage = ColorHandler.color(plugin.getMessageManager().getString("CHAT-OFF"));
        this.noPermissionMessage = ColorHandler.color(plugin.getMessageManager().getString("NO-PERMISSION"));


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


        if (permissionRequired && !player.hasPermission(requiredPermission)) {
            player.sendMessage(noPermissionMessage);
            playNoPermissionSound(player);
            return true;
        }


        boolean wasToggled = api.isChatToggled(player);


        CompletableFuture.runAsync(() -> {
            try {

                api.toggleChat(player);


                boolean isNowToggled = api.isChatToggled(player);
                boolean isdebug = plugin.getSettings().getBoolean("DEBUG-MODE");


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
