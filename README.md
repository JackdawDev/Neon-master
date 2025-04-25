# Neon Chat Manager (Beta)

## Whats Neon?
neon is an all-in-one And Optimized Chat Manager for Minecraft Servers Based on 1.8 all the way to 1.21.x
all features are customizeable through the settings.yml!

## Features
- Auto Broadcast (announcements): You can add as many anonnounces as you wish with lot of diffrent settings!

- Anti AD: prevent player from sending links in chat with log and discord webhook support!

- Anti Caps: Don't allow players to use too many capital words

- Anti Spam: Prevent players from spamming in chat

- Anti Swear: Prevent players from using blocked words in chat with log and webhook support and diffrent cancel type

- Anti Unicode: Prevent players from using non-ASCII characters

- Chat Mute: Mute the chat

- Chat Format: Chat format with hover, toggleable chat and diffrent click events (chat logs and chat in console)

- Grammer API: Corrects player messages

- MOTD System: Welcome and join/leave message with hover, click events and sound support

- Mention System: Mention players in chat with sound support

- Command Log System: Log Commands That Players Use

- Auto Response: Auto response to specific words with hover and sound support

- Datanbase support for features to Support Network Uses

- Decent API & Events For nearly all features provided

- Optimized and lightweight. have no impact on performance

# 📢 Auto Announce System

the **Auto Announce System**! This system automatically broadcasts announcements to players with customizable settings, including sounds, hover messages, clickable actions, and more.

## ⚙️ Configuration Guide

You can configure announcements inside your `locale.yml` file using the following structure:

```yaml
<announcement-name>:
  REQUIRE-PERMISSION: true/false # Should only certain players receive this announcement?
  PERMISSION: "your.permission.node" # Required permission (if REQUIRE-PERMISSION is true)
  TEXT: "Your announcement message here" # The content of the announcement
  HOVER: true/false # Enable hover text?
  HOVER-CONTENT: "Hover text here" # Content displayed on hover (if HOVER is true)
  PLAY-SOUND: true/false # Should a sound play?
  SOUND: "ENTITY_EXPERIENCE_ORB_PICKUP" # Sound to play (Check Line 33 of settings.yml for more details)
  CLICK-COMMAND: true/false # Run a command when clicked?
  SUGGEST-COMMAND: true/false # Suggest a command in the chat bar?
  OPEN-URL: true/false # Open a URL on click?
  URL: "https://example.com" # URL to open (if OPEN-URL is true)
  COMMAND: "/yourcommand" # Command for both CLICK-COMMAND & SUGGEST-COMMAND
  INTERVAL: 60 # Time in seconds between announcements

```

# 🤖 Auto Response System

The **Auto Response System** automatically replies to specific words or phrases entered by players in chat. Customize responses easily and add as many keywords as you need!

---

## ⚙️ Configuration Guide

The auto-responses are defined inside your `locale.yml`, `settings.yml` & `messages.yml` file under the `AUTO-RESPONSES` section.

### 🔹 Basic Format:
- locale.yml
```yaml
AUTO-RESPONSES:
  <word>:
    - "<response1>"
    - "<response2>"
```
- settings.yml
```yaml
# Enable Auto response system
AUTO-RESPONSE-ENABLED: true
#
# Sound Get Played For Player On Response
AUTO-RESPONSE-USE-SOUND: true
#
# (!) If You Are Using XSound Utils Use minecraft Normal sound ids
# but if using Isound Util You Can See the available sounds below (Work Also FOr Xsound Util)
#
# Check Line 33 For More Info About Sounds
#
# (!) Use Xsound Util For Better Experience
#
# The Sound Value
AUTO-RESPONSE-SOUND: "NOTE"
#
# Enable Hover for Auto Response
AUTO-RESPONSE-HOVER-ENABLED: true
#
# Auto-response words (<word>: {answer})
# words In locale.yml
#
# Hover messages for auto-responses
AUTO-RESPONSE-HOVER:
  - "&eYour Name: &7%player_name%"
  - "&eYour health: &c%player_health%"
  - ' '
```
- Messages.yml
```yaml
# prefix for the system
AUTO-RESPONSE-PREFIX: "&7[&a&lAUTO RESPONSE&7]"
#
# Format of the auto response system
FORMAT: "{prefix} {auto_response_prefix}&8: &e%answer%"
```

# API
🔌 Getting Started

**You need to use the plugin .jar ro access the API**

## 📦 Adding the Neon Dependency
Step 1: Create a libs folder
Place the Neon-1.0.jar file inside a libs folder in your project directory.

```yaml

your-project/  
├── src/  
├── libs/  
│   └── Neon-1.0.jar  
├── pom.xml (Maven)  
└── build.gradle (Gradle)
└── Maven (pom.xml)
```
Add this dependency to your pom.xml:

```xml
<dependency>
   <groupId>dev.Jackdaw1101</groupId>
   <artifactId>neon</artifactId>
   <version>1.0</version>
   <scope>system</scope>
   <systemPath>${project.basedir}/libs/Neon-1.0.jar</systemPath>
</dependency>
```
### 🟢 Gradle (build.gradle)
Add this to your dependencies block:

```groovy
dependencies {
   implementation files("${project.projectDir}/libs/Neon-1.0.jar")
   }
```

### 🚀 Using the Neon API

```java
// Check is Neon is enabled
Plugin neonPlugin = getServer().getPluginManager().getPlugin("Neon");
if (neonPlugin == null || !neonPlugin.isEnabled()) {
    getLogger().severe("Neon plugin is not enabled! Disabling this plugin...");
    getServer().getPluginManager().disablePlugin(this);
    return;
}
```

```java
// Available APIs
  AutoResponseAPI api = Bukkit.getServicesManager().load(AutoResponseAPI.class);
  AntiSwearAPI api = Bukkit.getServicesManager().load(AntiSwearAPI.class);
  NeonJoinLeaveAPI api = Bukkit.getServicesManager().load(NeonJoinLeaveAPI.class);
  ChatToggleAPI api = Bukkit.getServicesManager().load(ChatToggleAPI.class);
  GrammarAPI api = Bukkit.getServicesManager().load(GrammarAPI.class);
  NeonAPI api = Bukkit.getServicesManager().load(NeonAPI.class);
  AddonManager addonManager = Bukkit.getServicesManager().load(AddonManager.class);
```

## 🚀 NeonAPI
Global Neon API for addons and Configuration Stuff

```java
public class NeonAPI {
    private final AddonManager addonManager;
    private final Neon plugin;
    
    public boolean registerAddon(String addonName, String version, Class<?> mainClass)
      
    public List<String> getRegisteredAddons();
      
    public boolean isAddonRegistered(String addonName);
      
    public YamlConfiguration createAddonConfig(String addonName, String fileName);

    public YamlConfiguration getAddonConfig(String addonName, String fileName);
    
    public boolean saveAddonConfig(YamlConfiguration config, String addonName, String fileName);
    
    public boolean createAddonSubfolder(String addonName, String folderName);

    public File getAddonSubfolder(String addonName, String folderName);
    
    public String getAddonVersion(String addonName);

    public Class<?> getAddonMainClass(String addonName);

    public String getNeonPrefix();
}
```
💡 Example Usage
```java
@Override
public void onEnable() {
  System.out.println(api.getNeonPrefix() + "Addon Enabled!");
}
```

## 🤖 AutoResponseAPI
Manage automatic chat responses

```java
public interface AutoResponseAPI {
// Add/remove responses
void addResponse(String triggerWord, List<String> responses);
void removeResponse(String triggerWord);

    // Configure global settings
    void setGlobalHoverText(List<String> hoverText);
    void setGlobalSound(String sound);
    
    // Get current responses
    Map<String, List<String>> getAllResponses();
    List<String> getResponsesForWord(String triggerWord);
    
    // Force trigger response
    void triggerResponse(Player player, String triggerWord);
}
```

💡 Example Usage
```java
// Add new response
autoResponseAPI.addResponse("hello", Arrays.asList(
"Hi there, %player_name%!",
"Welcome to our server!"
));
```

# 🔥 AntiSwearAPI

**Advanced profanity filtering system**

```java
public interface AntiSwearAPI {
   
    void addToBlacklist(String word);
    
    void removeFromBlacklist(String word);
    
    void addTemporaryWhitelistWord(String word);
    
    List<String> getBlacklist();
    
    void clearTemporaryBlacklist();

    int getSwearStrikes(Player player);
    
    void resetSwearStrikes(Player player);
    
    void setSwearStrikes(Player player, int strikes);

    boolean isSwearWord(String word);
    
    boolean containsSwear(String message);
    
    String censorMessage(String message);
  
    String sanitizeMessage(String message);
}
```
💡 Example Usage
```java
// Basic detection
if (antiSwearAPI.containsSwear(player.getMessage())) {
player.sendMessage("§cNo profanity allowed!");
String clean = antiSwearAPI.censorMessage(player.getMessage());
player.chat(clean); // Re-send censored version
}

// Strike management
int strikes = antiSwearAPI.getSwearStrikes(player);
if (strikes > 3) {
player.kickPlayer("Too many swear violations");
}

// Dynamic word management
antiSwearAPI.addToBlacklist("newword");
antiSwearAPI.addTemporaryWhitelistWord("allowedword");
```

# ✨ NeonJoinLeaveAPI

Advanced join/leave message customization system

```java
public interface NeonJoinLeaveAPI {
    void sendCustomJoinMessage(Player player, String message, 
                             List<String> hoverText, 
                             String clickCommand, 
                             ClickAction clickAction);
                             
    void sendCustomLeaveMessage(Player player, String message,
                              List<String> hoverText,
                              String clickCommand,
                              ClickAction clickAction);

    void setJoinMessageFormat(String format);
    void setLeaveMessageFormat(String format);

    void setJoinHoverText(List<String> hoverText);
    void setLeaveHoverText(List<String> hoverText);

    void setJoinClickCommand(String command, ClickAction action);
    void setLeaveClickCommand(String command, ClickAction action);

    void setJoinHoverEnabled(boolean enabled);
    void setLeaveHoverEnabled(boolean enabled);

    void setJoinClickEnabled(boolean enabled);
    void setLeaveClickEnabled(boolean enabled);

    void setJoinRequirePermission(boolean require);
    void setLeaveRequirePermission(boolean require);

    void setJoinPermission(String permission);
    void setLeavePermission(String permission);

    void reloadConfig();

    enum ClickAction {
        RUN_COMMAND,
        SUGGEST_COMMAND,
        OPEN_URL
    }
}
```

💡 Example Usage
```java
// Custom join message with hover and click action
api.sendCustomJoinMessage(player,
"§e{player} §bhas joined with §a{rank}",
Arrays.asList("Click for profile!", "Playtime: 5h"),
"profile {player}",
ClickAction.RUN_COMMAND);

// Configure default formats
api.setJoinMessageFormat("§aWelcome {player} to the server!");
api.setLeaveMessageFormat("§c{player} has left");

// Enable advanced features
api.setJoinHoverEnabled(true);
api.setJoinClickEnabled(true);
api.setJoinClickCommand("msg {player}", ClickAction.SUGGEST_COMMAND);
```

# 🔇 ChatToggleAPI

Player chat toggle management system

```java
public interface ChatToggleAPI {
    
    void toggleChat(Player player);
    
    void setChatToggled(Player player, boolean toggled);
    
    boolean isChatToggled(Player player);
    
    boolean isChatToggled(UUID uuid);
    
    Set<UUID> getAllToggledPlayers();
      
    void saveAll();
}
```

💡 Example Usage
```java
// Basic usage
chatToggleAPI.toggleChat(player);

// Check state
if (chatToggleAPI.isChatToggled(player)) {
// Chat is disabled
}
```

## ✍️ GrammarAPI
Advanced message grammar correction system

```java
public interface GrammarAPI {

    // Auto-Correction Management
    void addAutoCorrectWord(String incorrect, String correct);
    void removeAutoCorrectWord(String incorrect);
    Map<String, String> getAutoCorrectWords();
    
    // Feature Toggles
    void setAutoCorrectEnabled(boolean enabled);
    void setPunctuationCheckEnabled(boolean enabled);
    void setCapitalizationEnabled(boolean enabled);
    void setMinMessageLength(int length);
    
    // Status Checks
    boolean isAutoCorrectEnabled();
    boolean isPunctuationCheckEnabled();
    boolean isCapitalizationEnabled();
    int getMinMessageLength();
    
    // Core Processing
    String processMessage(String message);
}
```
💡 Example Usage

```java
// Basic setup
grammarAPI.addAutoCorrectWord("teh", "the");
grammarAPI.addAutoCorrectWord("adn", "and");
grammarAPI.setCapitalizationEnabled(true);

// Process a message
String corrected = grammarAPI.processMessage("hello to teh world. adn welcome!");
// Returns: "Hello to the world. And welcome!"

// Check features
if (grammarAPI.isAutoCorrectEnabled()) {
// Auto-correction is active
}

// Bulk operations
Map<String, String> corrections = Map.of(
"btw", "by the way",
"afaik", "as far as I know"
);
corrections.forEach(grammarAPI::addAutoCorrectWord);
```

# Events

## 🚫 AntiCapsEvent
Event triggered when a player sends a message with excessive capital letters.

```java
public class AntiCapsEvent extends Event implements Cancellable {
// Event Handlers
public static HandlerList getHandlerList();
public @NotNull HandlerList getHandlers();

    // Cancellation
    public boolean isCancelled();
    public void setCancelled(boolean cancel);

    // Message & Player Data
    public Player getPlayer();
    public String getMessage();
    public boolean isCommand();

    // Caps Detection Stats
    public double getCapsPercentage();
    public int getUpperChars();
    public int getLowerChars();
    public int getMinLength();
    public int getRequiredPercentage();

    // Behavior Control
    public boolean shouldCancel();
    public void setShouldCancel(boolean shouldCancel);

    // Warning & Sound Settings
    public String getWarningMessage();
    public void setWarningMessage(String warningMessage);
    public String getSound();
    public void setSound(String sound);
    public boolean shouldPlaySound();
    public void setPlaySound(boolean playSound);
}
```
💡 Example Usage
```java
@EventHandler
public void onCapsEvent(AntiCapsEvent event) {
   if (event.getCapsPercentage() > 70.0) {
       event.setWarningMessage("§cPlease avoid excessive caps!");
       event.setShouldCancel(true);
       event.setPlaySound(true);
       event.setSound(XSound.ENTITY_ENDERMAN_TELEPORT);
     }
}
```

## 🔗 AntiLinkTriggerEvent
Event triggered when a player sends a message containing a blocked link.

```java
public class AntiLinkTriggerEvent extends Event implements Cancellable {  
// Event Handlers  
public static HandlerList getHandlerList();  
public @NotNull HandlerList getHandlers();

    // Cancellation  
    public boolean isCancelled();  
    public void setCancelled(boolean cancel);  

    // Message & Link Data  
    public Player getPlayer();  
    public String getOriginalMessage();  
    public String getSanitizedMessage();  
    public String getDetectedLink();  
    public String getCancelType();  

    // Alert & Logging Settings  
    public boolean shouldAlertAdmins();  
    public void setAlertAdmins(boolean alertAdmins);  
    public boolean shouldLogToConsole();  
    public void setLogToConsole(boolean logToConsole);  
    public boolean shouldSendWebhook();  
    public void setSendWebhook(boolean sendWebhook);  

    // Sound & Message Customization  
    public String getWarnSound();  
    public void setWarnSound(String warnSound);  
    public String getAlertSound();  
    public void setAlertSound(String alertSound);  
    public String getWarnMessage();  
    public void setWarnMessage(String warnMessage);  
    public String getAlertMessage();  
    public void setAlertMessage(String alertMessage);  
}  
```
💡 Example Usage
```java
@EventHandler  
public void onLinkDetected(AntiLinkTriggerEvent event) {  
if (event.getDetectedLink().contains("discord.gg")) {  
    event.setWarnMessage("§cDiscord links are not allowed!");  
    event.setAlertAdmins(true);  
    event.setAlertMessage("[STAFF] " + event.getPlayer().getName() + " sent: " + event.getDetectedLink());  
    event.setCancelled(true);  
    }  
}
```  

## 🚨 AntiSpamEvent
Event triggered when a player's message or command is flagged as spam. Supports multiple detection types with customizable actions.

```java
public class AntiSpamEvent extends Event implements Cancellable {
// Event Handlers
public static HandlerList getHandlerList();
public @NotNull HandlerList getHandlers();

    // Cancellation
    public boolean isCancelled();
    public void setCancelled(boolean cancel);

    // Spam Detection Data
    public Player getPlayer();
    public String getMessage();
    public String getPreviousMessage();
    public double getSimilarityPercentage();
    public SpamType getSpamType();

    // Response Configuration
    public boolean shouldCancel();
    public void setShouldCancel(boolean shouldCancel);
    public String getWarningMessage();
    public void setWarningMessage(String warningMessage);
    
    // Sound Effects
    public String getSound();
    public void setSound(String sound);
    public boolean shouldPlaySound();
    public void setPlaySound(boolean playSound);
    
    // Player Punishment
    public boolean shouldKick();
    public void setShouldKick(boolean shouldKick);
    public String getKickMessage();
    public void setKickMessage(String kickMessage);

    // Spam Types
    public enum SpamType {
        SIMILAR_MESSAGE,          // Similar text content
        REPETITIVE_MESSAGE,       // Identical messages
        REPETITIVE_CHARACTERS,    // Repeated characters
        COMMAND_SPAM,             // Command flooding
        COMMAND_REPETITIVE,      // Repeated commands
        COMMAND_REPETITIVE_CHARACTERS, // Command with repeated chars
        CHAT_DELAY,               // Chat cooldown violation
        COMMAND_DELAY             // Command cooldown violation
    }
}
```
💡 Example Usage
```java
@EventHandler
public void onSpamDetected(AntiSpamEvent event) {
if (event.getSpamType() == SpamType.REPETITIVE_MESSAGE) {
    event.setWarningMessage("§cPlease don't repeat messages!");
    event.setSound(XSound.ENTITY_ENDERMAN_TELEPORT);
    event.setShouldCancel(true);
    }
}
```

## 🔹 SwearDetectEvent (Implementation)
```java
public class SwearDetectEvent extends AntiSwearEvent {
// Detection Data
public String getDetectedWord();

    // Message Handling
    public String getCensoredMessage();
    public void setCensoredMessage(String censoredMessage);
    
    // Logging Configuration
    public boolean shouldNotifyAdmins();
    public void setNotifyAdmins(boolean notifyAdmins);
    public boolean shouldLogToConsole();
    public void setLogToConsole(boolean logToConsole);
}
```
💡 Example Usage
```java
@EventHandler
public void onSwearDetected(SwearDetectEvent event) {
// Custom censorship
    event.setCensoredMessage("[CENSORED]");

    // Only notify admins for severe words
    if(isSevereWord(event.getDetectedWord())) {
        event.setNotifyAdmins(true);
        event.setLogToConsole(true);
    } else {
        event.setNotifyAdmins(false);
    }
    
    // Optional: Cancel the original message
    event.setCancelled(true);
}
```

## 🔹 SwearPunishEvent
```java
public class SwearPunishEvent extends AntiSwearEvent {
// Strike System
public int getStrikes();  // Returns current violation count

    // Punishment Customization
    public String getPunishCommand();  // e.g. "kick {player} Swearing"
    public void setPunishCommand(String command);
}
```
💡 Example Usage
```java
// Punishment Handling
@EventHandler
public void onSwearPunish(SwearPunishEvent e) {
    if(e.getStrikes() >= 3) {
        e.setPunishCommand("tempban {player} 1h Repeated swearing");
    } else {
      e.setPunishCommand("warn {player} No swearing!");
    }
}
```

## 🔤 AntiUnicodeEvent
Event triggered when a player uses restricted Unicode characters in chat messages.

```java
public class AntiUnicodeEvent extends Event implements Cancellable {
// Event Handlers
public static HandlerList getHandlerList();
public @NotNull HandlerList getHandlers();

    // Core Data
    public Player getPlayer();
    public String getMessage();
    public String getDetectedUnicode();  // Returns the filtered Unicode characters

    // Cancellation Control
    public boolean isCancelled();
    public void setCancelled(boolean cancel);

    // Punishment System
    public boolean shouldKick();
    public void setShouldKick(boolean shouldKick);
    public String getKickMessage();
    public void setKickMessage(String message);

    // Player Feedback
    public String getBlockMessage();  // Message shown when chat is blocked
    public void setBlockMessage(String message);
    
    // Sound Effects
    public String getSound();
    public void setSound(String sound);
    public boolean shouldPlaySound();
    public void setPlaySound(boolean playSound);
}
```
💡 Example Usage

```java
import com.cryptomorin.xseries.XSound; // For Sounds You need XSound (ISound is not implemented for api Yet)
@EventHandler
public void onUnicodeDetect(AntiUnicodeEvent event) {
// Block all Unicode except allowed emojis
  if (!isAllowedEmoji(event.getDetectedUnicode())) {
    event.setBlockMessage("§cSpecial characters are restricted!");
    event.setSound(XSound.ENTITY_ENDERMAN_TELEPORT);
    event.setCancelled(true);

    // Auto-kick after 3 violations
    if (getViolationCount(event.getPlayer()) >= 3) {
      event.setShouldKick(true);
      event.setKickMessage("§cKicked for excessive special characters");
    }
  }
}

private boolean isAllowedEmoji(String unicode) {
  return unicode.matches("[😀-🙏]");  // Only allow basic emojis (Dont do exactly this in your code or it might explode)
}
```

## 🤖 AutoResponseEvent
Event triggered when a player's message matches a registered trigger word, allowing dynamic response customization.

```java
public class AutoResponseEvent extends Event implements Cancellable {
// Event Handlers
public static HandlerList getHandlerList();
public @NotNull HandlerList getHandlers();

    // Core Data
    public Player getPlayer();
    public String getTriggerWord();  // The word that triggered this response

    // Response Configuration
    public List<String> getResponses();  // Messages to send
    public void setResponses(List<String> responses);
    
    // Hover Text (Tooltips)
    public List<String> getHoverText();
    public void setHoverText(List<String> hoverText);
    public boolean shouldUseHover();
    public void setUseHover(boolean useHover);

    // Sound Effects
    public String getSound();
    public void setSound(String sound);
    public boolean shouldPlaySound();
    public void setPlaySound(boolean playSound);

    // Event Control
    public boolean isCancelled();
    public void setCancelled(boolean cancel);
}
```
💡 Example Usage
```java
@EventHandler
public void onAutoResponse(AutoResponseEvent event) {
// Customize responses for specific triggers
if (event.getTriggerWord().equalsIgnoreCase("hello")) {
event.setResponses(Arrays.asList(
"Hi there, " + event.getPlayer().getName() + "!",
"Welcome to our server!"
));
event.setHoverText(Arrays.asList(
"§aClick to view rules",
"§eOnline players: " + Bukkit.getOnlinePlayers().size()
));
event.setSound("entity.experience_orb.pickup");
}

    // Disable hover for mobile users
    if (isMobileUser(event.getPlayer())) {
        event.setUseHover(false);
    }
}
```

## 🎉 NeonPlayerJoinEvent
Fully customizable join message event with interactive hover and click actions.

```java
public class NeonPlayerJoinEvent extends Event implements Cancellable {
// Event Control
public boolean isCancelled();
public void setCancelled(boolean cancel);

    // Player Data
    public Player getPlayer();
    
    // Message Configuration
    public String getJoinMessage();
    public void setJoinMessage(String message);
    
    // Hover Text System
    public List<String> getHoverText();
    public void setHoverText(List<String> text);
    public boolean isHoverEnabled();
    public void setHoverEnabled(boolean enabled);
    
    // Click Action System
    public boolean isClickEnabled();
    public void setClickEnabled(boolean enabled);
    public String getClickCommand();
    public void setClickCommand(String command);
    public ClickAction getClickAction();
    public void setClickAction(ClickAction action);
    
    // Click Actions
    public enum ClickAction {
        RUN_COMMAND,    // Executes command as player
        SUGGEST_COMMAND, // Inserts command in chat
        OPEN_URL        // Opens web browser
    }
}
```
💡 Example Usage
```java
@EventHandler
public void onPlayerJoin(NeonPlayerJoinEvent event) {
// Customize join message
event.setJoinMessage("§b✦ " + event.getPlayer().getName() + " joined the server!");

    // Set hover text
    event.setHoverText(Arrays.asList(
        "§6Level: " + getPlayerLevel(event.getPlayer()),
        "§aClick to view profile!"
    ));
    
    // Configure click action
    if (event.getPlayer().hasPermission("profile.view")) {
        event.setClickEnabled(true);
        event.setClickCommand("/profile " + event.getPlayer().getName());
        event.setClickAction(ClickAction.RUN_COMMAND);
    }
    
    // VIP Players
    if (event.getPlayer().hasPermission("vip")) {
        event.setJoinMessage("§d★ VIP " + event.getPlayer().getName() + " arrived!");
        event.setHoverText(Arrays.asList(
            "§dVIP Member",
            "§7Join date: " + getJoinDate(event.getPlayer())
        ));
    }
}
```

## 🚪 NeonPlayerLeaveEvent
Fully customizable leave message system with interactive elements, mirroring the join event functionality.

```java
public class NeonPlayerLeaveEvent extends Event implements Cancellable {
// Event Control
public boolean isCancelled();
public void setCancelled(boolean cancel);

    // Player Data
    public Player getPlayer();
    
    // Message Configuration
    public String getLeaveMessage();
    public void setLeaveMessage(String message);
    
    // Interactive Elements
    public boolean isHoverEnabled();
    public void setHoverEnabled(boolean enabled);
    public boolean isClickEnabled();
    public void setClickEnabled(boolean enabled);
    
    // Click Action System
    public String getClickCommand();
    public void setClickCommand(String command);
    public ClickAction getClickAction();
    public void setClickAction(ClickAction action);
    
    // Hover Text
    public List<String> getHoverText();
    public void setHoverText(List<String> text);
    
    // Click Actions
    public enum ClickAction {
        RUN_COMMAND,    // Executes command as console
        SUGGEST_COMMAND, // Suggests command in chat
        OPEN_URL        // Opens web URL
    }
}
```
💡 Example Usage
```java
@EventHandler
public void onPlayerLeave(NeonPlayerLeaveEvent event) {
// Basic leave message
event.setLeaveMessage("§8[§c-§8] " + event.getPlayer().getName());

    // Add hover stats for donators
    if (event.getPlayer().hasPermission("donator")) {
        event.setHoverEnabled(true);
        event.setHoverText(Arrays.asList(
            "§6Donator Status: §aActive",
            "§7Last seen: §f" + getCurrentTime(),
            "§7Playtime: §f" + formatPlaytime(getPlaytime(event.getPlayer()))
        );
        
        // Click to view donator profile
        event.setClickEnabled(true);
        event.setClickCommand("/donator profile " + event.getPlayer().getName());
        event.setClickAction(ClickAction.RUN_COMMAND);
    }
    
    // Special treatment for staff
    if (event.getPlayer().hasPermission("staff")) {
        event.setLeaveMessage("§4[STAFF] §c" + event.getPlayer().getName() + " left");
        event.setHoverText(Arrays.asList(
            "§cStaff Member Offline",
            "§7Status: §fOn break",
            "§7Click to notify"
        ));
        event.setClickCommand("/notifystaff " + event.getPlayer().getName());
    }
}
```

## 🔇 ToggleChatEvent
Event triggered when a player toggles their chat visibility state (on/off).

```java
public class ToggleChatEvent extends Event implements Cancellable {
// Event Handlers
public static HandlerList getHandlerList();
public @NotNull HandlerList getHandlers();

    // State Management
    public boolean isCancelled();
    public void setCancelled(boolean cancel);

    // Player Data
    public Player getPlayer();

    // Toggle State
    public boolean getNewState();  // true = chat disabled, false = chat enabled
    public void setNewState(boolean newState);
}
```
💡 Example Usage
```java
@EventHandler
public void onChatToggle(ToggleChatEvent event) {
// Notify staff when VIPs disable chat
    if (event.getNewState() && event.getPlayer().hasPermission("vip")) {
        Bukkit.broadcast(
          "§7[STAFF] VIP " + event.getPlayer().getName() + " disabled chat",
          "staff.notify"
        );
}

    // Prevent new players from disabling chat
    if (isNewPlayer(event.getPlayer()) && event.getNewState()) {
        event.setCancelled(true);
        event.getPlayer().sendMessage("§cYou must play for 1 hour before disabling chat!");
    }
}
```

## 🎊 WelcomeEvent
Highly customizable welcome message system with rich text formatting, sounds, and interactive elements.

```java
public class WelcomeEvent extends Event {
// Core Components
public Player getPlayer();
public List<String> getMessageLines();
public void setMessageLines(List<String> lines);

    // Event Control
    public boolean isCancelled();
    public void setCancelled(boolean cancelled);
    
    // Sound Effects
    public String getSound();
    public void setSound(String sound);
    
    // Hover Text Options
    public List<String> getHoverMessages(); // Simple text version
    public void setHoverMessages(List<String> messages);
    public BaseComponent[] getHoverComponents(); // Advanced JSON
    public void setHoverComponents(BaseComponent[] components);
    
    // Click Actions
    public String getClickCommand();
    public void setClickCommand(String command);
    public boolean isClickCommandEnabled();
    public void setClickCommandEnabled(boolean enabled);
    
    // URL Handling
    public String getOpenUrl();
    public void setOpenUrl(String url);
    public boolean isOpenUrlEnabled();
    public void setOpenUrlEnabled(boolean enabled);
    
    // Command Suggestions
    public String getSuggestCommand();
    public void setSuggestCommand(String command);
    public boolean isSuggestCommandEnabled();
    public void setSuggestCommandEnabled(boolean enabled);
}
```
💡 Example Usage
```java
@EventHandler
public void onWelcome(WelcomeEvent event) {
// Basic welcome message
event.setMessageLines(Arrays.asList(
"§6Welcome to the server, " + event.getPlayer().getName() + "!",
"§7You are visitor #" + getTotalJoins()
));

    // Add hover profile info
    event.setHoverMessages(Arrays.asList(
        "§aAccount created: §f" + getJoinDate(event.getPlayer()),
        "§aPlaytime: §f" + formatPlaytime(event.getPlayer())
    ));
    
    // VIP Players
    if (event.getPlayer().hasPermission("vip")) {
        event.setSound("entity.player.levelup");
        event.setClickCommandEnabled(true);
        event.setClickCommand("/vip lounge " + event.getPlayer().getName());
        
        // Add JSON hover components
        event.setHoverComponents(new ComponentBuilder()
            .append("VIP PERKS\n").color(ChatColor.GOLD)
            .append("Click to teleport!").color(ChatColor.GREEN)
            .create());
    } 
    
    // First-time players
    if (isFirstJoin(event.getPlayer())) {
        event.setSuggestCommandEnabled(true);
        event.setSuggestCommand("/tutorial");
    }
}
```

## @️ MentionEvent
Event triggered when a player mentions another player or group in chat.

```java
public class MentionEvent extends Event {
// Participant Info
public Player getSender();        // Player who sent the mention
public Player getMentioned();    // Player who was mentioned (null if @everyone)

    // Message Content
    public String getMessage();      // Full message containing the mention
    
    // Mention Type Detection
    public boolean isMentionedBySymbol();  // True if @ symbol was used
    public String getMentionSymbol();       // Returns "@" or custom symbol used
    public boolean isEveryoneMention();     // True for @everyone mentions
    
    // Cooldown State
    public boolean isCooldownActive();     // True if sender is in mention cooldown
}
```
💡 Example Usage
```java
@EventHandler
public void onPlayerMention(MentionEvent event) {
// Highlight VIP mentions
if (event.getMentioned() != null &&
event.getMentioned().hasPermission("vip")) {
Bukkit.broadcastMessage("§6VIP Mention! §7" +
event.getSender().getName() + " mentioned " +
event.getMentioned().getName());
}

    // Prevent @everyone abuse
    if (event.isEveryoneMention() && 
        !event.getSender().hasPermission("mention.everyone")) {
        event.getSender().sendMessage("§cYou can't mention everyone!");
        event.setCancelled(true);
    }

    // Notify staff about cooldown bypass attempts
    if (event.isCooldownActive()) {
        alertStaff("Cooldown bypass attempt by " + 
            event.getSender().getName());
    }
}
```

## ✍️ GrammarCheckEvent
Event triggered when a player's chat message undergoes grammar correction.

```java
public class GrammarCheckEvent extends Event implements Cancellable {
// Core Components
public Player getPlayer();
public String getOriginalMessage();
public String getCorrectedMessage();
public void setCorrectedMessage(String message);

    // Grammar Modules
    public boolean isAutoCorrectEnabled();
    public void setAutoCorrectEnabled(boolean enabled);
    public boolean isPunctuationCheckEnabled();
    public void setPunctuationCheckEnabled(boolean enabled);
    public boolean isCapitalizationEnabled();
    public void setCapitalizationEnabled(boolean enabled);
    
    // Event Control
    public boolean isCancelled();
    public void setCancelled(boolean cancel);
}
```
💡 Example Usage
```java
@EventHandler
public void onGrammarCheck(GrammarCheckEvent event) {
// Bypass grammar checks for staff
if (event.getPlayer().hasPermission("neon.grammar.bypass")) {
event.setCancelled(true);
return;
}

    // Enhance corrections for new players
    if (isNewPlayer(event.getPlayer())) {
        event.setAutoCorrectEnabled(true);
        event.setPunctuationCheckEnabled(true);
        event.setCapitalizationEnabled(true);
        
        // Add learning tips to corrections
        if (!event.getOriginalMessage().equals(event.getCorrectedMessage())) {
            String tip = "\n§7Tip: Try: '" + event.getCorrectedMessage() + "'";
            event.setCorrectedMessage(event.getCorrectedMessage() + tip);
        }
    }
    
    // Disable punctuation checks in creative mode
    if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
        event.setPunctuationCheckEnabled(false);
    }
}
```

### Additional Info About API and Event
- You should use XSound from Xseries for Sounds and Enable XSound Util Usage in The Settings.yml
- All messages support hex color

# Create Addons
**To Create Folders in plugins/Neon/Addons/<whatever>/<whatever>.yml**
Default Config API For Addons and Folders. (Doesn't support configs in folders)
```java
// In addon's main class
NeonAPI api = Neon.getAPI();

// Register the addon
api.registerAddon("MyAddon", "1.0.0", getClass());

// Create configuration
YamlConfiguration config = api.createAddonConfig("MyAddon", "settings");
config.set("enabled", true);
config.set("message", "Hello World!");
api.saveAddonConfig(config, "MyAddon", "settings");

// Create data folder
api.createAddonSubfolder("MyAddon", "data");

// Get list of all addons
List<String> addons = api.getRegisteredAddons();
```
```java
// Register an addon
addonManager.registerAddon("MyAddon", "1.0.0", MyAddonMain.class);

// Get addon information
AddonManager.AddonInfo info = addonManager.getAddonInfo("MyAddon");
System.out.println(info.getFormattedUptime());

// Unregister an addon (on disable)
addonManager.unregisterAddon("MyAddon");
```
# Neon Advanced Addon Configuration Manager

The `AddonConfigManager` provides a robust configuration system for Neon addons, featuring automatic file creation, version updating, comment preservation, and placeholder replacement.
## Basic Setup

Initialize the config manager in your addon's main class:

💡 Usage Examples:
1. Default configs folder:

```java
// Will create/store in: Neon/Addons/MyAddon/configs/config.yml
AddonConfigManager config = new AddonConfigManager(this, "config.yml");
```
2. Custom subfolder:

```java
// Will create/store in: Neon/Addons/MyAddon/data/config.yml
AddonConfigManager config = new AddonConfigManager(this, "config.yml", true, "data");
```
3. No subfolder:

```java
// Will create/store in: Neon/Addons/MyAddon/config.yml
AddonConfigManager config = new AddonConfigManager(this, "config.yml", false);
```
4. With header:

```java
AddonConfigManager config = new AddonConfigManager(this, "config.yml", true, "settings");
config.setHeader(
"MyAddon Configuration",
"Version: 1.0.0",
"Auto-generated - modify with care"
);
```

## Full usage example
```java
package com.yourproject.myaddon;

import dev.jackdaw1101.neon.API.Configuration.AddonConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MyAddon extends JavaPlugin {
    private AddonConfigManager config;
    private AddonConfigManager messages;
    private AddonConfigManager data;

    @Override
    public void onEnable() {
      Plugin neonPlugin = getServer().getPluginManager().getPlugin("Neon");
      if (neonPlugin == null || !neonPlugin.isEnabled()) {
        getLogger().severe("Neon plugin is not enabled! Disabling this plugin...");
        getServer().getPluginManager().disablePlugin(this);
        return;
      }
      NeonAPI api = Bukkit.getServicesManager().load(NeonAPI.class);
      AddonManager addonManager = Bukkit.getServicesManager().load(AddonManager.class);
      api.registerAddon("NeonAddon", this.getDescription().getVersion(), NeonAddon.class);

      // 1. Basic config in default 'configs' subfolder
        config = new AddonConfigManager(this, "config.yml");
        setupMainConfig();
        
        // 2. Messages in custom 'lang' subfolder with header
        messages = new AddonConfigManager(this, "messages.yml", true, "lang");
        setupMessages();
        
        // 3. Data storage in main addon folder (no subfolder)
        data = new AddonConfigManager(this, "storage.yml", false);
        setupDataStorage();
        
        // Example usage
        getCommand("myaddon").setExecutor(new MyAddonCommand(this));
        getServer().getPluginManager().registerEvents(new MyAddonListener(this), this);
        
        
      System.out.println(addonManager.getAddonInfo("NeonAddon"));

    private void setupMainConfig() {
        // Set header for the config
        config.setHeader(
            "MyAddon Main Configuration",
            "Version: " + addonManager.getVersion(),
            "Modify carefully!",
            "Auto-generated by MyAddon"
        );
        
        // Add defaults with comments
        config.addDefault("enabled", true, 
            "Whether the addon is enabled");
        config.addDefault("settings.cooldown", 30,
            "Default cooldown in seconds",
            "Minimum: 10, Maximum: 300");
        config.addDefault("settings.prefix", "&a[MyAddon] &7",
            "Chat prefix for all messages",
            "Supports color codes with &");
        
        // Save if any defaults were added
        config.save();
    }

    private void setupMessages() {
        messages.setHeader(
            "MyAddon Messages",
            "Version: " + getDescription().getVersion(),
            "You can customize all messages here"
        );
        
        messages.addDefault("welcome", "&aWelcome {player} to our server!",
            "Welcome message when player joins",
            "Placeholders: {player} - Player name");
        messages.addDefault("no-permission", "&cYou don't have permission for that!",
            "Permission denied message");
        
        messages.save();
    }

    private void setupDataStorage() {
        // No header needed for data files
        data.addDefault("last-login", new HashMap<String, Long>());
        data.addDefault("player-stats", new HashMap<String, Integer>());
        
        // Don't need to save here as we're using maps that will be populated later
    }

    // Example getter methods for other classes to access
    public String getMessage(String path, String playerName) {
        return messages.getString(path, "{player}", playerName);
    }
    
    public boolean isFeatureEnabled() {
        return config.getBoolean("enabled");
    }
    
    public int getCooldown() {
        return config.getInt("settings.cooldown");
    }
    
    public void updatePlayerLogin(String playerName) {
        Map<String, Long> logins = (Map<String, Long>) data.getConfig().get("last-login");
        logins.put(playerName, System.currentTimeMillis());
        data.set("last-login", logins);
    }

    @Override
    public void onDisable() {
        // Save all data when disabling
        data.save();
        getLogger().info("MyAddon disabled - data saved");
    }
}
```

# Hex Colors & internal Placeholders
## Global Placeholders
- {prefix} - in all files but only in Strings
- {main_theme} - in all files but only in Strings
- {second_theme} - in all files but only in Strings
- {third_theme} - in all files but only in Strings

## Private Placeholders
**only used in Chat Format and is not available on other features YET**
- <lp_prefix> = Prefix Of Player (luckPerms)
- <lp_suffix> = Suffix Of Player (luckPerms)

## Hex & Colors
**To use hex colors you need to run Neon on a 1.16+ MC Version**
```yaml
* <#0000FF>Message</#FFFFFF>
* &#0000FF
* <rainbow>Message</rainbow>
* <color-name>Message</color-name>
```

## 🟢 ColorHandler
Use Neon Hex Color and Color Utils in your addons / plugins that use Neon API
and CC for a better in code color system

### Usage

```java
player.sendMessage(CC.AQUA + "Test");
ColorHandler.color("&#0000FFText &bWith <rainbow>Hex Support</rainbow>");
```
# Databases
## Supported Databases
- MongoDB
- MySQL
- SQLite

### These database infos are used globally in neon for adding network support and data and stats
### saving of some features
```yaml
# Options: sqlite, mysql, mongodb
DATABASE:
  TYPE: "sqlite"
#
# Mongo DB Settings
MONGODB:
  DATABASE: "Neon"
  URL: "mongodb://localhost:27017"
#
# MySQL Settings
MYSQL:
  HOST: "localhost"
  PORT: 3306
  DATABASE: "database"
  USERNAME: "root"
  PASSWORD: "veryHardPassWord1"
  #
  # Do not modify these if you don't know what your doing
  USE-SSL: false
  AUTO-RECONNECT: true
  FAIL-OVER-RED-ONLY: true
  MAX-RECONNECTS: 10
#
# SQLite database (.db is added in the code)
SQLITE-CHATTOGGLE:
  NAME: "chat_toggle"
```

**(!) Databases cannot be reload via /neon reload and they require a restart**
