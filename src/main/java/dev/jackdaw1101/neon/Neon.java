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
import dev.jackdaw1101.neon.Chat.ListenerMuteChat;
import dev.jackdaw1101.neon.Chat.Manager.ChatMuteManager;
import dev.jackdaw1101.neon.Command.API.CommandManager;
import dev.jackdaw1101.neon.Command.Alerts.AlertManager;
import dev.jackdaw1101.neon.Command.Chat.MuteChatCommand;
import dev.jackdaw1101.neon.Command.ToggleChat.Listener.ChatListener;
import dev.jackdaw1101.neon.Command.ToggleChat.ToggleChatCommand;
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
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class Neon extends JavaPlugin {

    private ConfigFile settings;
    private ConfigFile messageManager;
    private ConfigFile permissionManager;
    private ConfigFile discord;
    private ChatMuteManager chatMuteManager;
    private AntiSpamManager antiSpamManager;
    private AntiLinkSystem antilinksystem;
    private WelcomeListener welcomeListener;
    private SwearManager swearManager;
    private AnnouncementManager announcementManager;
    private ConfigFile locales;
    //private Database database;
    private NeonAPI neonAPI;
    private AddonManager addonManager;
    private ToggleChatCommand toggleChatCommand;
    private ChatListener chatListener;
    private AlertManager alertManager;

    @Override
    public void onEnable() {
        load();
    }

    private void loadChatLogLogFolder() {
        File logFolder = new File("plugins/Neon/Logs/Chat");
        boolean debugMode = getSettings().getBoolean("DEBUG-MODE");  // Default to true if not set
        if (!logFolder.exists()) {
            if (logFolder.mkdirs()) {
                if (debugMode) {
                    Bukkit.getConsoleSender().sendMessage(CC.GREEN + "[Neon-Debug] ChatLogs folder created successfully at " + logFolder.getPath());}
            } else {
                if (debugMode) {
                    Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon-Debug] Failed to create ChatLogs folder at " + logFolder.getPath());
                }}
        } else {
            if (debugMode) {
                Bukkit.getConsoleSender().sendMessage(CC.YELLOW + "[Neon-Debug] ChatLog folder already exists.");
            }}
    }

    private void loadAntiAdLogFolder() {
        File logFolder = new File("plugins/Neon/Logs/AntiAdvertise");
        boolean debugMode = getSettings().getBoolean("DEBUG-MODE");  // Default to true if not set
        if (!logFolder.exists()) {
            if (logFolder.mkdirs()) {
                if (debugMode) {
                    Bukkit.getConsoleSender().sendMessage(CC.GREEN + "[Neon-Debug] ChatLogs folder created successfully at " + logFolder.getPath());}
            } else {
                if (debugMode) {
                    Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon-Debug] Failed to create ChatLogs folder at " + logFolder.getPath());
                }}
        } else {
            if (debugMode) {
                Bukkit.getConsoleSender().sendMessage(CC.YELLOW + "[Neon-Debug] ChatLog folder already exists.");
            }}
    }


    private void loadAntiSwearLogFolder() {
        File logFolder = new File("plugins/Neon/Logs/AntiSwear");
        boolean debugMode = getSettings().getBoolean("DEBUG-MODE");  // Default to true if not set
        if (!logFolder.exists()) {
            if (logFolder.mkdirs()) {
                if (debugMode) {
                    getLogger().info("[Neon-Debug] AntiSwearLogs folder created successfully at " + logFolder.getPath());
                }} else {
                if (debugMode) {
                    getLogger().severe("[Neon-Debug] Failed to create AntiSwearLogs folder at " + logFolder.getPath());
                }}
        } else {
            if (debugMode) {
                getLogger().info("[Neon-Debug] AntiSwearLogs folder already exists.");
            }
        }
    }

    private void loadCOmmandLOgger() {
        File logFolder = new File("plugins/Neon/Logs/Commands");
        boolean debugMode = getSettings().getBoolean("DEBUG-MODE");  // Default to true if not set
        if (!logFolder.exists()) {
            if (logFolder.mkdirs()) {
                if (debugMode) {
                    getLogger().info("[Neon-Debug] AntiSwearLogs folder created successfully at " + logFolder.getPath());
                }} else {
                if (debugMode) {
                    getLogger().severe("[Neon-Debug] Failed to create AntiSwearLogs folder at " + logFolder.getPath());
                }}
        } else {
            if (debugMode) {
                getLogger().info("[Neon-Debug] AntiSwearLogs folder already exists.");
            }
        }
    }

    @Override
    public void onDisable() {
        unload();
    }

    private void unload() {
        long stopTime = System.currentTimeMillis();
        this.getNeonAPI().stopAPI();
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long disableTime = System.currentTimeMillis() - stopTime;
        Bukkit.getConsoleSender().sendMessage(CC.BD_RED + "=============================================");
        Bukkit.getConsoleSender().sendMessage(CC.RED + "| \\ | |" + CC.BL_PURPLE + " || " + CC.BL_PURPLE + "Version: " + CC.L_PURPLE + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(CC.RED + "|  \\| |" + CC.BL_PURPLE + " || " + CC.BL_PURPLE + "Author: " + CC.L_PURPLE + "Jackdaw1101");
        Bukkit.getConsoleSender().sendMessage(CC.RED + "| |\\  |" + CC.BL_PURPLE + " || ");
        Bukkit.getConsoleSender().sendMessage(CC.RED + "|_| \\_|" + CC.BL_PURPLE + " || " + CC.BL_PURPLE + "Disabled in: " + CC.L_PURPLE + disableTime + "ms");
        Bukkit.getConsoleSender().sendMessage(CC.BD_RED + "=============================================");
        //NeonLoader.Terminate();
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Terminated!");
    }


    public void load() {
        String debugversion = "1.1";
        long startTime = System.currentTimeMillis();

        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Loading Configurations...");
        loadAntiAdLogFolder();
        loadCOmmandLOgger();
        loadChatLogLogFolder();
        loadAntiSwearLogFolder();

        File serverDir = Bukkit.getServer().getWorldContainer();
        File pluginsDir = new File(serverDir, "plugins");
        String pluginName = "NeonLoader";
        File pluginDir = new File(pluginsDir, pluginName);

        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
        }
        //settings = new Settings(this);
        //database = new Database(this);
        //locales = new Locales(this);
        //this.messageManager = new Messages(this);
        settings = new ConfigFile("settings.yml");
        messageManager = new ConfigFile("messages.yml");
        discord = new ConfigFile("discord.yml");
        permissionManager = new ConfigFile("permissions.yml");
        locales= new ConfigFile("locale.yml");
        messageManager.replacePlaceholdersInConfig("{prefix}", getMessageManager().getString("PREFIX"), "{main_theme}", getMessageManager().getString("MAIN-THEME"), "{second_theme}", getMessageManager().getString("SECOND-THEME"), "{third_theme}", getMessageManager().getString("THIRD-THEME"));

        //this.discord = new Discord(this);
        //this.permissionManager = new Permissions(this);

        if (!checkConfigVersion()) {
            Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Unable To Load Configurations: Invalid Config Version");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Successfully Loaded Configurations!");

        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Loading Debug Util...");
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Fetching Debug Util " + "(" + debugversion + ")" + "...");
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Fetched Debug Util " + "(" + debugversion + ")" + ".");

        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Loading Commands...");
        boolean MuteChat = getSettings().getBoolean("COMMANDS.MUTE-CHAT");

        NeonCommand neonCommand = new NeonCommand(this);
        getCommand("neon").setExecutor(neonCommand);
        getCommand("neon").setTabCompleter(new NeonTabCompleter(neonCommand));
        getCommand("chatclear").setExecutor(new ClearChatCommand(this));
        if (MuteChat) {
            CommandManager.registerCommand(this, new MuteChatCommand(this));
        }
        toggleChatCommand = new ToggleChatCommand(this);
        getCommand("togglechat").setExecutor(toggleChatCommand);
        this.alertManager = new AlertManager(this);
        try {
            Thread.sleep(9);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Successfully Loaded Commands");

        //* Features (event) registeries
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Loading Features...");
        boolean isdebug = getSettings().getBoolean("DEBUG-MODE");
        boolean ISound = getSettings().getBoolean("ISOUNDS-UTIL");
        boolean XSound = getSettings().getBoolean("XSOUNDS-UTIL");
        String isoundversion = "0.0.1";
        String xsoundversion = "1.0";

        if (ISound) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Using ISound " + "("+ isoundversion +")" +" Util as Sound Handler!");
        }
        if (XSound) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Using XSound " + "("+ xsoundversion +")" +" Util as Sound Handler!");
        }

        welcomeListener = new WelcomeListener(this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded MOTD (welocome) API.");
        }
        this.antiSpamManager = new AntiSpamManager();
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Anti Spam Event API.");
        }
        getServer().getPluginManager().registerEvents(new ListenerAntiSpam(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Anti Spam Listener.");
        }
        getServer().getPluginManager().registerEvents(welcomeListener, this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Welcome Listener.");
        }
        announcementManager = new AnnouncementManager(this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Auto Announce Task.");
        }
        getServer().getPluginManager().registerEvents(new AutoResponse(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Auto Response Listener.");
        }
        this.chatMuteManager = new ChatMuteManager(this);
        getServer().getPluginManager().registerEvents(new ListenerMuteChat(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Chat Mute.");
        }
        AlertManager alertManager = new AlertManager(this);
        getServer().getPluginManager().registerEvents(new AntiSwearSystem(this, alertManager), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Anti Swear Listener.");
        }
        getServer().getPluginManager().registerEvents(new GrammerAPI(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Grammar API.");
        }
        chatListener = new ChatListener(toggleChatCommand);
        getServer().getPluginManager().registerEvents(chatListener, this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Toggle Chat API.");
        }
        getServer().getPluginManager().registerEvents(new AntiCapsSystem(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Anti Caps System.");
        }
        getServer().getPluginManager().registerEvents(new ListenerAntiUnicode(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Anti Unicode System.");
        }
        getServer().getPluginManager().registerEvents(new ListenerMentions(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Mentions System.");
        }
        // Neon Sound API
        Bukkit.getConsoleSender().sendMessage(CC.GRAY +"[SoundUtil] Loaded ISound for version " + SoundUtil.getVersion());

        getServer().getPluginManager().registerEvents(new CommandLoggerListener(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Command Logger.");
        }
        getServer().getPluginManager().registerEvents(new AntiLinkSystem(this, alertManager), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Anti Advertise Listener.");
        }
        getServer().getPluginManager().registerEvents(new ChatFormat(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Chat Format Manager.");
        }
        getServer().getPluginManager().registerEvents(new JoinLeaveListener(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Join/Leave Utils.");
        }
        this.swearManager = new SwearManager(this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Swear API.");
        }
        loadAntiSwearLogFolder();
        loadChatLogLogFolder();
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Log Files.");
        }
        try {
            Thread.sleep(75);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Successfully Loaded Events And Features");

        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Loading API...");
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Starting API...");
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Successfully Loaded And Booted The API!");

        // load message
        boolean chatFormatEnabled = getSettings().getBoolean("CHAT_FORMAT_ENABLED");
        boolean hover = getSettings().getBoolean("HOVER_ENABLED");
        boolean clickevent = getSettings().getBoolean("CLICK_EVENT_ENABLED");
        boolean chatinconsole = getSettings().getBoolean("CHAT-IN-CONSOLE");
        boolean logchat =getSettings().getBoolean("LOG-CHAT");

        String status = chatFormatEnabled ? CC.GREEN + "true" : CC.RED + "false";
        String fs = hover ? CC.GREEN + "true" : CC.RED + "false";
        String dd = clickevent ? CC.GREEN + "true" : CC.RED + "false";
        String sl = chatinconsole ? CC.GREEN + "true" : CC.RED + "false";
        String lkg = logchat ? CC.GREEN + "true" : CC.RED + "false";

        boolean debugmode = getSettings().getBoolean("DEBUG-MODE");

        String vaziat = debugmode ? CC.GREEN + "true " + CC.GRAY + "(" + debugversion + CC.GRAY + ")" : CC.RED + "false";
        boolean debugMode = getSettings().getBoolean("DEBUG-MODE");  // Default to true if not set

        long loadTime = System.currentTimeMillis() - startTime;
        Bukkit.getConsoleSender().sendMessage(CC.D_AQUA + "=============================================");
        Bukkit.getConsoleSender().sendMessage(CC.GREEN + "| \\ | |" + CC.YELLOW + " || " + CC.AQUA + "Version: " + CC.YELLOW + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(CC.GREEN + "|  \\| |" + CC.YELLOW + " || " + CC.AQUA + "Author: " + CC.YELLOW + "Jackdaw1101");
        Bukkit.getConsoleSender().sendMessage(CC.GREEN + "| |\\  |" + CC.YELLOW + " || ");
        Bukkit.getConsoleSender().sendMessage(CC.GREEN + "|_| \\_|" + CC.YELLOW + " || " + CC.AQUA + "Enabled in: " + CC.RED + loadTime + "ms");
        Bukkit.getConsoleSender().sendMessage(CC.GOLD + " ");
        Bukkit.getConsoleSender().sendMessage(CC.GOLD + "Load Information:");
        Bukkit.getConsoleSender().sendMessage(CC.AQUA + " * " + CC.YELLOW + "Chat Format: " + status);
        if (debugMode) {
            Bukkit.getConsoleSender().sendMessage(CC.DARK_AQUA + "  * " + CC.YELLOW + "Hover: " + fs);
            Bukkit.getConsoleSender().sendMessage(CC.DARK_AQUA + "  * " + CC.YELLOW + "Click Event: " + dd);
            Bukkit.getConsoleSender().sendMessage(CC.DARK_AQUA + "  * " + CC.YELLOW + "Chat In Console: " + sl);
            Bukkit.getConsoleSender().sendMessage(CC.DARK_AQUA + "  * " + CC.YELLOW + "Log Chat: " + lkg);
            Bukkit.getConsoleSender().sendMessage(CC.DARK_AQUA + "  ");
        }
        if (ISound) {
            Bukkit.getConsoleSender().sendMessage(CC.AQUA + " * " + CC.YELLOW + "Sound Util: " + CC.BLUE + "ISound");
            if (debugmode) {
                Bukkit.getConsoleSender().sendMessage(CC.DARK_AQUA + "  * " + CC.YELLOW + "Version: " + CC.GRAY + isoundversion);
                Bukkit.getConsoleSender().sendMessage(CC.GOLD + " ");
            }
        }
        if (XSound) {
            Bukkit.getConsoleSender().sendMessage(CC.AQUA + " * " + CC.YELLOW + "Sound Util: " + CC.BLUE + "XSound");
            if (debugmode) {
                Bukkit.getConsoleSender().sendMessage(CC.DARK_AQUA + "  * " + CC.YELLOW + "Version: " + CC.GRAY + xsoundversion);
            }
        }
        Bukkit.getConsoleSender().sendMessage(CC.AQUA + " * " + CC.YELLOW + "Debug Mode: " + vaziat);
        Bukkit.getConsoleSender().sendMessage(CC.AQUA + " * " + CC.YELLOW + "Loaded Configurations: ");
        List<String> ymlFiles = FileUtils.getYmlFiles(getDataFolder());
        for (String fileName : ymlFiles) {
            Bukkit.getConsoleSender().sendMessage(CC.RED + "  * " + CC.GREEN + fileName);
        }
        Bukkit.getConsoleSender().sendMessage(CC.DARK_AQUA + "  ");
        Bukkit.getConsoleSender().sendMessage(CC.D_AQUA + "=============================================");
        DebugUtil.checkDebug(this);
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Done!");
    }

    private boolean checkConfigVersion() {
        int configVersion = getSettings().getInt("CONFIG-VERSION");

        if (configVersion != getLatestConfigVersion()) {
            return false;
        }

        return true;
    }

    // GLOBAL API
    public NeonAPI getNeonAPI() {
        return neonAPI;
    }
    // LOCAL API
    public AntiSpamManager getAntiSpamManager() {
        return this.antiSpamManager;
    }

    public ConfigFile getMessageManager() {
        return this.messageManager;
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    public ConfigFile getPermissionManager() {
        return this.permissionManager;
    }

    public ConfigFile getDiscordManager() {
        return this.discord;
    }

    private int getLatestConfigVersion() {
        return 1;
    }

    public ConfigFile getSettings() {
        return settings;
    }

    //public Database getDatabase() {
    //    return database;
    //}

    public ConfigFile getLocales() {
        return locales;
    }

    // papi hooker
    private boolean isPlaceholderAPIInstalled() {
        Plugin placeholderAPI = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        return placeholderAPI != null && placeholderAPI.isEnabled();
    }

    private boolean isLuckpermsInstalled() {
        Plugin LP = getServer().getPluginManager().getPlugin("LuckPerms");
        return LP != null && LP.isEnabled();
    }

    public ChatMuteManager getChatMuteManager() {
        return chatMuteManager;
    }

    public SwearManager getSwearManager() {
        return swearManager;
    }
}
