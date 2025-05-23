package dev.jackdaw1101.neon;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.jackdaw1101.neon.API.modules.moderation.AntiSwearAPI;
import dev.jackdaw1101.neon.implementions.AntiSwearAPIImpl;
import dev.jackdaw1101.neon.API.modules.moderation.NeonJoinLeaveAPI;
import dev.jackdaw1101.neon.implementions.NeonJoinLeaveAPIImpl;
import dev.jackdaw1101.neon.API.modules.moderation.ChatToggleAPI;
import dev.jackdaw1101.neon.implementions.ChatToggleAPIImpl;
import dev.jackdaw1101.neon.database.ChatToggleDatabase;
import dev.jackdaw1101.neon.implementions.GrammarAPIImpl;
import dev.jackdaw1101.neon.API.NeonAPI;
import dev.jackdaw1101.neon.API.addons.AddonManager;
import dev.jackdaw1101.neon.integration.IntegrationHandler;
import dev.jackdaw1101.neon.modules.automated.AnnouncementManager;
import dev.jackdaw1101.neon.modules.moderation.AntiLinkSystem;
import dev.jackdaw1101.neon.modules.moderation.AntiCapsSystem;
import dev.jackdaw1101.neon.manager.moderation.AntiSpamManager;
import dev.jackdaw1101.neon.modules.moderation.AntiSpamSystem;
import dev.jackdaw1101.neon.modules.moderation.AntiSwearSystem;
import dev.jackdaw1101.neon.manager.moderation.SwearManager;
import dev.jackdaw1101.neon.modules.moderation.AntiUnicodeSystem;
import dev.jackdaw1101.neon.modules.automated.AutoResponse;
import dev.jackdaw1101.neon.modules.chat.listeners.ChatMuteListener;
import dev.jackdaw1101.neon.manager.chat.ChatMuteManager;
import dev.jackdaw1101.neon.API.command.CommandManager;
import dev.jackdaw1101.neon.manager.commands.AlertManager;
import dev.jackdaw1101.neon.modules.commands.MuteChatCommand;
import dev.jackdaw1101.neon.modules.chat.listeners.ToggleChatListener;
import dev.jackdaw1101.neon.modules.commands.ToggleChatCommand;
import dev.jackdaw1101.neon.modules.commands.ClearChatCommand;
import dev.jackdaw1101.neon.manager.commands.loggers.CommandLoggerManager;
import dev.jackdaw1101.neon.commands.NeonCommand;
import dev.jackdaw1101.neon.commands.NeonTabCompleter;
import dev.jackdaw1101.neon.database.togglechat.MongoDBChatToggleDatabase;
import dev.jackdaw1101.neon.database.togglechat.MySQLChatToggleDatabase;
import dev.jackdaw1101.neon.database.togglechat.SQLiteChatToggleDatabase;
import dev.jackdaw1101.neon.modules.automated.GrammarAPI;
import dev.jackdaw1101.neon.integration.bedwars1058.Bedwars1058Integration;
import dev.jackdaw1101.neon.integration.bedwars2023.Bedwars2023Integration;
import dev.jackdaw1101.neon.modules.chat.PerWorldChatSystem;
import dev.jackdaw1101.neon.modules.chat.ChatFormat;
import dev.jackdaw1101.neon.modules.player.GroupJoinMessageHandler;
import dev.jackdaw1101.neon.modules.player.GroupLeaveMessageHandler;
import dev.jackdaw1101.neon.modules.player.JoinLeaveListener;
import dev.jackdaw1101.neon.modules.player.WelcomeListener;
import dev.jackdaw1101.neon.modules.mention.MentionSystem;
import dev.jackdaw1101.neon.API.utilities.CC;
import dev.jackdaw1101.neon.utils.UpdateChecker;
import dev.jackdaw1101.neon.utils.DebugUtil;
import dev.jackdaw1101.neon.utils.FileUtils;
import dev.jackdaw1101.neon.utils.configs.ConfigFile;
import dev.jackdaw1101.neon.utils.metrics.MetricsManager;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
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
    private GroupJoinMessageHandler groupJoinMessageHandler;
    private GroupLeaveMessageHandler groupLeaveMessageHandler;

    private ConfigFile database;
    private AddonManager addonManager;
    private ToggleChatCommand toggleChatCommand;
    private ToggleChatListener chatListener;
    private AlertManager alertManager;
    private static Neon instance;
    private ChatToggleDatabase chatToggleDatabase;
    private ChatToggleAPI chatToggleAPI;
    private dev.jackdaw1101.neon.API.modules.grammar.GrammarAPI grammarAPI;
    private NeonJoinLeaveAPI neonJoinLeaveAPI;
    private AntiSwearAPI antiSwearAPI;
    private NeonAPI api;


    @Override
    public void onEnable() {
        instance = this;
        load();
    }

    private void loadChatLogLogFolder() {
        File logFolder = new File("plugins/Neon/Logs/Chat");
        boolean debugMode = getSettings().getBoolean("DEBUG-MODE");
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

    private void loadDataFolder() {
        File logFolder = new File("plugins/Neon/data");
        boolean debugMode = getSettings().getBoolean("DEBUG-MODE");
        if (!logFolder.exists()) {
            if (logFolder.mkdirs()) {
                if (debugMode) {
                    Bukkit.getConsoleSender().sendMessage(CC.GREEN + "[Neon-Debug] Data folder created successfully at " + logFolder.getPath());}
            } else {
                if (debugMode) {
                    Bukkit.getConsoleSender().sendMessage(CC.YELLOW + "[Neon-Debug] Data folder already exists.");
                }}
        } else {
            if (debugMode) {
                Bukkit.getConsoleSender().sendMessage(CC.YELLOW + "[Neon-Debug] Data folder already exists.");
            }}
    }

    private void createFolders() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File addonsFolder = new File(getDataFolder(), "Addons");
        if (!addonsFolder.exists()) {
            addonsFolder.mkdir();
        }
    }

    private void loadAntiAdLogFolder() {
        File logFolder = new File("plugins/Neon/Logs/AntiAdvertise");
        boolean debugMode = getSettings().getBoolean("DEBUG-MODE");
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
        File logFolder = new File("plugins/AstroLoader/Neon/Logs/AntiSwear");
        boolean debugMode = getSettings().getBoolean("DEBUG-MODE");
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
        boolean debugMode = getSettings().getBoolean("DEBUG-MODE");
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
        chatToggleDatabase.shutdown();
        long disableTime = System.currentTimeMillis() - stopTime;
        Bukkit.getConsoleSender().sendMessage(CC.BD_RED + "=============================================");
        Bukkit.getConsoleSender().sendMessage(CC.RED + "| \\ | |" + CC.BL_PURPLE + " || " + CC.BL_PURPLE + "Version: " + CC.L_PURPLE + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(CC.RED + "|  \\| |" + CC.BL_PURPLE + " || " + CC.BL_PURPLE + "Author: " + CC.L_PURPLE + "Jackdaw1101");
        Bukkit.getConsoleSender().sendMessage(CC.RED + "| |\\  |" + CC.BL_PURPLE + " || ");
        Bukkit.getConsoleSender().sendMessage(CC.RED + "|_| \\_|" + CC.BL_PURPLE + " || " + CC.BL_PURPLE + "Disabled in: " + CC.L_PURPLE + disableTime + "ms");
        Bukkit.getConsoleSender().sendMessage(CC.BD_RED + "=============================================");
        addonManager.getAllAddons().keySet().forEach(addonManager::unregisterAddon);
        if (api != null) {
            api.shutdown();
        }
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Loader] Terminated!");
    }


    public void load() {
        String debugversion = "1.1";
        long startTime = System.currentTimeMillis();

        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Loading Configurations...");

        settings = new ConfigFile(this, "settings.yml");
        messageManager = new ConfigFile(this, "messages.yml");
        discord = new ConfigFile(this, "discord.yml");
        permissionManager = new ConfigFile(this, "permissions.yml");
        locales= new ConfigFile(this, "locale.yml");
        messageManager = new ConfigFile(this, "messages.yml");
        database = new ConfigFile(this, "database.yml");
        messageManager.replacePlaceholdersInConfig(
            "{prefix}", messageManager.getString("PREFIX"),
            "{main_theme}", messageManager.getString("MAIN-THEME"),
            "{second_theme}", messageManager.getString("SECOND-THEME"),
            "{third_theme}", messageManager.getString("THIRD-THEME")
        );

        File serverDir = Bukkit.getServer().getWorldContainer();
        File pluginsDir = new File(serverDir, "plugins");

        File pluginDir = new File(pluginsDir, "Neon");

        if (!pluginDir.exists()) pluginDir.mkdirs();

        File settings = new File(getDataFolder(), "settings.yml");

        try {
            ConfigUpdater.update(this, "settings.yml", settings);
        } catch (IOException e) {
            e.printStackTrace();
        }

        reloadConfig();
        File db = new File(getDataFolder(), "database.yml");

        try {
            ConfigUpdater.update(this, "database.yml", db);
        } catch (IOException e) {
            e.printStackTrace();
        }

        reloadConfig();
        File messages = new File(getDataFolder(), "messages.yml");

        try {
            ConfigUpdater.update(this, "messages.yml", messages);
        } catch (IOException e) {
            e.printStackTrace();
        }

        reloadConfig();
        File permissions = new File(getDataFolder(), "permissions.yml");

        try {
            ConfigUpdater.update(this, "permissions.yml", permissions);
        } catch (IOException e) {
            e.printStackTrace();
        }

        reloadConfig();
        File discord = new File(getDataFolder(), "discord.yml");

        try {
            ConfigUpdater.update(this, "discord.yml", discord);
        } catch (IOException e) {
            e.printStackTrace();
        }

        reloadConfig();

        loadAntiAdLogFolder();
        loadCOmmandLOgger();
        loadDataFolder();
        createFolders();

        if (!checkConfigVersion()) {
            Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Unable To Load Configurations: Invalid Config Version");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Successfully Loaded Configurations!");

        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Loading API...");
        this.addonManager = new AddonManager(getLogger());
        this.api = new NeonAPI(this, addonManager);
        Bukkit.getServicesManager().register(NeonAPI.class, api, this, ServicePriority.Highest);
        Bukkit.getServicesManager().register(AddonManager.class, addonManager, this, ServicePriority.Highest);
        this.antiSwearAPI = new AntiSwearAPIImpl(this, swearManager);
        Bukkit.getServicesManager().register(AntiSwearAPI.class, antiSwearAPI, this, ServicePriority.Normal);
        chatToggleAPI = new ChatToggleAPIImpl(this);
        grammarAPI = new GrammarAPIImpl(this);
        neonJoinLeaveAPI = new NeonJoinLeaveAPIImpl(this);
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Starting API...");
        getServer().getServicesManager().register(NeonJoinLeaveAPI.class, neonJoinLeaveAPI, this, ServicePriority.Normal);
        getServer().getServicesManager().register(dev.jackdaw1101.neon.API.modules.grammar.GrammarAPI.class, grammarAPI, this, ServicePriority.Normal);
        getServer().getServicesManager().register(ChatToggleAPI.class, chatToggleAPI, this, ServicePriority.Normal);
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Successfully Loaded And Booted The API!");

        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Loading Debug Util...");
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
        String databaseType = getDatabaseManager().getString("DATABASE.TYPE").toLowerCase();

        switch (databaseType) {
            case "mysql":
                chatToggleDatabase = new MySQLChatToggleDatabase(this);
                break;
            case "mongodb":
                chatToggleDatabase = new MongoDBChatToggleDatabase(this);
                break;
            case "sqlite":
                chatToggleDatabase = new SQLiteChatToggleDatabase(this);
            default:
                chatToggleDatabase = new SQLiteChatToggleDatabase(this);
        }
        if (chatToggleDatabase != null) {
            chatToggleDatabase.initialize();
        } else {
            getLogger().severe("Failed to initialize chat toggle database! Falling back to SQLite.");
            chatToggleDatabase = new SQLiteChatToggleDatabase(this);
            chatToggleDatabase.initialize();
        }
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Registered Databases!");




        getCommand("togglechat").setExecutor(toggleChatCommand);
        this.alertManager = new AlertManager(this);
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Successfully Loaded Commands");

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
        getServer().getPluginManager().registerEvents(new AntiSpamSystem(this), this);
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
        getServer().getPluginManager().registerEvents(new ChatMuteListener(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Chat Mute.");
        }
        AlertManager alertManager = new AlertManager(this);
        getServer().getPluginManager().registerEvents(new AntiSwearSystem(this, alertManager, swearManager), this);        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Anti Swear Listener.");
        }
        getServer().getPluginManager().registerEvents(new GrammarAPI(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Grammar API.");
        }
        chatListener = new ToggleChatListener(this);
        getServer().getPluginManager().registerEvents(chatListener, this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Toggle Chat API.");
        }
        getServer().getPluginManager().registerEvents(new AntiCapsSystem(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Anti Caps System.");
        }
        getServer().getPluginManager().registerEvents(new AntiUnicodeSystem(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Anti Unicode System.");
        }
        getServer().getPluginManager().registerEvents(new MentionSystem(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Mentions System.");
        }
        groupJoinMessageHandler = new GroupJoinMessageHandler();
        groupLeaveMessageHandler = new GroupLeaveMessageHandler();
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Per Group Join Message System.");
        }
        getServer().getPluginManager().registerEvents(new PerWorldChatSystem(this), this);
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Per World Chat System..");
        }

        getServer().getPluginManager().registerEvents(new CommandLoggerManager(this), this);
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
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Successfully Loaded Events And Features");

        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Started Hooking into Plugins...");
        IntegrationHandler handler = new IntegrationHandler(this);
        handler.registerIntegration(new Bedwars1058Integration());
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Bedwars1058 Hook");
        }
        handler.registerIntegration(new Bedwars2023Integration());
        if (isdebug) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon-Debug] Loaded Bedwars2023 Hook");
        }
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Finished Hoooking into plugins!");


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
        boolean debugMode = getSettings().getBoolean("DEBUG-MODE");
        String dbtype = getDatabaseManager().getString("DATABASE.TYPE");

        long loadTime = System.currentTimeMillis() - startTime;
        Bukkit.getConsoleSender().sendMessage(CC.D_AQUA + "=============================================");
        Bukkit.getConsoleSender().sendMessage(CC.GREEN + "| \\ | |" + CC.YELLOW + " || " + CC.AQUA + "Version: " + CC.YELLOW + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(CC.GREEN + "|  \\| |" + CC.YELLOW + " || " + CC.AQUA + "Author: " + CC.YELLOW + "Jackdaw1101");
        Bukkit.getConsoleSender().sendMessage(CC.GREEN + "| |\\  |" + CC.YELLOW + " || ");
        Bukkit.getConsoleSender().sendMessage(CC.GREEN + "|_| \\_|" + CC.YELLOW + " || " + CC.AQUA + "Enabled in: " + CC.RED + loadTime + "ms");
        Bukkit.getConsoleSender().sendMessage(CC.GOLD + " ");
        Bukkit.getConsoleSender().sendMessage(CC.GOLD + "Load Information:");
        Bukkit.getConsoleSender().sendMessage(CC.AQUA + " * " + CC.YELLOW + "Chat Format: " + status);
        Bukkit.getConsoleSender().sendMessage(CC.AQUA + " * " + CC.YELLOW + "Database: " + CC.PINK + dbtype.toUpperCase());
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

        MetricsManager.initService(this);
        UpdateChecker updateChecker = new UpdateChecker(this, 124425);
        if (updateChecker.isUpdateRequired() && getSettings().getBoolean("UPDATE-SYSTEM.CHECK-UPDATE")) {
            String latestVersion = updateChecker.getUpdateVersion();
            updateChecker.sendUpdateMessage(latestVersion);

            updateChecker.autoUpdate();
        }
        if (!updateChecker.isUpdateRequired() && getSettings().getBoolean("UPDATE-SYSTEM.CHECK-UPDATE")) {
            Bukkit.getConsoleSender().sendMessage(CC.GREEN + "[Neon] No Updates Available!");
        }
    }

    private boolean checkConfigVersion() {
        int configVersion = getSettings().getInt("CONFIG-VERSION");

        if (configVersion != getLatestConfigVersion()) {
            return false;
        }

        return true;
    }


    public NeonAPI getNeonAPI() {
        return api;
    }

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

    public ConfigFile getDatabaseManager() {
        return this.database;
    }

    public ChatToggleAPI getChatToggleAPI() {
        return this.chatToggleAPI;
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

    public GroupJoinMessageHandler getPerGroupChat() {
        return groupJoinMessageHandler;
    }

    public GroupLeaveMessageHandler getPerGroupLeave() {
        return groupLeaveMessageHandler;
    }

    public static Neon getInstance() {
        if (instance == null) {
            instance = new Neon();
        }
        return instance;
    }
    public ConfigFile getLocales() {
        return locales;
    }


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

    //public SwearManager getSwearManager() {
    //    return swearManager;
    //}
}
