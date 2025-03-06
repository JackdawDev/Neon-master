package dev.jackdaw1101.neon;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.jackdaw1101.neon.API.NeonAPI;
import dev.jackdaw1101.neon.AddonHandler.AddonManager;
import dev.jackdaw1101.neon.Announcements.AnnouncementManager;
import dev.jackdaw1101.neon.AntiAdvertise.AntiLinkSystem;
import dev.jackdaw1101.neon.AntiCaps.AntiCapsSystem;
import dev.jackdaw1101.neon.AntiSpam.AntiSpamManager;
import dev.jackdaw1101.neon.AntiSpam.ListenerAntiSpam;
import dev.jackdaw1101.neon.AntiSwear.AntiSwearSystem;
import dev.jackdaw1101.neon.AntiSwear.SwearManager;
import dev.jackdaw1101.neon.AntiUniCode.ListenerAntiUnicode;
import dev.jackdaw1101.neon.AutoResponse.AutoResponse;
import dev.jackdaw1101.neon.Command.ChatClear.ClearChatCommand;
import dev.jackdaw1101.neon.Command.Logger.CommandLoggerListener;
import dev.jackdaw1101.neon.Command.NeonCommand;
import dev.jackdaw1101.neon.Command.tabcomp.NeonTabCompleter;
import dev.jackdaw1101.neon.Configurations.*;
import dev.jackdaw1101.neon.GrammerAPI.GrammerAPI;
import dev.jackdaw1101.neon.Manager.ChatFormat;
import dev.jackdaw1101.neon.Manager.JoinLeave.JoinLeaveListener;
import dev.jackdaw1101.neon.Manager.MOTD.WelcomeListener;
import dev.jackdaw1101.neon.Manager.MentionManager.ListenerMentions;
import dev.jackdaw1101.neon.Utils.Chat.CC;
import dev.jackdaw1101.neon.Utils.Core.DebugUtil;
import dev.jackdaw1101.neon.Utils.File.FileUtils;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.respark.licensegate.LicenseGate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public enum Loader {

    INSTANCE;

    final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY----- MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkCGmv68OnRyOxKXcZJFH 4031ImtxVx0bZ72ch/0Se4UFzoacZwmTUQJ+8QZfFDq0XXik2UFm7tjO/p1pizTk y/UCGpfe9mGsgcFVGWAPbX02QTH8A5VOr1AGwWkg1WL0KEBkGCD1px8SBDi+MQmt 6nvjlZNpk6qN6vl+0WUEVXgk0HzKOrCESJb+tZP8p//rw8xrKmxCjy69wHJre9Qw QF0CGQRuwKhjuQb8ch3BHNwUG7Ij1dC0F/pMMOw99xjKfTCUghEsu57znpGv/Ch+ AOGF9QH1FXVTDFAd2ltdz/qA0WGVSwIskgmZ879PHaulo+eNQEhvXZcd5yzJ41Wg HQIDAQAB -----END PUBLIC KEY-----";
    LicenseGate licenseGate = new LicenseGate("a1e45", PUBLIC_KEY);
    private License licenseManager;

    private String scope() {
        String Scope = "neon-dev";
        return Scope;
    }

    public void start(final Neon plugin) {
        String neonloader = "1.1";
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Started Fetching Local Neon Loader Libraries " + "(" + neonloader + ")...");
        try {
            Thread.sleep(7500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Successfully Fetched Local Neon Loader Libraries " + "(" + neonloader + ")...");
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Loading License.yml...");
        this.licenseManager = new License(plugin);
        File license = new File(plugin.getDataFolder(), "license.yml");

        try {
            ConfigUpdater.update(plugin, "license.yml", license);
        } catch (IOException e) {
            e.printStackTrace();
        }

        plugin.reloadConfig();
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Loaded License.yml");

        String licensekey = this.licenseManager.getLicense("LICENSE-KEY");

        LicenseGate.ValidationType result = licenseGate.verify(licensekey, scope());

        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Started Fetching Neon (" + plugin.getDescription().getVersion() + ")...");
        // Sleep
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long startTime = System.currentTimeMillis();
        if (result == LicenseGate.ValidationType.VALID) {
            // Enable
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Successfully Fetched Neon (" + plugin.getDescription().getVersion() + ")!");
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Loading Neon (" + plugin.getDescription().getVersion() + ")!");
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Successfully Loaded Neon (" + plugin.getDescription().getVersion() + ")!");


        } else if (result == LicenseGate.ValidationType.IP_LIMIT_EXCEEDED) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Unable To Fetch Neon (" + plugin.getDescription().getVersion() + "): Too Many IPs!");
            Bukkit.getScheduler().cancelTasks(plugin);
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        } else if (result == LicenseGate.ValidationType.CONNECTION_ERROR) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Unable To Fetch Neon (" + plugin.getDescription().getVersion() + "): Connection Error!");
            Bukkit.getScheduler().cancelTasks(plugin);
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        } else if (result == LicenseGate.ValidationType.NOT_FOUND) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Unable To Fetch Neon (" + plugin.getDescription().getVersion() + "): License Not Found!");
            Bukkit.getScheduler().cancelTasks(plugin);
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        } else if (result == LicenseGate.ValidationType.SERVER_ERROR) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Unable To Fetch Neon (" + plugin.getDescription().getVersion() + "): Server Error!");
            Bukkit.getScheduler().cancelTasks(plugin);
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        } else if (result == LicenseGate.ValidationType.LICENSE_SCOPE_FAILED) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Unable To Fetch Neon (" + plugin.getDescription().getVersion() + "): Couldn't Verify The Required Data!");
            Bukkit.getScheduler().cancelTasks(plugin);
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        } else if (result == LicenseGate.ValidationType.NOT_ACTIVE) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Unable To Fetch Neon (" + plugin.getDescription().getVersion() + "): Disabled License!");
            Bukkit.getScheduler().cancelTasks(plugin);
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        } else if (result == LicenseGate.ValidationType.FAILED_CHALLENGE) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Unable To Fetch Neon (" + plugin.getDescription().getVersion() + "): Failed Challenge!");
            Bukkit.getScheduler().cancelTasks(plugin);
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        } else {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Unable To Fetch Neon (" + plugin.getDescription().getVersion() + "): Error While Validating The License!");
            Bukkit.getScheduler().cancelTasks(plugin);
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }
    }
}

