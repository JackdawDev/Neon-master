package dev.jackdaw1101.neon.AddonHandler;

import dev.jackdaw1101.neon.Utils.Chat.CC;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class AddonManager {

    private final Logger logger;
    private final Map<String, AddonInfo> registeredAddons;

    public AddonManager(Logger logger) {
        this.logger = logger;
        this.registeredAddons = new HashMap<>();
    }

    /**
     * Registers an addon by its name and version.
     *
     * @param addonName    The name of the addon.
     * @param addonVersion The version of the addon.
     * @return True if the addon was successfully registered, false if it was already registered.
     */
    public boolean registerAddon(String addonName, String addonVersion) {
        if (registeredAddons.containsKey(addonName)) {
            Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Addon " + addonName + " is already registered.");
            return false;
        }

        registeredAddons.put(addonName, new AddonInfo(addonName, addonVersion));
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Addon " + addonName + " (v" + addonVersion + ") registered successfully.");
        return true;
    }

    /**
     * Retrieves information about a registered addon.
     *
     * @param addonName The name of the addon.
     * @return The AddonInfo object, or null if the addon is not registered.
     */
    public AddonInfo getAddon(String addonName) {
        return registeredAddons.get(addonName);
    }

    /**
     * Checks if an addon is registered.
     *
     * @param addonName The name of the addon.
     * @return True if the addon is registered, false otherwise.
     */
    public boolean isAddonRegistered(String addonName) {
        return registeredAddons.containsKey(addonName);
    }

    /**
     * @return A map of all registered addons.
     */
    public Map<String, AddonInfo> getAllAddons() {
        return new HashMap<>(registeredAddons);
    }

    // Inner class to hold addon details
    public static class AddonInfo {
        private final String name;
        private final String version;

        public AddonInfo(String name, String version) {
            this.name = name;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            return "AddonInfo{name='" + name + "', version='" + version + "'}";
        }
    }
}
