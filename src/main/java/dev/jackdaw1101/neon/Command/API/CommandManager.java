package dev.jackdaw1101.neon.Command.API;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager {
    private static CommandMap commandMap;
    private static final Map<String, NeonCommand> registeredCommands = new HashMap<>();

    static {
        try {
            Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(Bukkit.getPluginManager());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerCommand(JavaPlugin plugin, NeonCommand command) {
        if (commandMap != null) {
            registeredCommands.put(command.getName(), command);
            commandMap.register(plugin.getDescription().getName(), new CommandWrapper(command));
        }
    }

    public static void unregisterCommand(String commandName) {
        if (commandMap != null) {
            registeredCommands.remove(commandName);
            commandMap.getCommand(commandName).unregister(commandMap);
        }
    }

    private static class CommandWrapper extends Command implements CommandExecutor, TabCompleter {
        private final NeonCommand command;

        protected CommandWrapper(NeonCommand command) {
            super(command.getName(), command.getDescription(), command.getUsage(), command.getAliases());
            this.command = command;
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            return command.onCommand(sender, commandLabel, args);
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
            return command.onTabComplete(sender, alias, args);
        }

        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
            return false;
        }

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
            return Collections.emptyList();
        }
    }
}
