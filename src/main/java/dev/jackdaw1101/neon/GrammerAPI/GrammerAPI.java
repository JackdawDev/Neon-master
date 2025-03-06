package dev.jackdaw1101.neon.GrammerAPI;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.Utils.StringUtil.IStringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.Arrays;

public class GrammerAPI implements Listener {
    private final Neon plugin;

    public GrammerAPI(Neon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void grammarCheck(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String message = event.getMessage();

        if (message.length() < (int) plugin.getSettings().getValue("GRAMMAR-API.MIN-MESSAGE-LENGTH", 5) ||
                !(boolean) plugin.getSettings().getValue("GRAMMAR-API.ENABLED", false)) return;

        if (player.hasPermission(plugin.getPermissionManager().getPermission("BYPASS-GRAMMAR"))) return;

        // Capitalize first letter
        try {
            message = message.replaceFirst(message.charAt(0) + "", IStringUtils.capitalize(message.charAt(0) + ""));
        } catch (Exception ignored) {}

        // Ensure punctuation at end
        char lastChar = message.charAt(message.length() - 1);
        if (!Arrays.asList('!', '.', ',', '?').contains(lastChar)) {
            message += ".";
        }

        // Auto-correct feature
        if ((boolean) plugin.getSettings().getValue("GRAMMAR-API.AUTO-CORRECT.ENABLED", false)) {
            String[] messageSplit = message.split(" ");
            StringBuilder sb = new StringBuilder();

            for (String word : messageSplit) {
                switch (word.toLowerCase()) {
                    case "i": sb.append("I"); break;
                    case "im": case "i'm": sb.append("I'm"); break;
                    case "ill": case "i'll": sb.append("I'll"); break;
                    case "cant": sb.append("can't"); break;
                    case "youre": sb.append("you're"); break;
                    case "dont": sb.append("don't"); break;
                    case "theyre": sb.append("they're"); break;
                    case "couldnt": sb.append("couldn't"); break;
                    case "whos": sb.append("who's"); break;
                    case "alot": sb.append("a lot"); break;
                    case "nor": case "yet": case "or": case "and": sb.append(word + ","); break;
                    default: sb.append(word);
                }
                sb.append(" ");
            }
            event.setMessage(sb.toString().trim());
        }
    }
}

