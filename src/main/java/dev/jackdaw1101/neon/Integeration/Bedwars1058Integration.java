package dev.jackdaw1101.neon.Integeration;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.arena.Arena;
import dev.jackdaw1101.neon.API.Chat.ChatAPI;
import dev.jackdaw1101.neon.API.Chat.Events.ChatMessageEvent;
import dev.jackdaw1101.neon.API.Utils.CC;
import dev.jackdaw1101.neon.API.Utils.ColorHandler;
import dev.jackdaw1101.neon.Neon;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class Bedwars1058Integration implements Listener {

    private final Neon plugin;
    private final ChatAPI api;

    public Bedwars1058Integration(Neon plugin) {
        this.plugin = plugin;
        this.api = new ChatAPI(plugin);
    }

    @EventHandler
    public void onChatMessage(ChatMessageEvent event) {
        if (!Bukkit.getPluginManager().isPluginEnabled("BedWars1058")) {
            return;
        }
        Player sender = event.getSender();
        BedWars api = Bukkit.getServicesManager().load(BedWars.class);
        if (!plugin.getSettings().getBoolean("BEDWARS1058-SUPPORT")) {
            return;
        }


        if (api == null) {
            Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Fatal Error: Bedwars1058 API is not available!");
        }

        IArena arena = api.getArenaUtil().getArenaByPlayer(sender);

        if (arena == null) {
            return;
        }

        switch (arena.getStatus()) {
            case waiting:
            case starting:
                handleWaitingLobbyChat(event, sender, (Arena) arena);
                break;
            case playing:
                handlePlayingChat(event, sender, (Arena) arena);
                break;
            default:
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
        if (hoverLines.isEmpty()) {
            hoverLines.add("&7No hover text.");
        }

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

        String teamColor = "&f";
        ITeam team = arena.getTeam(sender);
        if (team != null) {
            teamColor = team.getColor().chat().toString();
        }

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
        if (hoverLines.isEmpty()) {
            hoverLines.add("&7No hover text.");
        }

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
