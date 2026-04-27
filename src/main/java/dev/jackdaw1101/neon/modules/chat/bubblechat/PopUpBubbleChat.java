package dev.jackdaw1101.neon.modules.chat.bubblechat;

import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.Neon;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class PopUpBubbleChat {

    private final Neon plugin;
    private final Map<UUID, BukkitTask> activeHolograms = new HashMap<>();
    private final Map<UUID, Hologram> playerHolograms = new HashMap<>();
    private final Map<UUID, BukkitTask> followingTasks = new HashMap<>();

    public PopUpBubbleChat(Neon plugin) {
        this.plugin = plugin;
    }

    public void sendPopUpBubble(Player sender, String message, Collection<? extends Player> recipients) {
        if (!plugin.getSettings().getBoolean("POPUP-BUBBLE.ENABLED")) return;
        if (recipients == null || recipients.isEmpty()) return;
        if (sender == null) return;

        removeHologram(sender.getUniqueId());

        List<String> bubbleFormat = plugin.getSettings().getStringList("POPUP-BUBBLE.FORMAT");
        if (bubbleFormat == null || bubbleFormat.isEmpty()) {
            bubbleFormat = Arrays.asList("<sender>: <message>");
        }

        String color = getMessageColor(sender);
        String coloredMessage = color + message;

        double baseHeight = plugin.getSettings().getDouble("POPUP-BUBBLE.BASE-HEIGHT");
        double extraHeightPerChar = plugin.getSettings().getDouble("POPUP-BUBBLE.EXTRA-HEIGHT-PER-CHAR");
        int maxLength = plugin.getSettings().getInt("POPUP-BUBBLE.MAX-LINE-LENGTH");
        boolean followPlayer = plugin.getSettings().getBoolean("POPUP-BUBBLE.FOLLOW-PLAYER");

        List<String> wrappedLines = new ArrayList<>();
        for (String formatLine : bubbleFormat) {
            String formattedLine = formatLine;

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                formattedLine = PlaceholderAPI.setPlaceholders(sender, formattedLine);
            }

            if (plugin.getServer().getPluginManager().getPlugin("BedWars1058") != null && plugin.getServer().getPluginManager().getPlugin("BedWars1058").isEnabled()) {
                try {
                    com.andrei1058.bedwars.api.BedWars bedwars = plugin.getServer().getServicesManager().getRegistration(com.andrei1058.bedwars.api.BedWars.class).getProvider();
                    com.andrei1058.bedwars.api.arena.IArena arena = bedwars.getArenaUtil().getArenaByPlayer(sender);
                    if (arena != null) {
                        formattedLine = formattedLine
                                .replace("<bw_team>", arena.getTeam(sender).getDisplayName(com.andrei1058.bedwars.api.language.Language.getDefaultLanguage()))
                                .replace("<bw_team_color>", arena.getTeam(sender).getColor().toString())
                                .replace("<bw_players>", String.valueOf(arena.getPlayers().size()))
                                .replace("<bw_alive>", String.valueOf(arena.getPlayers().size()))
                                .replace("<bw_spectators>", String.valueOf(arena.getSpectators().size()));
                    }
                } catch (Exception ignored) {}
            }

            if (plugin.getServer().getPluginManager().getPlugin("BedWars2023") != null && plugin.getServer().getPluginManager().getPlugin("BedWars2023").isEnabled()) {
                try {
                    com.tomkeuper.bedwars.api.BedWars bedwars = plugin.getServer().getServicesManager().getRegistration(com.tomkeuper.bedwars.api.BedWars.class).getProvider();
                    com.tomkeuper.bedwars.api.arena.IArena arena = bedwars.getArenaUtil().getArenaByPlayer(sender);
                    if (arena != null) {
                        formattedLine = formattedLine
                                .replace("<bw_team>", arena.getTeam(sender).getDisplayName(com.tomkeuper.bedwars.api.language.Language.getPlayerLanguage(sender)))
                                .replace("<bw_team_color>", arena.getTeam(sender).getColor().toString())
                                .replace("<bw_players>", String.valueOf(arena.getPlayers().size()))
                                .replace("<bw_alive>", String.valueOf(arena.getPlayers().size()))
                                .replace("<bw_spectators>", String.valueOf(arena.getSpectators().size()));
                    }
                } catch (Exception ignored) {}
            }

            formattedLine = formattedLine
                    .replace("<message>", coloredMessage)
                    .replace("<sender>", sender.getName())
                    .replace("<displayname>", sender.getDisplayName())
                    .replace("<world>", sender.getWorld().getName())
                    .replace("<health>", String.valueOf((int) sender.getHealth()));

            List<String> messageWrapped = wrapMessage(formattedLine, maxLength);
            wrappedLines.addAll(messageWrapped);
        }

        int lineCount = wrappedLines.size();

        double totalHeight = baseHeight + (lineCount * extraHeightPerChar);
        double lineSpacing = plugin.getSettings().getDouble("POPUP-BUBBLE.LINE-SPACING");

        Location bubbleLocation = sender.getLocation().clone().add(0, totalHeight, 0);

        Hologram hologram = new Hologram();
        for (int i = 0; i < wrappedLines.size(); i++) {
            double yOffset = (wrappedLines.size() - 1 - i) * lineSpacing;
            Location lineLocation = bubbleLocation.clone().add(0, yOffset, 0);
            String colorline = getMessageColor(sender);
            String coloredLine = ChatColor.translateAlternateColorCodes('&', colorline + wrappedLines.get(i));
            hologram.addLine(coloredLine, lineLocation);
        }

        playerHolograms.put(sender.getUniqueId(), hologram);

        for (Player viewer : recipients) {
            hologram.show(viewer);
        }

        if (followPlayer) {
            startFollowing(sender.getUniqueId(), sender, hologram, wrappedLines, totalHeight, lineSpacing);
        }

        int duration = plugin.getSettings().getInt("POPUP-BUBBLE.DURATION-TICKS");
        BukkitTask removalTask = new BukkitRunnable() {
            @Override
            public void run() {
                removeHologram(sender.getUniqueId());
            }
        }.runTaskLater(plugin, duration);

        activeHolograms.put(sender.getUniqueId(), removalTask);
    }

    private void startFollowing(UUID playerUUID, Player sender, Hologram hologram, List<String> lines, double totalHeight, double lineSpacing) {
        BukkitTask existingTask = followingTasks.get(playerUUID);
        if (existingTask != null) {
            try {
                existingTask.cancel();
            } catch (Exception ignored) {}
        }

        BukkitTask followTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (sender == null || !sender.isOnline() || !playerHolograms.containsKey(playerUUID)) {
                    this.cancel();
                    followingTasks.remove(playerUUID);
                    return;
                }
                Location newLocation = sender.getLocation().clone().add(0, totalHeight, 0);
                hologram.updatePosition(newLocation, lines.size(), lineSpacing);
            }
        }.runTaskTimer(plugin, 0L, 2L);

        followingTasks.put(playerUUID, followTask);
    }

    public void removeHologram(UUID playerUUID) {
        BukkitTask followTask = followingTasks.remove(playerUUID);
        if (followTask != null) {
            try {
                followTask.cancel();
            } catch (Exception ignored) {}
        }

        BukkitTask task = activeHolograms.remove(playerUUID);
        if (task != null) {
            try {
                task.cancel();
            } catch (Exception ignored) {}
        }

        Hologram hologram = playerHolograms.remove(playerUUID);
        if (hologram != null) {
            hologram.destroy();
        }
    }

    public void removeAllHolograms() {
        for (BukkitTask task : followingTasks.values()) {
            try {
                if (task != null) task.cancel();
            } catch (Exception ignored) {}
        }
        followingTasks.clear();

        for (Hologram hologram : playerHolograms.values()) {
            if (hologram != null) hologram.destroy();
        }
        playerHolograms.clear();

        for (BukkitTask task : activeHolograms.values()) {
            try {
                if (task != null) task.cancel();
            } catch (Exception ignored) {}
        }
        activeHolograms.clear();
    }

    private String getMessageColor(Player sender) {
        if (plugin.getSettings().getBoolean("POPUP-BUBBLE.RANDOM-COLOR")) {
            ChatColor[] colors = {
                    ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW,
                    ChatColor.GREEN, ChatColor.AQUA, ChatColor.LIGHT_PURPLE,
                    ChatColor.BLUE, ChatColor.DARK_PURPLE
            };
            return colors[new Random().nextInt(colors.length)].toString();
        } else {
            String colorCode = plugin.getSettings().getString("POPUP-BUBBLE.COLOR", "&f");
            return PlaceholderAPI.setPlaceholders(sender, ColorHandler.translateAlternateColorCodes('&', colorCode));
        }
    }

    private List<String> wrapMessage(String message, int maxLength) {
        List<String> lines = new ArrayList<>();
        String stripped = ChatColor.stripColor(message);

        if (stripped.length() <= maxLength) {
            lines.add(message);
            return lines;
        }

        String[] words = message.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLength) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                if (word.length() > maxLength) {
                    for (int i = 0; i < word.length(); i += maxLength) {
                        int end = Math.min(i + maxLength, word.length());
                        lines.add(word.substring(i, end));
                    }
                } else {
                    currentLine.append(word);
                }
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private class Hologram {
        private final List<ArmorStand> lines = new ArrayList<>();

        public void addLine(String text, Location location) {
            ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName(text);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            lines.add(armorStand);
        }

        public void show(Player player) {
            for (ArmorStand line : lines) {
                if (line == null || line.isDead()) continue;
                if (!line.isVisible()) {
                    line.setVisible(false);
                }
                try {
                    // nothing here for now
                } catch (Exception ignored) {}
            }
        }

        public void updatePosition(Location baseLocation, int lineCount, double lineSpacing) {
            for (int i = 0; i < lines.size(); i++) {
                ArmorStand line = lines.get(i);
                if (line != null && !line.isDead()) {
                    double yOffset = (lineCount - 1 - i) * lineSpacing;
                    Location newLocation = baseLocation.clone().add(0, yOffset, 0);
                    line.teleport(newLocation);
                }
            }
        }

        public void destroy() {
            for (ArmorStand line : lines) {
                if (line != null && !line.isDead()) {
                    line.remove();
                }
            }
            lines.clear();
        }
    }
}