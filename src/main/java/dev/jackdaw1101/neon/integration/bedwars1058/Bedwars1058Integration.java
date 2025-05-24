package dev.jackdaw1101.neon.integration.bedwars1058;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.arena.Arena;
import dev.jackdaw1101.neon.API.modules.chat.ChatAPI;
import dev.jackdaw1101.neon.API.modules.events.ChatMessageEvent;
import dev.jackdaw1101.neon.API.utilities.CC;
import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.integration.Integration;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Bedwars1058Integration implements Integration, Listener {

    private Neon plugin;
    private ChatAPI api;
    private List<String> soloGroups;
    private Map<UUID, Long> shoutCooldowns;

    @Override
    public void register(Neon plugin) {
        if (!(plugin instanceof Neon)) return;

        this.plugin = (Neon) plugin;
        this.api = new ChatAPI(this.plugin);
        this.shoutCooldowns = new HashMap<>();

        this.soloGroups = plugin.getSettings().getConfig().getStringList("BEDWARS-CHAT.SOLO-GROUPS");

        if (!Bukkit.getPluginManager().isPluginEnabled("BedWars1058")) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] BedWars1058 is not installed. Skipping integration.");
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] BedWars1058 integration loaded.");
    }

    @EventHandler
    public void onChatMessage(ChatMessageEvent event) {
        if (!plugin.getSettings().getBoolean("BEDWARS1058-SUPPORT")) return;
        if (!Bukkit.getPluginManager().isPluginEnabled("BedWars1058")) return;

        Player sender = event.getSender();
        BedWars api = Bukkit.getServicesManager().load(BedWars.class);

        if (api == null) {
            Bukkit.getConsoleSender().sendMessage(CC.RED + "[Neon] Fatal Error: Bedwars1058 API is not available!");
            return;
        }

        IArena arena = api.getArenaUtil().getArenaByPlayer(sender);
        if (arena == null) return;

        if (arena.isSpectator(sender) || !arena.isPlayer(sender)) {
            handleSpectatorChat(event, sender, (Arena) arena);
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
        }
    }

    private void handleSpectatorChat(ChatMessageEvent event, Player sender, Arena arena) {
        ConfigurationSection spectatorChat = plugin.getSettings().getConfig().getConfigurationSection("BEDWARS-CHAT.SPECTATOR");
        if (spectatorChat == null) return;
        event.setCancelled(true);

        String format = PlaceholderAPI.setPlaceholders(sender, ColorHandler.color(spectatorChat.getString("FORMAT"))
            .replace("<player>", sender.getName())
            .replace("<arena>", arena.getArenaName())
            .replace("<message>", event.getMessage())
            .replace("<arena_displayname>", arena.getDisplayName()));

        List<String> hoverLines = spectatorChat.getStringList("HOVER");
        if (hoverLines.isEmpty()) hoverLines.add("&7No hover text.");

        String hoverText = PlaceholderAPI.setPlaceholders(sender, ColorHandler.color(String.join("\n", hoverLines))
            .replace("<player>", sender.getName())
            .replace("<arena>", arena.getArenaName())
            .replace("<teamname>", CC.GRAY + "[SPECTATOR]")
            .replace("<arena_displayname>", arena.getDisplayName()));

        for (Player viewer : arena.getSpectators()) {
            api.sendFormattedMessage(
                viewer,
                format,
                hoverText,
                spectatorChat.getString("CLICK-COMMAND").replace("<player>", sender.getName()),
                spectatorChat.getBoolean("HOVER-ENABLED"),
                spectatorChat.getBoolean("CLICK-EVENT"),
                spectatorChat.getBoolean("RUN-COMMAND"),
                spectatorChat.getBoolean("SUGGEST-COMMAND")
            );
        }

        if (plugin.getSettings().getBoolean("CHAT-IN-CONSOLE")) {
            api.sendMessageToConsole(format);
        }
    }

    private void handleWaitingLobbyChat(ChatMessageEvent event, Player sender, Arena arena) {
        ConfigurationSection waitingChat = plugin.getSettings().getConfig().getConfigurationSection("BEDWARS-CHAT.WAITING-LOBBY");
        if (waitingChat == null) return;
        event.setCancelled(true);

        String format = PlaceholderAPI.setPlaceholders(sender, ColorHandler.color(waitingChat.getString("FORMAT"))
            .replace("<player>", sender.getName())
            .replace("<arena>", arena.getArenaName())
            .replace("<message>", event.getMessage()));

        List<String> hoverLines = waitingChat.getStringList("HOVER");
        if (hoverLines.isEmpty()) hoverLines.add("&7No hover text.");

        String hoverText = PlaceholderAPI.setPlaceholders(sender, ColorHandler.color(String.join("\n", hoverLines))
            .replace("<player>", sender.getName())
            .replace("<arena>", arena.getArenaName())
            .replace("<arena_displayname>", arena.getDisplayName()));

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
        ConfigurationSection shoutChat = plugin.getSettings().getConfig().getConfigurationSection("BEDWARS-CHAT.SHOUT");
        if (playingChat == null || shoutChat == null) return;

        ITeam team = arena.getTeam(sender);
        if (team == null) return;

        String message = event.getMessage();
        boolean isShout = message.startsWith("!");

        boolean isSoloGame = soloGroups.contains(arena.getGroup());

        if (isShout) {
            if (isSoloGame) {
                sender.sendMessage(ColorHandler.color(plugin.getMessageManager().getString("BEDWARS.SHOUT-DISABLED-MESSAGE")));
                event.setCancelled(true);
                return;
            }

            if (!sender.hasPermission(plugin.getPermissionManager().getString("BEDWARS.SHOUT-BYPASS"))) {
                int shoutCooldown = plugin.getSettings().getConfig().getInt("BEDWARS-CHAT.SHOUT-COOLDOWN", 5);
                if (shoutCooldown > 0) {
                    long lastShout = shoutCooldowns.getOrDefault(sender.getUniqueId(), 0L);
                    long currentTime = System.currentTimeMillis() / 1000;
                    long remaining = shoutCooldown - (currentTime - lastShout);

                    if (remaining > 0) {
                        sender.sendMessage(ColorHandler.color(plugin.getMessageManager()
                            .getString("BEDWARS.SHOUT-COOLDOWN-MESSAGE")
                            .replace("<time>", String.valueOf(remaining))));
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            message = message.substring(1).trim();
            if (message.isEmpty()) {
                event.setCancelled(true);
                return;
            }

            if (!sender.hasPermission(plugin.getPermissionManager().getString("BEDWARS.SHOUT-BYPASS"))) {
                shoutCooldowns.put(sender.getUniqueId(), System.currentTimeMillis() / 1000);
            }

            handleShoutChat(event, sender, arena, message);
            return;
        }

        event.setCancelled(true);

        String teamColor = team.getColor().chat().toString();
        String format = PlaceholderAPI.setPlaceholders(sender, ColorHandler.color(playingChat.getString("FORMAT"))
            .replace("<player>", sender.getName())
            .replace("<arena>", arena.getArenaName())
            .replace("<teamcolor>", teamColor)
            .replace("<message>", message)
            .replace("<teamname>", teamColor + "[" + team.getName() + "]")
            .replace("<teamletter>", team.getName())
            .replace("<arena_displayname>", arena.getDisplayName()));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(sender, format);
        }

        List<String> hoverLines = playingChat.getStringList("HOVER");
        if (hoverLines.isEmpty()) hoverLines.add("&7No hover text.");

        String hoverText = PlaceholderAPI.setPlaceholders(sender, ColorHandler.color(String.join("\n", hoverLines))
            .replace("<player>", sender.getName())
            .replace("<arena>", arena.getArenaName())
            .replace("<arena_displayname>", arena.getDisplayName()));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            hoverText = PlaceholderAPI.setPlaceholders(sender, hoverText);
        }

        for (Player member : team.getMembers()) {
            api.sendFormattedMessage(
                member,
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

    private void handleShoutChat(ChatMessageEvent event, Player sender, Arena arena, String message) {
        ConfigurationSection shoutChat = plugin.getSettings().getConfig().getConfigurationSection("BEDWARS-CHAT.SHOUT");
        if (shoutChat == null) return;
        event.setCancelled(true);

        ITeam team = arena.getTeam(sender);
        if (team == null) return;

        String teamColor = team.getColor().chat().toString();
        String format = PlaceholderAPI.setPlaceholders(sender, ColorHandler.color(shoutChat.getString("FORMAT"))
            .replace("<player>", sender.getName())
            .replace("<arena>", arena.getArenaName())
            .replace("<teamcolor>", teamColor)
            .replace("<message>", message)
            .replace("<teamname>", teamColor + "[" + team.getName() + "]")
            .replace("<teamletter>", team.getName())
            .replace("<arena_displayname>", arena.getDisplayName()));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(sender, format);
        }

        List<String> hoverLines = shoutChat.getStringList("HOVER");
        if (hoverLines.isEmpty()) hoverLines.add("&7No hover text.");

        String hoverText = PlaceholderAPI.setPlaceholders(sender, ColorHandler.color(String.join("\n", hoverLines))
            .replace("<player>", sender.getName())
            .replace("<arena>", arena.getArenaName())
            .replace("<arena_displayname>", arena.getDisplayName())
            .replace("<player>", sender.getName())
            .replace("<teamcolor>", teamColor)
            .replace("<message>", message)
            .replace("<teamname>", teamColor + "[" + team.getName() + "]")
            .replace("<teamletter>", team.getName())
            .replace("<arena_displayname>", arena.getDisplayName()));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            hoverText = PlaceholderAPI.setPlaceholders(sender, hoverText);
        }

        for (Player viewer : arena.getPlayers()) {
            api.sendFormattedMessage(
                viewer,
                format,
                hoverText,
                shoutChat.getString("CLICK-COMMAND").replace("<player>", sender.getName()),
                shoutChat.getBoolean("HOVER-ENABLED"),
                shoutChat.getBoolean("CLICK-EVENT"),
                shoutChat.getBoolean("RUN-COMMAND"),
                shoutChat.getBoolean("SUGGEST-COMMAND")
            );
        }

        if (plugin.getSettings().getBoolean("CHAT-IN-CONSOLE")) {
            api.sendMessageToConsole(format);
        }
    }
}
