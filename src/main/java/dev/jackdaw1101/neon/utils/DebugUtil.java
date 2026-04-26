package dev.jackdaw1101.neon.utils;


import dev.jackdaw1101.neon.API.utilities.ColorHandler;
import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.CC;
import org.bukkit.Bukkit;

public class DebugUtil {
    public static void checkLoadedClasses(Neon plugin) {
        boolean debug = (boolean) plugin.getSettings().getBoolean("DEBUG-MODE");

        if (debug) {
            int loadedClasses = getLoadedClassCount();

            Bukkit.getLogger().info(CC.GRAY + "Loaded classes: " + CC.YELLOW + loadedClasses + CC.GRAY + " (in this jar totally)");
        }
    }

    public void stopDebugUtil() {
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Stopping Debug Util...");
        Bukkit.getConsoleSender().sendMessage(CC.GRAY + "[Neon] Successfully Stopped Debug Util.");
    }

        private static int getLoadedClassCount() {
        return java.lang.management.ManagementFactory.getClassLoadingMXBean().getLoadedClassCount();
    }

    public static boolean isDebugEnabled() {
        if (!Neon.getInstance().getSettings().getBoolean("DEBUG-MODE")) {
            return false;
        } else {
            return true;
        }
    }

    public static String getDebugVersion(String neonVer) {
        if (neonVer.equalsIgnoreCase(Neon.getInstance().getNeonAPI().getNeonVersion())) {
            return "2.0-BETA";
        } else {
            return "Unable to get";
        }
    }

    public static void debug(String message) {
        if (message.contains("[Neon]") || message.contains("[Neon-Debug]")) {
            Bukkit.getConsoleSender().sendMessage(ColorHandler.color(message));
        } else {
            Bukkit.getConsoleSender().sendMessage(ColorHandler.color("&7[Neon-Debug] " + message));
        }
    }

    public static void debugNoPrefix(String message) {
        if (message.contains("[Neon]") || message.contains("[Neon-Debug]")) {
            Bukkit.getConsoleSender().sendMessage(ColorHandler.color(message));
        } else {
            Bukkit.getConsoleSender().sendMessage(ColorHandler.color("&7[Neon-Debug] " + message));
        }
    }

    public static void debugChecked(String message) {
        if (isDebugEnabled()) {
            debug(message);
        }
    }

    public static void debugNoPrefixChecked(String message) {
        if (isDebugEnabled()) {
            debugNoPrefix(message);
        }
    }

    public static void debugError(String error) {
        Bukkit.getConsoleSender().sendMessage(ColorHandler.color("&c[Neon] " + error));
    }

    public static void debugErrorChecked(String error) {
        if (isDebugEnabled()) {
           Bukkit.getConsoleSender().sendMessage(ColorHandler.color("&c[Neon] " + error));
        }
    }

    public static void debugInfo(String info) {
        Bukkit.getConsoleSender().sendMessage(ColorHandler.color("&7[Neon] " + info));
    }

    public static void debugInfoChecked(String info) {
        if (isDebugEnabled()) {
            Bukkit.getConsoleSender().sendMessage(ColorHandler.color("&7[Neon] " + info));
        }
    }
}
