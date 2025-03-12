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


# 📢 Auto Announce System

the **Auto Announce System**! This system automatically broadcasts announcements to players with customizable settings, including sounds, hover messages, clickable actions, and more.

## ⚙️ Configuration Guide

You can configure announcements inside your `settings.yml` file using the following structure:

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