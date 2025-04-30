package dev.jackdaw1101.neon.integration.bedwars2023;

import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.arena.Arena;
import dev.jackdaw1101.neon.api.modules.chat.ChatAPI;
import dev.jackdaw1101.neon.api.modules.events.ChatMessageEvent;
import dev.jackdaw1101.neon.api.utilities.CC;
import dev.jackdaw1101.neon.api.utilities.ColorHandler;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.integration.Integration;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class Bedwars2023Integration implements Integration, Listener {

    private Neon plugin;
    private ChatAPI api;

    @Override
    public void register(Neon plugin) {
        if (!(plugin instanceof Neon)) return;

        this.plugin = (Neon) plugin;
        this.api = new ChatAPI(this.plugin);

        if (!Bukkit.getPluginManager().isPluginEnabled("BedWars2023")) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] BedWars2023 is not installed. Skipping integration.");
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] BedWars2023 integration loaded.");
    }

    @EventHandler
    public void onChatMessage(ChatMessageEvent event) {
        if (!plugin.getSettings().getBoolean("BEDWARS2023-SUPPORT")) return;
        if (!Bukkit.getPluginManager().isPluginEnabled("BedWars2023")) return;

        Player sender = event.getSender();
        BedWars api = Bukkit.getServicesManager().load(BedWars.class);

        if (api == null) {
            Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Fatal Error: BedWars2023 API is not available!");
            return;
        }

        IArena arena = api.getArenaUtil().getArenaByPlayer(sender);
        if (arena == null) return;

        switch (arena.getStatus()) {
            case waiting:
            case starting:
                handleWaitingLobbyChat(event, sender, (Arena) arena);
                break;
            case playing:
                handlePlayingChat(event, sender, (Arena) arena);
                break;
        }
    }

    private void handleWaitingLobbyChat(ChatMessageEvent event, Player sender, Arena arena) {
        ConfigurationSection waitingChat = plugin.getSettings().getConfig().getConfigurationSection("BEDWARS-CHAT.WAITING-LOBBY");
        if (waitingChat == null) return;

        String format = ColorHandler.color(waitingChat.getString("FORMAT"))
            .replace("<player>", sender.getName())
            .replace("<arena>", arena.getArenaName())
            .replace("<message>", event.getMessage());

        List<String> hoverLines = waitingChat.getStringList("HOVER");
        if (hoverLines.isEmpty()) hoverLines.add("&7No hover text.");

        String hoverText = ColorHandler.color(String.join("\n", hoverLines))
            .replace("<player>", sender.getName())
            .replace("<arena>", arena.getArenaName())
            .replace("<arena_displayname>", arena.getDisplayName());

        event.setCancelled(true);

        for (Player viewer : arena.getPlayers()) {
            api.sendFormattedMessage(
                viewer,
                format,
                hoverText,
                waitingChat.getString("CLICK-COMMAND").replace("<player>", sender.getName()),
                waitingChat.getBoolean("HOVER-ENABLED"),
                waitingChat.getBoolean("CLICK-EVENT"),
                waitingChat.getBoolean("RUN-COMMAND"),
                waitingChat.getBoolean("SUGGEST-COMMAND")
            );
        }

        api.sendMessageToConsole(format);
    }

    private void handlePlayingChat(ChatMessageEvent event, Player sender, Arena arena) {
        ConfigurationSection playingChat = plugin.getSettings().getConfig().getConfigurationSection("BEDWARS-CHAT.PLAYING");
        if (playingChat == null) return;

        ITeam team = arena.getTeam(sender);
        String teamColor = (team != null) ? team.getColor().chat().toString() : "&f";

        String format = ColorHandler.color(playingChat.getString("FORMAT"))
            .replace("<player>", sender.getName())
            .replace("<arena>", arena.getArenaName())
            .replace("<teamcolor>", teamColor)
            .replace("<message>", event.getMessage())
            .replace("<teamname>", teamColor + "[" + team.getName() + "]")
            .replace("<teamletter>", team.getName())
            .replace("<arena_displayname>", arena.getDisplayName());

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(sender, format);
        }

        List<String> hoverLines = playingChat.getStringList("HOVER");
        if (hoverLines.isEmpty()) hoverLines.add("&7No hover text.");

        String hoverText = ColorHandler.color(String.join("\n", hoverLines))
            .replace("<player>", sender.getName())
            .replace("<arena>", arena.getArenaName())
            .replace("<arena_displayname>", arena.getDisplayName());

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            hoverText = PlaceholderAPI.setPlaceholders(sender, hoverText);
        }

        event.setCancelled(true);

        for (Player viewer : arena.getPlayers()) {
            api.sendFormattedMessage(
                viewer,
                format,
                hoverText,
                playingChat.getString("CLICK-COMMAND").replace("<player>", sender.getName()),
                playingChat.getBoolean("HOVER-ENABLED"),
                playingChat.getBoolean("CLICK-EVENT"),
                playingChat.getBoolean("RUN-COMMAND"),
                playingChat.getBoolean("SUGGEST-COMMAND")
            );
        }

        if (plugin.getSettings().getBoolean("CHAT-IN-CONSOLE")) {
            api.sendMessageToConsole(format);
        }
    }
}
