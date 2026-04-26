package dev.jackdaw1101.neon.commands;

import dev.jackdaw1101.neon.API.NeonAPI;
import dev.jackdaw1101.neon.API.modules.moderation.IAutoResponse;
import dev.jackdaw1101.neon.implementions.IAutoResponseImpl;
import dev.jackdaw1101.neon.utils.DebugUtil;
import dev.jackdaw1101.neon.utils.configs.ConfigFile;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.utils.sounds.ISound;
import dev.jackdaw1101.neon.utils.sounds.XSounds;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NeonReloadCommand implements CommandExecutor {
    private final Neon plugin;

    public NeonReloadCommand(Neon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission(plugin.getPermissionManager().getString("RELOAD"))) {
            String noPermissionMsg = plugin.getMessageManager().getString("NO-PERMISSION");
            sender.sendMessage(ColorHandler.color(noPermissionMsg != null ? noPermissionMsg : "&cYou don't have permission!"));

            if (sender instanceof Player) {
                playNoPermissionSound((Player) sender);
            }
            return true;
        }

        executeReload(sender);
        return true;
    }

    private void playNoPermissionSound(Player player) {
        try {
            boolean useSound = plugin.getSettings().getBoolean("NO-PERMISSION.USE-SOUND");
            if (!useSound) return;

            String soundName = plugin.getSettings().getString("NO-PERMISSION.SOUND");
            if (soundName == null || soundName.isEmpty()) return;

            boolean useISounds = plugin.getSettings().getBoolean("ISOUNDS-UTIL");
            boolean useXSounds = plugin.getSettings().getBoolean("XSOUNDS-UTIL");

            if (useISounds) {
                ISound.playSound(player, soundName, 1.0f, 1.0f);
            } else if (useXSounds) {
                XSounds.playSound(player, soundName, 1.0f, 1.0f);
            }
        } catch (Exception e) {
            DebugUtil.debugError("Failed to play no-permission sound: " + e.getMessage());
        }
    }

    public void executeReload(CommandSender sender) {
        boolean allReloaded = true;
        StringBuilder failedReloads = new StringBuilder();

        try {

            if (plugin.getLocales() != null) {
                if (plugin.getLocales().reloadLocales()) {
                    plugin.getLocales().reload();
                    DebugUtil.debug("Locales reloaded successfully");
                } else {
                    allReloaded = false;
                    failedReloads.append("Locales, ");
                }
            }
        } catch (Exception e) {
            allReloaded = false;
            failedReloads.append("Locales (error), ");
            DebugUtil.debugError("Failed to reload locales: " + e.getMessage());
            e.printStackTrace();
        }

        try {

            if (plugin.getSettings() != null) {
                if (plugin.getSettings().reload()) {
                    DebugUtil.debug("Settings reloaded successfully");
                } else {
                    allReloaded = false;
                    failedReloads.append("Settings, ");
                }
            }
        } catch (Exception e) {
            allReloaded = false;
            failedReloads.append("Settings (error), ");
            DebugUtil.debugError("Failed to reload settings: " + e.getMessage());
            e.printStackTrace();
        }

        try {

            if (plugin.getMessageManager() != null) {
                plugin.getMessageManager().reload();
                DebugUtil.debug("&aMessage manager reloaded successfully");
            }
        } catch (Exception e) {
            allReloaded = false;
            failedReloads.append("MessageManager, ");
            DebugUtil.debugError("Failed to reload message manager: " + e.getMessage());
            e.printStackTrace();
        }

        try {

            if (plugin.getPermissionManager() != null) {
                plugin.getPermissionManager().reload();
                DebugUtil.debug("Permission manager reloaded successfully");
            }
        } catch (Exception e) {
            allReloaded = false;
            failedReloads.append("PermissionManager, ");
            DebugUtil.debugError("Failed to reload permission manager: " + e.getMessage());
            e.printStackTrace();
        }

        try {

            if (plugin.getDiscordManager() != null) {
                plugin.getDiscordManager().reload();
                DebugUtil.debug("Discord manager reloaded successfully");
            }
        } catch (Exception e) {
            allReloaded = false;
            failedReloads.append("DiscordManager, ");
            DebugUtil.debugError("Failed to reload discord manager: " + e.getMessage());
            e.printStackTrace();
        }

        String reloadMessage;
        if (allReloaded) {
            reloadMessage = plugin.getMessageManager().getString("RELOADED-SUCCESSFULLY");
            if (reloadMessage == null) {
                reloadMessage = "&aPlugin reloaded successfully!";
            }
        } else {
            reloadMessage = plugin.getMessageManager().getString("RELOADED-PARTIALLY");
            if (reloadMessage == null) {
                reloadMessage = "&ePlugin reloaded partially. Check console for errors.";
            }

            String failed = failedReloads.length() > 0 ?
                    failedReloads.substring(0, failedReloads.length() - 2) : "Unknown";
            DebugUtil.debugError("Failed to reload: " + failed);
        }

        sender.sendMessage(ColorHandler.color(reloadMessage));

        String senderName = sender instanceof Player ? sender.getName() : "CONSOLE";
        DebugUtil.debug("Plugin reloaded by: " + senderName + " - Success: " + allReloaded);
    }
}