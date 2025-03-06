package dev.jackdaw1101.neon.Command;

import dev.jackdaw1101.neon.Command.Chat.MuteChatCommandNormal;
import dev.jackdaw1101.neon.Command.ChatClear.ChatClearCommand;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.Chat.CC;
import dev.jackdaw1101.neon.Utils.Color.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public class NeonCommand implements CommandExecutor {
    private final Neon plugin;

    public NeonCommand(Neon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (!sender.hasPermission(plugin.getPermissionManager().getPermission("HELP"))) {
                sendNoPermissionMessage(sender);
                return true;
            }

            if (args.length == 1) {
                sendHelp(sender);
            } else if (args.length == 2) {
                if (args[1].equalsIgnoreCase("1")) {
                    sendHelp(sender);
                } else if (args[1].equalsIgnoreCase("2")) {
                    sendHelpPage2(sender);
                } else {
                    sendHelp(sender);
                }
            } else {
                sendHelp(sender);
            }
            return true;
        }


        if (args[0].equalsIgnoreCase("chatclear")) {
            if (!sender.hasPermission(plugin.getPermissionManager().getPermission("CHAT_CLEAR.PERMISSION"))) {
                sendNoPermissionMessage(sender);
                return true;
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;
                ChatClearCommand chatClearCommand = new ChatClearCommand(plugin);
                chatClearCommand.clearChat(player); // Passing player object
            } else {
                ChatClearCommand chatClearCommand = new ChatClearCommand(plugin);
                chatClearCommand.clearChat(sender); // Passing CommandSender for console handling
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("mutechat")) {
            if (!sender.hasPermission(plugin.getPermissionManager().getPermission("MUTE-CHAT-USE"))) {
                sendNoPermissionMessage(sender);
                return true;
            }

            if (sender instanceof Player) {
                MuteChatCommandNormal chatMute = new MuteChatCommandNormal(plugin);
                chatMute.toggleChatMute(sender, args.length > 1 ? args[1] : null); // Passing player and optional argument
            } else {
                MuteChatCommandNormal chatMute = new MuteChatCommandNormal(plugin);
                chatMute.toggleChatMute(sender, args.length > 1 ? args[1] : null); // Allow console execution
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("togglealerts")) {
            if (!sender.hasPermission(plugin.getPermissionManager().getPermission("TOGGLE-ALERTS-USE"))) {
                sendNoPermissionMessage(sender);
                return true;
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;
                plugin.getAlertManager().toggleAlerts(player); // Use the shared AlertManager instance
            } else {
                sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("PLAYER-ONLY")));
            }

            return true;
        }



        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(plugin.getPermissionManager().getPermission("RELOAD"))) {
                sendNoPermissionMessage(sender);
                return true;
            }
            NeonReloadCommand reloadCommand = new NeonReloadCommand(plugin);
            reloadCommand.executeReload(sender);
            return true;
        }

        // If the argument is not recognized, show the help message.
        sendHelp(sender);
        return true; // Prevents further processing and shows the help message for invalid arguments.
    }

    public void sendHelp(CommandSender sender) {
        String header = ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("SECOND-THEME") + CC.STRIKE_THROUGH + "----------------------------------------");
        String page = ChatColor.translateAlternateColorCodes('&', "   &ePage: &7(1/2)");
        String versionInfo = ChatColor.translateAlternateColorCodes('&', CC.B_GOLD + "NEON CHAT MANAGER " + "&7v" + plugin.getDescription().getVersion());
        String mainMessage = ChatColor.translateAlternateColorCodes('&', "&7 * &b&lBy Jackdaw1101&d");
        String blank = ChatColor.translateAlternateColorCodes('&', " ");

        // Create clickable and hoverable messages for each command
        String reload = ChatColor.translateAlternateColorCodes('&', " &f* &eReload The Plugin");
        String help = ChatColor.translateAlternateColorCodes('&',  " &f* &eShows This Message");
        String chatclear = ChatColor.translateAlternateColorCodes('&', " &f* &eClears The Chat");
        String mute = ChatColor.translateAlternateColorCodes('&', " &f* &eMute The Chat &7&o(toggle)");
        String togglechatinfo = ChatColor.translateAlternateColorCodes('&', " &f* &eToggle The Chat Messages &7&o(toggle)");
        String reloadconsole = ChatColor.translateAlternateColorCodes('&', " &f- &eReload The Plugin");
        String helpconsole = ChatColor.translateAlternateColorCodes('&',  " &f- &eShows This Message");
        String mutechatconsole = ChatColor.translateAlternateColorCodes('&', " &f- &eMutes The Chat");
        String togglechatconsole = ChatColor.translateAlternateColorCodes('&', " &f- &eToggle The Chat For PLayer");
        String chatclearconsole = ChatColor.translateAlternateColorCodes('&', " &f- &eClears The Chat");

        TextComponent reloadMessage = createClickableMessage("/neon reload", CC.GRAY + "Click Reload the plugin");
        TextComponent helpMessage = createClickableMessage("/neon help", CC.GRAY + "/neon help <page> to navigate through pages");
        TextComponent chatClearMessage = createClickableMessage("/neon chatclear", CC.GRAY + "Click to Clear the chat");
        TextComponent muteChat = createClickableMessage("/neon mutechat", CC.GRAY + "Click To Mute Chat");
        TextComponent togglechat = createClickableMessage("/togglechat", CC.GRAY + "Toggle The Chat");

        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.spigot().sendMessage(new TextComponent(header));
            player.spigot().sendMessage(new TextComponent(versionInfo));
            player.spigot().sendMessage(new TextComponent(mainMessage));
            player.spigot().sendMessage(new TextComponent(page));
            player.spigot().sendMessage(new TextComponent(blank));
            player.spigot().sendMessage(reloadMessage);
            player.spigot().sendMessage(new TextComponent(reload));
            player.spigot().sendMessage(new TextComponent(blank));
            player.spigot().sendMessage(helpMessage);
            player.spigot().sendMessage(new TextComponent(help));
            player.spigot().sendMessage(new TextComponent(blank));
            player.spigot().sendMessage(chatClearMessage);
            player.spigot().sendMessage(new TextComponent(chatclear));
            player.spigot().sendMessage(new TextComponent(blank));
            player.spigot().sendMessage(new TextComponent(muteChat));
            player.spigot().sendMessage(new TextComponent(mute));
            player.spigot().sendMessage(new TextComponent(blank));
            player.spigot().sendMessage(new TextComponent(togglechat));
            player.spigot().sendMessage(new TextComponent(togglechatinfo));
            player.spigot().sendMessage(new TextComponent(blank));
            player.spigot().sendMessage(new TextComponent(header));
        } else {
            sender.sendMessage(header);
            sender.sendMessage(versionInfo);
            sender.sendMessage(mainMessage);
            sender.sendMessage(page);
            sender.sendMessage(blank);
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("MAIN-THEME") + "/neon reload" + reloadconsole));
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("MAIN-THEME") + "/neon help <page>" + helpconsole));
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("MAIN-THEME") + "/neon chatclear" + chatclearconsole));
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("MAIN-THEME") + "/neon mutechat" + mutechatconsole));
            sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getMessage("MAIN-THEME") + "/togglechat" + togglechatconsole));
            sender.sendMessage(header);
        }
    }

    public void sendHelpPage2(CommandSender sender) {
        String header = ColorHandler.color(plugin.getMessageManager().getMessage("SECOND-THEME") + CC.STRIKE_THROUGH + "----------------------------------------");
        String page = ChatColor.translateAlternateColorCodes('&', " &ePage: &7(2/2)");
        String blank = ChatColor.translateAlternateColorCodes('&', " ");

        // Create clickable and hoverable messages for each command

        String togglealerts = ChatColor.translateAlternateColorCodes('&', " &f* &eAdmin Alerts &7&o(toggle)");
        String togglealertsconsole = ChatColor.translateAlternateColorCodes('&', " &f- &eClears The Chat");
        TextComponent togglealert = createClickableMessage("/neon togglealerts", CC.GRAY + "Toggle The Alerts For Admins");

        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.spigot().sendMessage(new TextComponent(header));
            player.spigot().sendMessage(new TextComponent(page));
            player.spigot().sendMessage(new TextComponent(blank));
            player.spigot().sendMessage(togglealert);
            player.spigot().sendMessage(new TextComponent(togglealerts));
            player.spigot().sendMessage(new TextComponent(blank));
            player.spigot().sendMessage(new TextComponent(header));
        } else {
            sender.sendMessage(header);
            sender.sendMessage(page);
            sender.sendMessage(blank);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("MAIN-THEME") + "/neon togglealerts" + togglealertsconsole));
            sender.sendMessage(header);
        }
    }


        private void sendNoPermissionMessage(CommandSender sender) {
        String noPermissionMessage = ColorHandler.color(
                plugin.getMessageManager().getMessage("NO-PERMISSION"));
        if ((boolean) plugin.getSettings().getValue("NO-PERMISSION.USE-SOUND", true)) {
            if ((boolean) plugin.getSettings().getValue("ISOUNDS-UTIL", true)) {
                if ((boolean) plugin.getSettings().getValue("NO-PERMISSION.USE-SOUND", true)) {
                    SoundUtil.playSound((Player) sender, (String) plugin.getSettings().getValue("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
                }
            } else if ((boolean) plugin.getSettings().getValue("XSOUNDS-UTIL", true)) {
                XSounds.playSound((Player) sender, (String) plugin.getSettings().getValue("NO-PERMISSION.SOUND"), 1.0f, 1.0f);
            }
        }
        sender.sendMessage(noPermissionMessage);
    }

    private TextComponent createClickableMessage(String command, String hoverText) {
        boolean isSuggestCommand = (boolean) plugin.getSettings().getValue("NEON-COMMAND.SUGGEST-COMMANDS", true);
        boolean isRunCommand = (boolean) plugin.getSettings().getValue("NEON-COMMAND.RUN-COMMANDS", true);

        TextComponent message = new TextComponent(ColorHandler.color(plugin.getMessageManager().getMessage("MAIN-THEME") + command));

        if (isSuggestCommand && !isRunCommand) {
            message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        } else if (!isSuggestCommand && isRunCommand) {
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        }
        // FallBack Method
        else if (!isSuggestCommand && !isRunCommand) {
            message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        } else if (isSuggestCommand && isRunCommand) {
            message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));

        }

        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hoverText)).create()));
        return message;
    }
}
