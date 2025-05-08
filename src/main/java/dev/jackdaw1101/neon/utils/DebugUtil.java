package dev.jackdaw1101.neon.utils;


import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.CC;
import org.bukkit.Bukkit;

public class DebugUtil {
    public static void checkDebug(Neon plugin) {
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
}
