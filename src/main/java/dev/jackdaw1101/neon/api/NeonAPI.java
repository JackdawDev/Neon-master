package dev.jackdaw1101.neon.api;

import dev.jackdaw1101.neon.addonhandler.AddonManager;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.api.utils.ColorHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

/**
 * The main API for Neon addon development.
 * Allows developers to create and manage addons with custom configurations.
 */
public class NeonAPI {
    private final AddonManager addonManager;
    private final Neon plugin;

    public NeonAPI(Neon plugin, AddonManager addonManager) {
        this.plugin = plugin;
        this.addonManager = addonManager;
    }

    /**
     * Creates a new addon for Neon
     * @param addonName The name of the addon
     * @param version The version of the addon
     * @param mainClass The main class of the addon
     * @return true if registration was successful
     */
    public boolean registerAddon(String addonName, String version, Class<?> mainClass) {
        if (addonName == null || version == null || mainClass == null) {
            plugin.getLogger().log(Level.WARNING, "Failed to register addon - parameters cannot be null");
            return false;
        }

        return addonManager.registerAddon(addonName, version, mainClass);
    }

    /**
     * Gets a list of all registered addon names
     * @return List of addon names
     */
    public List<String> getRegisteredAddons() {
        return addonManager.getRegisteredAddonNames();
    }

    /**
     * Checks if an addon is registered
     * @param addonName The name of the addon to check
     * @return true if the addon is registered
     */
    public boolean isAddonRegistered(String addonName) {
        return addonManager.isAddonRegistered(addonName);
    }

    /**
     * Creates a configuration file for an addon
     * @param addonName The name of the addon
     * @param fileName The name of the config file (without .yml extension)
     * @return The YamlConfiguration object
     */
    public YamlConfiguration createAddonConfig(String addonName, String fileName) {
        File addonFolder = new File(plugin.getDataFolder(), "Addons" + File.separator + addonName);
        if (!addonFolder.exists()) {
            addonFolder.mkdirs();
        }

        File configFile = new File(addonFolder, fileName + ".yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create config file for addon " + addonName, e);
                return null;
            }
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Gets an existing addon configuration file
     * @param addonName The name of the addon
     * @param fileName The name of the config file (without .yml extension)
     * @return The YamlConfiguration object, or null if not found
     */
    public YamlConfiguration getAddonConfig(String addonName, String fileName) {
        File configFile = new File(plugin.getDataFolder(),
            "Addons" + File.separator + addonName + File.separator + fileName + ".yml");

        if (!configFile.exists()) {
            return null;
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Saves an addon's configuration file
     * @param config The configuration to save
     * @param addonName The name of the addon
     * @param fileName The name of the config file (without .yml extension)
     * @return true if the save was successful
     */
    public boolean saveAddonConfig(YamlConfiguration config, String addonName, String fileName) {
        try {
            File configFile = new File(plugin.getDataFolder(),
                "Addons" + File.separator + addonName + File.separator + fileName + ".yml");
            config.save(configFile);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save config for addon " + addonName, e);
            return false;
        }
    }

    /**
     * Creates a subfolder for an addon
     * @param addonName The name of the addon
     * @param folderName The name of the subfolder to create
     * @return true if the folder was created or already exists
     */
    public boolean createAddonSubfolder(String addonName, String folderName) {
        File folder = new File(plugin.getDataFolder(),
            "Addons" + File.separator + addonName + File.separator + folderName);

        if (!folder.exists()) {
            return folder.mkdirs();
        }
        return true;
    }

    /**
     * Gets a subfolder for an addon
     * @param addonName The name of the addon
     * @param folderName The name of the subfolder
     * @return The File object representing the folder, or null if it doesn't exist
     */
    public File getAddonSubfolder(String addonName, String folderName) {
        File folder = new File(plugin.getDataFolder(),
            "Addons" + File.separator + addonName + File.separator + folderName);

        return folder.exists() ? folder : null;
    }

    /**
     * Gets the version of an addon
     * @param addonName The name of the addon
     * @return The version string, or null if addon isn't registered
     */
    public String getAddonVersion(String addonName) {
        return addonManager.getAddonVersion(addonName);
    }

    /**
     * Gets the main class of an addon
     * @param addonName The name of the addon
     * @return The main class, or null if addon isn't registered
     */
    public Class<?> getAddonMainClass(String addonName) {
        return addonManager.getAddonMainClass(addonName);
    }

    /**
     * Gets The neon prefix
     * @return The string
     */
    public String getNeonPrefix() {
        return ColorHandler.color(Neon.getInstance().getMessageManager().getString("PREFIX"));
    }

    /**
     * Gets The neon main theme
     * @return The string
     */
    public String getNeonMainTheme() {
        return ColorHandler.color(Neon.getInstance().getMessageManager().getString("MAIN-THEME"));
    }

    /**
     * Gets The neon second theme
     * @return The string
     */
    public String getNeonSecondTheme() {
        return ColorHandler.color(Neon.getInstance().getMessageManager().getString("SECOND-THEME"));
    }

    /**
     * Gets The neon third theme
     * @return The string
     */
    public String getNeonThirdTheme() {
        return ColorHandler.color(Neon.getInstance().getMessageManager().getString("THIRD-THEME"));
    }

    /**
     * Gets The neon Version
     * @return The string
     */
    public String getNeonVersion() {
        return ColorHandler.color(Neon.getInstance().getDescription().getVersion());
    }

    /**
     * Shuts down the API and cleans up resources
     */
    public void shutdown() {
        Bukkit.getConsoleSender().sendMessage("[Neon] Shutting down API...");
        Bukkit.getConsoleSender().sendMessage("[Neon] API shutdown complete.");
    }
}
