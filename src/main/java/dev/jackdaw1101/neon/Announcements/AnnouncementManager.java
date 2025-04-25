package dev.jackdaw1101.neon.Announcements;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.Utils.ColorHandler;
import dev.jackdaw1101.neon.Utils.ISounds.SoundUtil;
import dev.jackdaw1101.neon.Utils.ISounds.XSounds;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnouncementManager {

    private final Neon plugin;
    private final Map<String, AnnouncementTask> tasks = new HashMap<>();

    public AnnouncementManager(Neon plugin) {
        this.plugin = plugin;
        boolean isAnnouncementEnabled = (Boolean) this.plugin.getSettings().getBoolean("ANNOUNCEMENTS.ENABLED");
        if (!isAnnouncementEnabled) return;
        startDynamicScheduler();

    }

    private void startDynamicScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateSchedulers();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 100L);
    }

    private void updateSchedulers() {
        ConfigurationSection announcementsSection = plugin.getLocales().getConfig().getConfigurationSection("ANNOUNCEMENTS");

        if (announcementsSection == null) return;

        tasks.keySet().removeIf(key -> {
            if (!announcementsSection.contains(key)) {
                tasks.get(key).cancel();
                return true;
            }
            return false;
        });

        for (String key : announcementsSection.getKeys(false)) {
            ConfigurationSection announcement = announcementsSection.getConfigurationSection(key);
            if (announcement == null) continue;

            int interval = announcement.getInt("INTERVAL", 60);

            if (tasks.containsKey(key)) {
                AnnouncementTask existingTask = tasks.get(key);
                if (existingTask.getInterval() != interval) {
                    existingTask.cancel();
                    tasks.put(key, createAndScheduleTask(key, interval));
                }
            } else {
                tasks.put(key, createAndScheduleTask(key, interval));
            }
        }
    }

    private AnnouncementTask createAndScheduleTask(String key, int interval) {
        AnnouncementTask task = new AnnouncementTask(key, interval);
        task.runTaskTimerAsynchronously(plugin, 0L, interval * 20L);
        return task;
    }

    private void sendAnnouncement(String key) {
        ConfigurationSection announcement = plugin.getLocales().getConfig().getConfigurationSection("ANNOUNCEMENTS." + key);
        if (announcement == null) return;

        boolean requirePermission = announcement.getBoolean("REQUIRE-PERMISSION", false);
        String permission = announcement.getString("PERMISSION", "");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!requirePermission || player.hasPermission(permission)) {
                sendAnnouncementToPlayer(player, announcement);
            }
        }
    }

    private void sendAnnouncementToPlayer(Player player, ConfigurationSection announcement) {
        List<String> text = announcement.getStringList("TEXT");
        boolean hover = announcement.getBoolean("HOVER", false);
        List<String> hoverContent = announcement.getStringList("HOVER-CONTENT");
        boolean clickCommand = announcement.getBoolean("CLICK-COMMAND", false);
        String command = announcement.getString("COMMAND", "");

        boolean suggestCommand = announcement.getBoolean("SUGGEST-COMMAND", false);
        String commandToSuggest = announcement.getString("COMMAND-TO-SUGGEST", "");

        boolean playSound = announcement.getBoolean("PLAY-SOUND", false);
        String soundName = announcement.getString("SOUND", "");

        boolean openUrl = announcement.getBoolean("OPEN-URL", false);
        String url = announcement.getString("URL", "");

        String message = String.join("\n", text);
        message = ColorHandler.color(message);

        BaseComponent[] components = new TextComponent[]{new TextComponent(message)};


        if (hover && hoverContent != null) {
            components = addHoverEffect(components, hoverContent, player);
        }


        if (clickCommand && command != null && !command.isEmpty()) {
            components = addClickCommand(components, command);
        } else if (suggestCommand && commandToSuggest != null && !commandToSuggest.isEmpty()) {
            components = addSuggestCommand(components, commandToSuggest);
        } else if (openUrl && url != null && !url.isEmpty()) {
            components = addOpenUrl(components, url);
        }

        player.spigot().sendMessage(components);

        if (playSound) {
            if (plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                if (playSound) {
                    SoundUtil.playSound(player, soundName, 1.0f, 1.0f);
                }
            } else if (plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                if (playSound) {
                    XSounds.playSound(player, soundName, 1.0f, 1.0f);
                }
            }
        }
    }

    private BaseComponent[] addHoverEffect(BaseComponent[] components, List<String> hoverContent, Player player) {
        String hoverText = hoverContent.stream()
                .map(line -> ColorHandler.color(line))
                .reduce((line1, line2) -> line1 + "\n" + line2)
                .orElse("");

        for (BaseComponent component : components) {
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
        }
        return components;
    }

    private BaseComponent[] addClickCommand(BaseComponent[] components, String command) {
        for (BaseComponent component : components) {
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        }
        return components;
    }

    private BaseComponent[] addSuggestCommand(BaseComponent[] components, String commandToSuggest) {
        for (BaseComponent component : components) {
            component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, commandToSuggest));
        }
        return components;
    }

    private BaseComponent[] addOpenUrl(BaseComponent[] components, String url) {
        for (BaseComponent component : components) {
            component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        }
        return components;
    }

    /**
     * Inner class for managing individual announcement tasks with an interval.
     */
    private class AnnouncementTask extends BukkitRunnable {
        private final String key;
        private final int interval;

        public AnnouncementTask(String key, int interval) {
            this.key = key;
            this.interval = interval;
        }

        public int getInterval() {
            return interval;
        }

        @Override
        public void run() {
            sendAnnouncement(key);
        }
    }
}
