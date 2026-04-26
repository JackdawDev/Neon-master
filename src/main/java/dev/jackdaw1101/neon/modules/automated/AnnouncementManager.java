package dev.jackdaw1101.neon.modules.automated;

import dev.jackdaw1101.neon.API.utilities.CC;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.utils.DebugUtil;
import dev.jackdaw1101.neon.utils.sounds.ISound;
import dev.jackdaw1101.neon.utils.sounds.XSounds;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnnouncementManager {

    private final Neon plugin;
    private final Map<String, AnnouncementTask> tasks = new ConcurrentHashMap<>();
    private final Map<String, Long> lastSendTime = new ConcurrentHashMap<>();
    private BukkitTask schedulerTask;
    private boolean enabled;
    private long lastReloadCheck = 0;
    private long lastKnownLocaleModification = 0;

    private final Map<String, CachedAnnouncement> announcementCache = new ConcurrentHashMap<>();

    public AnnouncementManager(Neon plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Reload the announcement manager
     */
    public void reload() {

        stopAllAnnouncements();

        announcementCache.clear();
        lastSendTime.clear();

        this.enabled = plugin.getSettings().getBoolean("ANNOUNCEMENTS.ENABLED");

        if (!enabled) {
            if (plugin.getSettings().getBoolean("DEBUG-MODE")) {
                plugin.getLogger().info("Announcements are disabled in config.");
            }
            return;
        }

        loadAnnouncements();

        startDynamicScheduler();

        updateLastModificationTime();

        DebugUtil.debugChecked("&aAnnouncement manager reloaded with " + tasks.size() + " announcements");

    }

    private void updateLastModificationTime() {
        try {

            java.io.File localeFile = new java.io.File(plugin.getDataFolder(), "locale.yml");
            if (localeFile.exists()) {
                lastKnownLocaleModification = localeFile.lastModified();
            }
        } catch (Exception e) {

        }
        lastReloadCheck = System.currentTimeMillis();
    }

    private boolean shouldReload() {

        if (System.currentTimeMillis() - lastReloadCheck < 30000) {
            return false;
        }

        try {
            java.io.File localeFile = new java.io.File(plugin.getDataFolder(), "locale.yml");
            if (localeFile.exists() && localeFile.lastModified() > lastKnownLocaleModification) {
                lastKnownLocaleModification = localeFile.lastModified();
                return true;
            }
        } catch (Exception e) {


        }

        lastReloadCheck = System.currentTimeMillis();
        return false;
    }

    private void loadAnnouncements() {
        ConfigurationSection announcementsSection = plugin.getLocales().getConfig().getConfigurationSection("ANNOUNCEMENTS");

        if (announcementsSection == null) {
            DebugUtil.debugErrorChecked("No announcements section found in locale.yml");
            return;
        }

        for (String key : announcementsSection.getKeys(false)) {
            ConfigurationSection announcement = announcementsSection.getConfigurationSection(key);
            if (announcement == null) continue;

            cacheAnnouncement(key, announcement);
        }
    }

    private void cacheAnnouncement(String key, ConfigurationSection section) {

        List<String> text = section.getStringList("TEXT");
        if (text == null || text.isEmpty()) {
                DebugUtil.debugInfoChecked("Announcement '" + key + "' has no text, skipping");
            return;
        }

        int interval = section.getInt("INTERVAL", 60);
        if (interval < 5) {
            interval = 60;
            DebugUtil.debugInfoChecked("Announcement '" + key + "' interval too low, set to 60");

        }

        CachedAnnouncement cached = new CachedAnnouncement(
                key,
                interval,
                section.getBoolean("REQUIRE-PERMISSION", false),
                section.getString("PERMISSION", ""),
                text,
                section.getBoolean("HOVER", false),
                section.getStringList("HOVER-CONTENT"),
                section.getBoolean("CLICK-COMMAND", false),
                section.getString("COMMAND", ""),
                section.getBoolean("SUGGEST-COMMAND", false),
                section.getString("COMMAND-TO-SUGGEST", ""),
                section.getBoolean("PLAY-SOUND", false),
                section.getString("SOUND", ""),
                section.getBoolean("OPEN-URL", false),
                section.getString("URL", "")
        );

        announcementCache.put(key, cached);

        AnnouncementTask task = new AnnouncementTask(cached);
        task.runTaskTimerAsynchronously(plugin, getInitialDelay(interval), interval * 20L);
        tasks.put(key, task);

        if (plugin.getSettings().getBoolean("DEBUG-MODE")) {
            DebugUtil.debugChecked("&aLoaded announcement: " + key + " (interval: " + interval + "s)");
        }
    }

    private long getInitialDelay(int interval) {

        return (long) (Math.random() * interval * 20);
    }

    private void startDynamicScheduler() {
        if (schedulerTask != null && !schedulerTask.isCancelled()) {
            schedulerTask.cancel();
        }

        schedulerTask = new BukkitRunnable() {
            @Override
            public void run() {

                if (shouldReload()) {

                    Bukkit.getScheduler().runTask(plugin, AnnouncementManager.this::reload);
                    return;
                }

                checkForUpdates();
            }
        }.runTaskTimerAsynchronously(plugin, 100L, 100L);
    }

    private void checkForUpdates() {
        ConfigurationSection announcementsSection = plugin.getLocales().getConfig().getConfigurationSection("ANNOUNCEMENTS");
        if (announcementsSection == null) return;

        tasks.keySet().removeIf(key -> {
            if (!announcementsSection.contains(key) && announcementCache.containsKey(key)) {
                tasks.get(key).cancel();
                announcementCache.remove(key);
                    DebugUtil.debugChecked("Removed announcement: " + key);
                return true;
            }
            return false;
        });

        for (String key : announcementsSection.getKeys(false)) {
            ConfigurationSection section = announcementsSection.getConfigurationSection(key);
            if (section == null) continue;

            int interval = section.getInt("INTERVAL", 60);

            if (!announcementCache.containsKey(key)) {

                cacheAnnouncement(key, section);
                    DebugUtil.debugChecked("Added new announcement: " + key);
            } else {

                CachedAnnouncement existing = announcementCache.get(key);
                if (existing != null && existing.hasChanged(section)) {

                    tasks.get(key).cancel();
                    announcementCache.remove(key);
                    cacheAnnouncement(key, section);
                        DebugUtil.debugChecked("Updated announcement: " + key);
                }
            }
        }
    }

    private void stopAllAnnouncements() {
        tasks.values().forEach(task -> {
            try {
                task.cancel();
            } catch (Exception e) {

            }
        });
        tasks.clear();

        if (schedulerTask != null && !schedulerTask.isCancelled()) {
            schedulerTask.cancel();
            schedulerTask = null;
        }
    }

    private void sendAnnouncement(CachedAnnouncement cached) {

        long now = System.currentTimeMillis();
        Long lastSend = lastSendTime.get(cached.getKey());
        if (lastSend != null && now - lastSend < (cached.getInterval() * 1000L) - 1000) {
            return; // Skip if sent too recently
        }
        lastSendTime.put(cached.getKey(), now);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!cached.isRequirePermission() || player.hasPermission(cached.getPermission())) {
                sendAnnouncementToPlayer(player, cached);
            }
        }
    }

    private void sendAnnouncementToPlayer(Player player, CachedAnnouncement cached) {
        boolean isDebug = DebugUtil.isDebugEnabled();

        try {

            String message = String.join("\n", cached.getText());
            message = ColorHandler.color(message);

            BaseComponent[] components = new TextComponent[]{new TextComponent(message)};

            if (cached.isHover() && cached.getHoverContent() != null && !cached.getHoverContent().isEmpty()) {
                components = addHoverEffect(components, cached.getHoverContent(), player);
            }

            if (cached.isClickCommand() && cached.getCommand() != null && !cached.getCommand().isEmpty()) {
                components = addClickCommand(components, cached.getCommand());
            } else if (cached.isSuggestCommand() && cached.getCommandToSuggest() != null && !cached.getCommandToSuggest().isEmpty()) {
                components = addSuggestCommand(components, cached.getCommandToSuggest());
            } else if (cached.isOpenUrl() && cached.getUrl() != null && !cached.getUrl().isEmpty()) {
                components = addOpenUrl(components, cached.getUrl());
            }

            player.spigot().sendMessage(components);

            if (cached.isPlaySound() && cached.getSoundName() != null && !cached.getSoundName().isEmpty()) {
                playSound(player, cached.getSoundName());
            }
        } catch (Exception e) {
            if (isDebug) {
                DebugUtil.debug("Error sending announcement '" + cached.getKey() + "' to " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    private void playSound(Player player, String soundName) {
        try {
            if (plugin.getSettings().getBoolean("ISOUNDS-UTIL")) {
                ISound.playSound(player, soundName, 1.0f, 1.0f);
            } else if (plugin.getSettings().getBoolean("XSOUNDS-UTIL")) {
                XSounds.playSound(player, soundName, 1.0f, 1.0f);
            }
        } catch (Exception e) {
            if (plugin.getSettings().getBoolean("DEBUG-MODE")) {
                DebugUtil.debug("Failed to play sound '" + soundName + "': " + e.getMessage());
            }
        }
    }

    private BaseComponent[] addHoverEffect(BaseComponent[] components, List<String> hoverContent, Player player) {
        String hoverText = hoverContent.stream()
                .map(ColorHandler::color)
                .reduce((line1, line2) -> line1 + "\n" + line2)
                .orElse("");

        for (BaseComponent component : components) {
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(hoverText).create()));
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
     * Clean up resources when plugin disables
     */
    public void shutdown() {
        stopAllAnnouncements();
        announcementCache.clear();
        lastSendTime.clear();
    }

    /**
     * Inner class for caching announcement data
     */
    private static class CachedAnnouncement {
        private final String key;
        private final int interval;
        private final boolean requirePermission;
        private final String permission;
        private final List<String> text;
        private final boolean hover;
        private final List<String> hoverContent;
        private final boolean clickCommand;
        private final String command;
        private final boolean suggestCommand;
        private final String commandToSuggest;
        private final boolean playSound;
        private final String soundName;
        private final boolean openUrl;
        private final String url;

        public CachedAnnouncement(String key, int interval, boolean requirePermission, String permission,
                                  List<String> text, boolean hover, List<String> hoverContent,
                                  boolean clickCommand, String command, boolean suggestCommand,
                                  String commandToSuggest, boolean playSound, String soundName,
                                  boolean openUrl, String url) {
            this.key = key;
            this.interval = interval;
            this.requirePermission = requirePermission;
            this.permission = permission != null ? permission : "";
            this.text = text != null ? text : java.util.Collections.emptyList();
            this.hover = hover;
            this.hoverContent = hoverContent != null ? hoverContent : java.util.Collections.emptyList();
            this.clickCommand = clickCommand;
            this.command = command != null ? command : "";
            this.suggestCommand = suggestCommand;
            this.commandToSuggest = commandToSuggest != null ? commandToSuggest : "";
            this.playSound = playSound;
            this.soundName = soundName != null ? soundName : "";
            this.openUrl = openUrl;
            this.url = url != null ? url : "";
        }

        public String getKey() { return key; }
        public int getInterval() { return interval; }
        public boolean isRequirePermission() { return requirePermission; }
        public String getPermission() { return permission; }
        public List<String> getText() { return text; }
        public boolean isHover() { return hover; }
        public List<String> getHoverContent() { return hoverContent; }
        public boolean isClickCommand() { return clickCommand; }
        public String getCommand() { return command; }
        public boolean isSuggestCommand() { return suggestCommand; }
        public String getCommandToSuggest() { return commandToSuggest; }
        public boolean isPlaySound() { return playSound; }
        public String getSoundName() { return soundName; }
        public boolean isOpenUrl() { return openUrl; }
        public String getUrl() { return url; }

        public boolean hasChanged(ConfigurationSection section) {
            return interval != section.getInt("INTERVAL", 60) ||
                    requirePermission != section.getBoolean("REQUIRE-PERMISSION", false) ||
                    !permission.equals(section.getString("PERMISSION", "")) ||
                    hover != section.getBoolean("HOVER", false) ||
                    clickCommand != section.getBoolean("CLICK-COMMAND", false) ||
                    !command.equals(section.getString("COMMAND", "")) ||
                    suggestCommand != section.getBoolean("SUGGEST-COMMAND", false) ||
                    !commandToSuggest.equals(section.getString("COMMAND-TO-SUGGEST", "")) ||
                    playSound != section.getBoolean("PLAY-SOUND", false) ||
                    !soundName.equals(section.getString("SOUND", "")) ||
                    openUrl != section.getBoolean("OPEN-URL", false) ||
                    !url.equals(section.getString("URL", ""));
        }
    }

    /**
     * Inner class for managing individual announcement tasks
     */
    private class AnnouncementTask extends BukkitRunnable {
        private final CachedAnnouncement cached;

        public AnnouncementTask(CachedAnnouncement cached) {
            this.cached = cached;
        }

        public int getInterval() {
            return cached.getInterval();
        }

        @Override
        public void run() {

            if (!enabled) {
                this.cancel();
                return;
            }

            sendAnnouncement(cached);
        }
    }
}