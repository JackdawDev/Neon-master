package dev.jackdaw1101.neon.API.command;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public abstract class NeonCommand {
    private final String name;
    private final String description;
    private final String usage;
    private final List<String> aliases;

    public NeonCommand(String name, String description, String usage, List<String> aliases) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public List<String> getAliases() {
        return aliases != null ? aliases : Collections.emptyList();
    }

    public abstract boolean onCommand(CommandSender sender, String label, String[] args);

    public List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        return Collections.emptyList();
    }
}

