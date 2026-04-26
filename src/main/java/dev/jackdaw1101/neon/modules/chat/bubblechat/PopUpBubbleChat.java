package dev.jackdaw1101.neon.modules.chat.bubblechat;

import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.Neon;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class PopUpBubbleChat {

    private final Neon plugin;
    private final Map<UUID, BukkitTask> activeHolograms = new HashMap<>();
    private final Map<UUID, Hologram> playerHolograms = new HashMap<>();

    public PopUpBubbleChat(Neon plugin) {
        this.plugin = plugin;
    }

    public void sendPopUpBubble(Player sender, String message, Collection<? extends Player> recipients) {
        if (!plugin.getSettings().getBoolean("POPUP-BUBBLE.ENABLED")) return;
        if (recipients == null || recipients.isEmpty()) return;

        // Remove existing hologram for this sender
        removeHologram(sender.getUniqueId());

        // Get message color
        String color = getMessageColor();
        String coloredMessage = color + message;

        // Calculate height based on message length
        double baseHeight = plugin.getSettings().getDouble("POPUP-BUBBLE.BASE-HEIGHT");
        double extraHeightPerChar = plugin.getSettings().getDouble("POPUP-BUBBLE.EXTRA-HEIGHT-PER-CHAR");
        int maxLength = plugin.getSettings().getInt("POPUP-BUBBLE.MAX-LINE-LENGTH");

        // Format message with line breaks
        List<String> wrappedLines = wrapMessage(coloredMessage, maxLength);
        int lineCount = wrappedLines.size();

        // Calculate total height
        double totalHeight = baseHeight + (lineCount * extraHeightPerChar);
        double lineSpacing = plugin.getSettings().getDouble("POPUP-BUBBLE.LINE-SPACING");

        // Create location above player's head
        Location bubbleLocation = sender.getLocation().clone().add(0, totalHeight, 0);

        // Create hologram lines (in reverse order so they stack upward)
        Hologram hologram = new Hologram();
        for (int i = 0; i < wrappedLines.size(); i++) {
            double yOffset = (wrappedLines.size() - 1 - i) * lineSpacing;
            Location lineLocation = bubbleLocation.clone().add(0, yOffset, 0);
            hologram.addLine(wrappedLines.get(i), lineLocation);
        }

        // Store hologram for later removal
        playerHolograms.put(sender.getUniqueId(), hologram);

        // Schedule removal
        int duration = plugin.getSettings().getInt("POPUP-BUBBLE.DURATION-TICKS");
        BukkitTask removalTask = new BukkitRunnable() {
            @Override
            public void run() {
                removeHologram(sender.getUniqueId());
            }
        }.runTaskLater(plugin, duration);

        activeHolograms.put(sender.getUniqueId(), removalTask);
    }

    public void removeHologram(UUID playerUUID) {
        BukkitTask task = activeHolograms.remove(playerUUID);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }

        Hologram hologram = playerHolograms.remove(playerUUID);
        if (hologram != null) {
            hologram.destroy();
        }
    }

    public void removeAllHolograms() {
        for (Hologram hologram : playerHolograms.values()) {
            hologram.destroy();
        }
        playerHolograms.clear();

        for (BukkitTask task : activeHolograms.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        activeHolograms.clear();
    }

    private String getMessageColor() {
        if (plugin.getSettings().getBoolean("POPUP-BUBBLE.RANDOM-COLOR")) {
            ChatColor[] colors = {
                    ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW,
                    ChatColor.GREEN, ChatColor.AQUA, ChatColor.LIGHT_PURPLE,
                    ChatColor.BLUE, ChatColor.DARK_PURPLE
            };
            return colors[new Random().nextInt(colors.length)].toString();
        } else {
            String colorCode = plugin.getSettings().getString("POPUP-BUBBLE.COLOR", "&f");
            return ColorHandler.color(colorCode);
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
        private final List<org.bukkit.entity.ArmorStand> lines = new ArrayList<>();

        public void addLine(String text, Location location) {
            org.bukkit.entity.ArmorStand armorStand = location.getWorld().spawn(location, org.bukkit.entity.ArmorStand.class);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', text));
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            lines.add(armorStand);
        }


        public void destroy() {
            for (org.bukkit.entity.ArmorStand line : lines) {
                if (line != null && !line.isDead()) {
                    line.remove();
                }
            }
            lines.clear();
        }
    }
}