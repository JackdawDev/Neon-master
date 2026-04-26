package dev.jackdaw1101.neon;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.jackdaw1101.neon.API.NeonAPI;
import dev.jackdaw1101.neon.API.addons.AddonManager;
import dev.jackdaw1101.neon.API.command.CommandManager;
import dev.jackdaw1101.neon.API.modules.grammar.IGrammar;
import dev.jackdaw1101.neon.API.modules.moderation.IAntiSwear;
import dev.jackdaw1101.neon.API.modules.moderation.IChatToggle;
import dev.jackdaw1101.neon.API.modules.moderation.ILogins;
import dev.jackdaw1101.neon.API.utilities.CC;
import dev.jackdaw1101.neon.commands.NeonCommand;
import dev.jackdaw1101.neon.commands.NeonTabCompleter;
import dev.jackdaw1101.neon.database.ChatToggleDatabase;
import dev.jackdaw1101.neon.database.togglechat.MongoDBChatToggleDatabase;
import dev.jackdaw1101.neon.database.togglechat.MySQLChatToggleDatabase;
import dev.jackdaw1101.neon.database.togglechat.SQLiteChatToggleDatabase;
import dev.jackdaw1101.neon.implementions.IAntiSwearImpl;
import dev.jackdaw1101.neon.implementions.IChatToggleImpl;
import dev.jackdaw1101.neon.implementions.IGrammarImpl;
import dev.jackdaw1101.neon.implementions.ILoginsImpl;
import dev.jackdaw1101.neon.integration.IntegrationHandler;
import dev.jackdaw1101.neon.integration.bedwars1058.Bedwars1058Integration;
import dev.jackdaw1101.neon.integration.bedwars2023.Bedwars2023Integration;
import dev.jackdaw1101.neon.manager.chat.ChatMuteManager;
import dev.jackdaw1101.neon.manager.commands.AlertManager;
import dev.jackdaw1101.neon.manager.commands.loggers.CommandLoggerManager;
import dev.jackdaw1101.neon.manager.moderation.AntiSpamManager;
import dev.jackdaw1101.neon.manager.moderation.SwearManager;
import dev.jackdaw1101.neon.modules.automated.AnnouncementManager;
import dev.jackdaw1101.neon.modules.automated.AutoResponse;
import dev.jackdaw1101.neon.modules.automated.GrammarAPI;
import dev.jackdaw1101.neon.modules.chat.ChatFormat;
import dev.jackdaw1101.neon.modules.chat.PerWorldChatSystem;
import dev.jackdaw1101.neon.modules.chat.bubblechat.PopUpBubbleChat;
import dev.jackdaw1101.neon.modules.chat.listeners.ChatMuteListener;
import dev.jackdaw1101.neon.modules.chat.listeners.ToggleChatListener;
import dev.jackdaw1101.neon.modules.commands.ClearChatCommand;
import dev.jackdaw1101.neon.modules.commands.MuteChatCommand;
import dev.jackdaw1101.neon.modules.commands.ToggleChatCommand;
import dev.jackdaw1101.neon.modules.mention.MentionSystem;
import dev.jackdaw1101.neon.modules.moderation.*;
import dev.jackdaw1101.neon.modules.player.GroupJoinMessageHandler;
import dev.jackdaw1101.neon.modules.player.GroupLeaveMessageHandler;
import dev.jackdaw1101.neon.modules.player.LoginsListener;
import dev.jackdaw1101.neon.modules.player.WelcomeListener;
import dev.jackdaw1101.neon.utils.DebugUtil;
import dev.jackdaw1101.neon.utils.FileUtils;
import dev.jackdaw1101.neon.utils.UpdateChecker;
import dev.jackdaw1101.neon.utils.VersionHandler;
import dev.jackdaw1101.neon.utils.configs.ConfigFile;
import dev.jackdaw1101.neon.utils.metrics.MetricsManager;
import dev.jackdaw1101.neon.utils.sounds.ISound;
import dev.jackdaw1101.neon.utils.sounds.XSounds;
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

    private static Neon instance;

    private ConfigFile settings;
    private ConfigFile messageManager;
    private ConfigFile permissionManager;
    private ConfigFile discord;
    private ConfigFile locales;
    private ConfigFile database;

    private ChatMuteManager chatMuteManager;
    private AntiSpamManager antiSpamManager;
    private SwearManager swearManager;
    private AnnouncementManager announcementManager;
    private AddonManager addonManager;
    private AlertManager alertManager;

    private WelcomeListener welcomeListener;
    private GroupJoinMessageHandler groupJoinMessageHandler;
    private GroupLeaveMessageHandler groupLeaveMessageHandler;
    private ToggleChatListener chatListener;
    private LoginsListener loginsListener;

    private AntiLinkSystem antiLinkSystem;
    private PopUpBubbleChat popUpBubbleChat;

    private ToggleChatCommand toggleChatCommand;

    private ChatToggleDatabase chatToggleDatabase;

    private IChatToggle iChatToggle;
    private IGrammar iGrammar;
    private ILogins iLogins;
    private IAntiSwear iAntiSwear;
    private NeonAPI api;

    private VersionHandler versionHandler;

    @Override
    public void onEnable() {
        instance = this;

        loadConfigurations();
        load();
        initializeVersionHandler();

    }

    @Override
    public void onDisable() {
        unload();
    }

    private boolean initializeVersionHandler() {
        versionHandler = new VersionHandler(this);

        if (!versionHandler.performVersionCheck()) {
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        if (versionHandler.isFolia()) {
            displayFoliaErrorMessage();
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        if (versionHandler.getVersionStatus() == VersionHandler.VersionStatus.UNSTABLE) {
            DebugUtil.debugInfo("Running on unstable version - some features may not work correctly");
        }

        return true;
    }

    private void displayFoliaErrorMessage() {
        DebugUtil.debugNoPrefix(CC.translate("&c&l✘ UNSUPPORTED SOFTWARE ✘"));
        DebugUtil.debugNoPrefix(CC.translate("&7Software: &cFolia"));
        DebugUtil.debugNoPrefix(CC.translate("&cFolia support is not added just Yet!"));
        DebugUtil.debugNoPrefix(CC.translate("&cit will be added soon!"));
    }

    public void load() {
        long startTime = System.currentTimeMillis();

        if (!checkConfigVersion()) {
            DebugUtil.debug(CC.RED + "[Neon] Unable To Load Configurations: Invalid Config Version");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        createDirectories();

        initializeApi();

        String debugVersion = DebugUtil.getDebugVersion(getNeonAPI().getNeonVersion());

        initializeDatabase();

        registerCommands();

        registerEventsAndFeatures();

        initializeIntegrations();

        displayLoadInformation(startTime, debugVersion);

        performStartupTasks();
    }

    private void loadConfigurations() {
        ConfigFile.debug(CC.GRAY + "[Neon] Loading Configurations...");

        settings = new ConfigFile(this, "settings.yml");
        messageManager = new ConfigFile(this, "messages.yml");
        discord = new ConfigFile(this, "discord.yml");
        permissionManager = new ConfigFile(this, "permissions.yml");
        locales = new ConfigFile(this, "locale.yml");
        database = new ConfigFile(this, "database.yml");

        messageManager.replacePlaceholdersInConfig(
                "{prefix}", messageManager.getString("PREFIX"),
                "{main_theme}", messageManager.getString("MAIN-THEME"),
                "{second_theme}", messageManager.getString("SECOND-THEME"),
                "{third_theme}", messageManager.getString("THIRD-THEME")
        );

        updateConfigFiles();

        reloadConfig();

        DebugUtil.debug(CC.GRAY + "[Neon] Successfully Loaded Configurations!");
    }

    private void updateConfigFiles() {
        String[] configFiles = {"settings.yml", "database.yml", "messages.yml", "permissions.yml", "discord.yml"};

        for (String configFile : configFiles) {
            File configFileObj = new File(getDataFolder(), configFile);
            try {
                ConfigUpdater.update(this, configFile, configFileObj);
            } catch (IOException e) {
                e.printStackTrace();
            }
            reloadConfig();
        }
    }

    private void createDirectories() {
        createPluginDirectories();
        createLogDirectories();
    }

    private void createPluginDirectories() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
            DebugUtil.debug("&a[Neon-Debug] Created plugin folder!");
        }

        File addonsFolder = new File(getDataFolder(), "Addons");
        if (!addonsFolder.exists()) {
            addonsFolder.mkdir();
            DebugUtil.debug("&7[Neon-Debug] Created addons folder!");
        }

        File dataFolder = new File("plugins/Neon/data");
        if (!dataFolder.exists() && dataFolder.mkdirs()) {
            DebugUtil.debug(CC.GREEN + "[Neon-Debug] Data folder created successfully!");
        }
    }

    private void createLogDirectories() {
        createLogFolder("Logs/Chat", "ChatLogs");
        createLogFolder("Logs/AntiAdvertise", "AntiAdvertiseLogs");
        createLogFolder("Logs/AntiSwear", "AntiSwearLogs");
        createLogFolder("Logs/Commands", "CommandLogs");
    }

    private void createLogFolder(String path, String logType) {
        File logFolder = new File("plugins/Neon/" + path);
        boolean debugMode = getSettings().getBoolean("DEBUG-MODE");

        if (!logFolder.exists()) {
            if (logFolder.mkdirs() && debugMode) {
                DebugUtil.debug(CC.GREEN + "[Neon-Debug] " + logType + " folder created successfully!");
            }
        } else if (debugMode) {
            DebugUtil.debug(CC.YELLOW + "[Neon-Debug] " + logType + " folder already exists.");
        }
    }

    private void initializeApi() {
        DebugUtil.debug(CC.GRAY + "[Neon] Loading API...");

        this.addonManager = new AddonManager(getLogger());
        this.api = new NeonAPI(this, addonManager);

        Bukkit.getServicesManager().register(NeonAPI.class, api, this, ServicePriority.Highest);
        Bukkit.getServicesManager().register(AddonManager.class, addonManager, this, ServicePriority.Highest);

        this.iAntiSwear = new IAntiSwearImpl(this, swearManager);
        this.iChatToggle = new IChatToggleImpl(this);
        this.iGrammar = new IGrammarImpl(this);
        this.iLogins = new ILoginsImpl(this);

        registerImplementations();

        DebugUtil.debug(CC.GRAY + "[Neon] Successfully Loaded And Booted The API!");
    }

    private void registerImplementations() {
        getServer().getServicesManager().register(IAntiSwear.class, iAntiSwear, this, ServicePriority.Normal);
        getServer().getServicesManager().register(IChatToggle.class, iChatToggle, this, ServicePriority.Normal);
        getServer().getServicesManager().register(IGrammar.class, iGrammar, this, ServicePriority.Normal);
        getServer().getServicesManager().register(ILogins.class, iLogins, this, ServicePriority.Normal);
    }

    private void initializeDatabase() {
        DebugUtil.debug(CC.GRAY + "[Neon] Initializing Database...");

        String databaseType = getDatabaseManager().getString("DATABASE.TYPE").toLowerCase();

        chatToggleDatabase = createDatabaseInstance(databaseType);

        if (chatToggleDatabase == null) {
            DebugUtil.debugError("Failed to initialize chat toggle database! Falling back to SQLite.");
            chatToggleDatabase = new SQLiteChatToggleDatabase(this);
        }

        chatToggleDatabase.initialize();

        DebugUtil.debug(CC.GRAY + "[Neon] Registered Databases!");
    }

    private ChatToggleDatabase createDatabaseInstance(String databaseType) {
        switch (databaseType) {
            case "mysql":
                DebugUtil.debug("&7[Neon-Debug] registered database on MySQL!");
                return new MySQLChatToggleDatabase(this);
            case "mongodb":
                DebugUtil.debug("&7[Neon-Debug] registered database on MongoDB!");
                return new MongoDBChatToggleDatabase(this);
            case "sqlite":
                DebugUtil.debug("&7[Neon-Debug] registered database on SQLite!");
                return new SQLiteChatToggleDatabase(this);
            default:
                return null;
        }
    }

    private void registerCommands() {
        DebugUtil.debug(CC.GRAY + "[Neon] Loading Commands...");

        NeonCommand neonCommand = new NeonCommand(this);
        getCommand("neon").setExecutor(neonCommand);
        getCommand("neon").setTabCompleter(new NeonTabCompleter(neonCommand));

        getCommand("chatclear").setExecutor(new ClearChatCommand(this));

        if (getSettings().getBoolean("COMMANDS.MUTE-CHAT")) {
            CommandManager.registerCommand(this, new MuteChatCommand(this));
        }

        toggleChatCommand = new ToggleChatCommand(this);
        getCommand("togglechat").setExecutor(toggleChatCommand);

        this.alertManager = new AlertManager(this);

        DebugUtil.debug(CC.GRAY + "[Neon] Successfully Loaded Commands");
    }

    private void registerEventsAndFeatures() {
        DebugUtil.debug(CC.GRAY + "[Neon] Loading Features...");

        boolean isDebug = getSettings().getBoolean("DEBUG-MODE");

        initializeSoundUtilities();

        this.antiSpamManager = new AntiSpamManager();
        this.chatMuteManager = new ChatMuteManager(this);
        this.swearManager = new SwearManager(this);
        this.announcementManager = new AnnouncementManager(this);

        registerListeners(isDebug);

        initializeFeatures(isDebug);

        DebugUtil.debug(CC.GRAY + "[Neon] Successfully Loaded Events And Features");
    }

    private void initializeSoundUtilities() {
        boolean iSound = getSettings().getBoolean("ISOUNDS-UTIL");
        boolean xSound = getSettings().getBoolean("XSOUNDS-UTIL");

        if (iSound) {
            DebugUtil.debug(CC.GRAY + "[Neon] Using ISound (" + ISound.getUtilVersion() + ") Util as Sound Handler!");
        }
        if (xSound) {
            DebugUtil.debug(CC.GRAY + "[Neon] Using XSound (" + XSounds.getUtilVersion() + ") Util as Sound Handler!");
        }
    }

    private void registerListeners(boolean isDebug) {
        welcomeListener = new WelcomeListener(this);
        registerListener(welcomeListener, "Welcome Listener", isDebug);

        loginsListener = new LoginsListener(this);
        registerListener(loginsListener, "Join/Leave Utils", isDebug);

        registerListener(new AntiSpamSystem(this), "Anti Spam Listener", isDebug);
        registerListener(new AntiSwearSystem(this, alertManager, swearManager), "Anti Swear Listener", isDebug);
        registerListener(new AntiCapsSystem(this), "Anti Caps System", isDebug);
        registerListener(new AntiUnicodeSystem(this), "Anti Unicode System", isDebug);

        registerListener(new AutoResponse(this), "Auto Response Listener", isDebug);
        registerListener(new ChatMuteListener(this), "Chat Mute", isDebug);
        registerListener(chatListener = new ToggleChatListener(this), "Toggle Chat API", isDebug);
        registerListener(new GrammarAPI(this), "Grammar API", isDebug);
        registerListener(new MentionSystem(this), "Mentions System", isDebug);
        registerListener(new PerWorldChatSystem(this), "Per World Chat System", isDebug);
        registerListener(new ChatFormat(this), "Chat Format Manager", isDebug);

        registerListener(new CommandLoggerManager(this), "Command Logger", isDebug);
        registerListener(new AntiLinkSystem(this, alertManager), "Anti Advertise Listener", isDebug);

        groupJoinMessageHandler = new GroupJoinMessageHandler();
        groupLeaveMessageHandler = new GroupLeaveMessageHandler();

        if (isDebug) {
            DebugUtil.debug(CC.GRAY + "[Neon-Debug] Loaded Per Group Join Message System.");
        }
    }

    private void registerListener(org.bukkit.event.Listener listener, String name, boolean isDebug) {
        getServer().getPluginManager().registerEvents(listener, this);
        if (isDebug) {
            DebugUtil.debug(CC.GRAY + "[Neon-Debug] Loaded " + name + ".");
        }
    }

    private void initializeFeatures(boolean isDebug) {
        popUpBubbleChat = new PopUpBubbleChat(this);
        if (isDebug) {
            DebugUtil.debug(CC.GRAY + "[Neon-Debug] Pop Up Bubble Chat Api Loaded.");
        }

        antiLinkSystem = new AntiLinkSystem(this, alertManager);

        if (isDebug) {
            DebugUtil.debug(CC.GRAY + "[Neon-Debug] Loaded Swear manager.");
        }
    }

    private void initializeIntegrations() {
        DebugUtil.debug(CC.GRAY + "[Neon] Started Hooking into Plugins...");

        IntegrationHandler handler = new IntegrationHandler(this);
        handler.registerIntegration(new Bedwars1058Integration());
        DebugUtil.debug(CC.GRAY + "[Neon-Debug] Loaded Bedwars1058 Hook");

        handler.registerIntegration(new Bedwars2023Integration());
        DebugUtil.debug(CC.GRAY + "[Neon-Debug] Loaded Bedwars2023 Hook");

        DebugUtil.debug(CC.GRAY + "[Neon] Finished Hooking into plugins!");
    }

    private void displayLoadInformation(long startTime, String debugVersion) {
        boolean isDebug = DebugUtil.isDebugEnabled();
        boolean iSound = getSettings().getBoolean("ISOUNDS-UTIL");
        boolean xSound = getSettings().getBoolean("XSOUNDS-UTIL");
        boolean chatFormatEnabled = getSettings().getBoolean("CHAT_FORMAT_ENABLED");
        boolean hover = getSettings().getBoolean("HOVER_ENABLED");
        boolean clickEvent = getSettings().getBoolean("CLICK_EVENT_ENABLED");
        boolean chatInConsole = getSettings().getBoolean("CHAT-IN-CONSOLE");
        boolean logChat = getSettings().getBoolean("LOG-CHAT");

        long loadTime = System.currentTimeMillis() - startTime;
        String databaseType = getDatabaseManager().getString("DATABASE.TYPE");

        DebugUtil.debug(CC.D_AQUA + "=============================================");
        DebugUtil.debug(CC.GREEN + "| \\ | |" + CC.YELLOW + " || " + CC.AQUA + "Version: " + CC.YELLOW + getDescription().getVersion());
        DebugUtil.debug(CC.GREEN + "|  \\| |" + CC.YELLOW + " || " + CC.AQUA + "Author: " + CC.YELLOW + "Jackdaw1101");
        DebugUtil.debug(CC.GREEN + "| |\\  |" + CC.YELLOW + " || ");
        DebugUtil.debug(CC.GREEN + "|_| \\_|" + CC.YELLOW + " || " + CC.AQUA + "Enabled in: " + CC.RED + loadTime + "ms");
        DebugUtil.debug(CC.GOLD + " ");
        DebugUtil.debug(CC.GOLD + "Load Information:");
        DebugUtil.debug(CC.AQUA + " * " + CC.YELLOW + "Chat Format: " + (chatFormatEnabled ? CC.GREEN + "true" : CC.RED + "false"));
        DebugUtil.debug(CC.AQUA + " * " + CC.YELLOW + "Database: " + CC.PINK + databaseType.toUpperCase());

        if (isDebug) {
            DebugUtil.debug(CC.DARK_AQUA + "  * " + CC.YELLOW + "Hover: " + (hover ? CC.GREEN + "true" : CC.RED + "false"));
            DebugUtil.debug(CC.DARK_AQUA + "  * " + CC.YELLOW + "Click Event: " + (clickEvent ? CC.GREEN + "true" : CC.RED + "false"));
            DebugUtil.debug(CC.DARK_AQUA + "  * " + CC.YELLOW + "Chat In Console: " + (chatInConsole ? CC.GREEN + "true" : CC.RED + "false"));
            DebugUtil.debug(CC.DARK_AQUA + "  * " + CC.YELLOW + "Log Chat: " + (logChat ? CC.GREEN + "true" : CC.RED + "false"));
            DebugUtil.debug(CC.DARK_AQUA + "  ");
        }

        if (iSound) {
            DebugUtil.debug(CC.AQUA + " * " + CC.YELLOW + "Sound Util: " + CC.BLUE + "ISound");
            if (isDebug) {
                DebugUtil.debug(CC.DARK_AQUA + "  * " + CC.YELLOW + "Version: " + CC.GRAY + ISound.getUtilVersion());
                DebugUtil.debug(CC.GOLD + " ");
            }
        }

        if (xSound) {
            DebugUtil.debug(CC.AQUA + " * " + CC.YELLOW + "Sound Util: " + CC.BLUE + "XSound");
            if (isDebug) {
                DebugUtil.debug(CC.DARK_AQUA + "  * " + CC.YELLOW + "Version: " + CC.GRAY + XSounds.getUtilVersion());
            }
        }

        DebugUtil.debug(CC.AQUA + " * " + CC.YELLOW + "Debug Mode: " + (isDebug ? CC.GREEN + "true " + CC.GRAY + "(" + debugVersion + ")" : CC.RED + "false"));
        DebugUtil.debug(CC.AQUA + " * " + CC.YELLOW + "Loaded Configurations: ");

        List<String> ymlFiles = FileUtils.getYmlFiles(getDataFolder());
        for (String fileName : ymlFiles) {
            DebugUtil.debug(CC.RED + "  * " + CC.GREEN + fileName);
        }

        DebugUtil.debug(CC.DARK_AQUA + "  ");
        DebugUtil.debug(CC.D_AQUA + "=============================================");
    }

    private void performStartupTasks() {
        DebugUtil.checkLoadedClasses(this);
        DebugUtil.debug(CC.GRAY + "[Neon] Done!");

        MetricsManager.initService(this);

        if (getSettings().getBoolean("UPDATE-SYSTEM.CHECK-UPDATE")) {
            checkForUpdates();
        }
    }

    private void checkForUpdates() {
        UpdateChecker updateChecker = new UpdateChecker(this, 124425);

        if (updateChecker.isUpdateRequired()) {
            String latestVersion = updateChecker.getUpdateVersion();
            updateChecker.sendUpdateMessage(latestVersion);
            updateChecker.autoUpdate();
        } else {
            DebugUtil.debug(CC.GREEN + "[Neon] No Updates Available!");
        }
    }

    private void unload() {
        long stopTime = System.currentTimeMillis();

        if (chatToggleDatabase != null) {
            chatToggleDatabase.shutdown();
        }

        if (announcementManager != null) {
            announcementManager.shutdown();
        }

        if (addonManager != null) {
            addonManager.getAllAddons().keySet().forEach(addonManager::unregisterAddon);
        }

        if (popUpBubbleChat != null) {
            popUpBubbleChat.removeAllHolograms();
        }

        if (api != null) {
            api.shutdown();
        }

        long disableTime = System.currentTimeMillis() - stopTime;

        DebugUtil.debug(CC.BD_RED + "=============================================");
        DebugUtil.debug(CC.RED + "| \\ | |" + CC.BL_PURPLE + " || " + CC.BL_PURPLE + "Version: " + CC.L_PURPLE + getDescription().getVersion());
        DebugUtil.debug(CC.RED + "|  \\| |" + CC.BL_PURPLE + " || " + CC.BL_PURPLE + "Author: " + CC.L_PURPLE + "Jackdaw1101");
        DebugUtil.debug(CC.RED + "| |\\  |" + CC.BL_PURPLE + " || ");
        DebugUtil.debug(CC.RED + "|_| \\_|" + CC.BL_PURPLE + " || " + CC.BL_PURPLE + "Disabled in: " + CC.L_PURPLE + disableTime + "ms");
        DebugUtil.debug(CC.BD_RED + "=============================================");
        DebugUtil.debug(CC.GRAY + "[Neon-Loader] Terminated!");
    }

    private boolean checkConfigVersion() {
        int configVersion = getSettings().getInt("CONFIG-VERSION");
        return configVersion == getLatestConfigVersion();
    }

    private int getLatestConfigVersion() {
        return 1;
    }

    @SuppressWarnings("unused")
    private boolean isPlaceholderAPIInstalled() {
        Plugin placeholderAPI = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        return placeholderAPI != null && placeholderAPI.isEnabled();
    }

    @SuppressWarnings("unused")
    private boolean isLuckpermsInstalled() {
        Plugin lp = getServer().getPluginManager().getPlugin("LuckPerms");
        return lp != null && lp.isEnabled();
    }

    public static Neon getInstance() {
        if (instance == null) {
            instance = new Neon();
        }
        return instance;
    }

    public NeonAPI getNeonAPI() {
        return api;
    }

    public AntiSpamManager getAntiSpamManager() {
        return antiSpamManager;
    }

    public ConfigFile getMessageManager() {
        return messageManager;
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    public ConfigFile getPermissionManager() {
        return permissionManager;
    }

    public ConfigFile getDatabaseManager() {
        return database;
    }

    public IChatToggle getChatToggleAPI() {
        return iChatToggle;
    }

    public ConfigFile getDiscordManager() {
        return discord;
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

    public ConfigFile getLocales() {
        return locales;
    }

    public ChatMuteManager getChatMuteManager() {
        return chatMuteManager;
    }

    public PopUpBubbleChat getPopUpBubbleChat() {
        return popUpBubbleChat;
    }
}