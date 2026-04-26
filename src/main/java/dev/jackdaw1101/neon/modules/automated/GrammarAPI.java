package dev.jackdaw1101.neon.modules.automated;

import dev.jackdaw1101.neon.API.modules.events.GrammarCheckEvent;
import dev.jackdaw1101.neon.implementions.IGrammarImpl;
import dev.jackdaw1101.neon.Neon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class GrammarAPI implements Listener {
    private final Neon plugin;
    private final IGrammarImpl api;

    public GrammarAPI(Neon plugin) {
        this.plugin = plugin;
        this.api = new IGrammarImpl(plugin);
    }

    @EventHandler
    public void grammarCheck(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String originalMessage = event.getMessage();

        if (originalMessage.length() < api.getMinMessageLength() ||
            !plugin.getSettings().getBoolean("GRAMMAR-API.ENABLED")) return;

        if (player.hasPermission(plugin.getPermissionManager().getString("BYPASS-GRAMMAR"))) return;


        String correctedMessage = api.processMessage(originalMessage);


        GrammarCheckEvent grammarEvent = new GrammarCheckEvent(
            player,
            originalMessage,
            correctedMessage,
            api.isAutoCorrectEnabled(),
            api.isPunctuationCheckEnabled(),
            api.isCapitalizationEnabled()
        );

        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getServer().getPluginManager().callEvent(grammarEvent);
        });

        if (grammarEvent.isCancelled()) return;


        if (!grammarEvent.getCorrectedMessage().equals(correctedMessage)) {
            correctedMessage = grammarEvent.getCorrectedMessage();
        }

        event.setMessage(correctedMessage);
    }

    public IGrammarImpl getAPI() {
        return api;
    }
}
