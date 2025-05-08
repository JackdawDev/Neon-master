package dev.jackdaw1101.neon.API.addons;

import dev.jackdaw1101.neon.API.utilities.CC;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the registration and lifecycle of Neon addons
 */
public class AddonManager {
    private final Logger logger;
    private final Map<String, AddonInfo> registeredAddons;
    private final Map<String, Class<?>> addonMainClasses;

    public AddonManager(Logger logger) {
        this.logger = logger;
        this.registeredAddons = new HashMap<>();
        this.addonMainClasses = new HashMap<>();
    }

    /**
     * Registers an addon with the system
     * @param addonName The name of the addon
     * @param version The version of the addon
     * @param mainClass The main class of the addon
     * @return true if registration was successful
     */
    public boolean registerAddon(String addonName, String version, Class<?> mainClass) {
        if (addonName == null || version == null || mainClass == null) {
            logger.log(Level.WARNING, "Failed to register addon - parameters cannot be null");
            return false;
        }

        if (registeredAddons.containsKey(addonName)) {
            logger.log(Level.WARNING, "Addon " + addonName + " is already registered");
            return false;
        }

        registeredAddons.put(addonName, new AddonInfo(addonName, version));
        addonMainClasses.put(addonName, mainClass);

        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Addon " + addonName +
            " v" + version + " registered successfully");
        return true;
    }

    /**
     * Unregisters an addon
     * @param addonName The name of the addon to unregister
     * @return true if the addon was successfully unregistered
     */
    public boolean unregisterAddon(String addonName) {
        if (!registeredAddons.containsKey(addonName)) {
            return false;
        }

        registeredAddons.remove(addonName);
        addonMainClasses.remove(addonName);
        logger.log(Level.INFO, "Addon " + addonName + " has been unregistered");
        return true;
    }

    /**
     * Gets information about a registered addon
     * @param addonName The name of the addon
     * @return AddonInfo object or null if not found
     */
    public AddonInfo getAddonInfo(String addonName) {
        return registeredAddons.get(addonName);
    }

    /**
     * Gets the main class of an addon
     * @param addonName The name of the addon
     * @return The main class or null if not found
     */
    public Class<?> getAddonMainClass(String addonName) {
        return addonMainClasses.get(addonName);
    }

    /**
     * Gets the version of an addon
     * @param addonName The name of the addon
     * @return The version string or null if not found
     */
    public String getAddonVersion(String addonName) {
        AddonInfo info = registeredAddons.get(addonName);
        return info != null ? info.getVersion() : null;
    }

    /**
     * Checks if an addon is registered
     * @param addonName The name of the addon to check
     * @return true if the addon is registered
     */
    public boolean isAddonRegistered(String addonName) {
        return registeredAddons.containsKey(addonName);
    }

    /**
     * Gets a list of all registered addon names
     * @return List of addon names
     */
    public List<String> getRegisteredAddonNames() {
        return new ArrayList<>(registeredAddons.keySet());
    }

    /**
     * Gets a map of all registered addons
     * @return Unmodifiable map of addons
     */
    public Map<String, AddonInfo> getAllAddons() {
        return Collections.unmodifiableMap(registeredAddons);
    }

    /**
     * Inner class to hold addon information
     */
    public static class AddonInfo {
        private final String name;
        private final String version;
        private final long registrationTime;

        public AddonInfo(String name, String version) {
            this.name = name;
            this.version = version;
            this.registrationTime = System.currentTimeMillis();
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public long getRegistrationTime() {
            return registrationTime;
        }

        public String getFormattedUptime() {
            long seconds = (System.currentTimeMillis() - registrationTime) / 1000;
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        }

        @Override
        public String toString() {
            return CC.GRAY + "Addon Name: " + CC.YELLOW + name + "\n" + CC.GRAY + "Version: " + CC.YELLOW + version;
        }
    }
}
