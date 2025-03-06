package dev.jackdaw1101.neon.Command.tabcomp;

import dev.jackdaw1101.neon.Command.NeonCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NeonTabCompleter implements TabCompleter {

    private final NeonCommand neonCommand;

    public NeonTabCompleter(NeonCommand neonCommand) {
        this.neonCommand = neonCommand;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> matches = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("help", "reload", "chatclear", "mutechat", "togglealerts"), matches);

            if (matches.isEmpty() && sender instanceof Player) {
                neonCommand.sendHelp(sender);
            }
        }

        return matches;
    }
}
