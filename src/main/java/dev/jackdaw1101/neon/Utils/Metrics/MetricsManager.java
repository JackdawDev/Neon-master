package dev.jackdaw1101.neon.Utils.Metrics;

import dev.jackdaw1101.neon.Neon;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

import java.util.concurrent.Callable;

public class MetricsManager {

    private static dev.jackdaw1101.neon.Utils.Metrics.MetricsManager instance;

    private final Metrics metrics;

    private MetricsManager(Neon plugin) {
        metrics = new Metrics(plugin, 25630);

        metrics.addCustomChart(new SimplePie("isound", () -> String.valueOf(plugin.getSettings().getBoolean("ISOUNDS-UTIL"))));
        metrics.addCustomChart(new SimplePie("xsound", () -> String.valueOf(plugin.getSettings().getBoolean("XSOUNDS-UTIL"))));
        metrics.addCustomChart(new SimplePie("chat_format", () -> String.valueOf(plugin.getSettings().getBoolean("CHAT-FORMAT-ENABLED"))));
        metrics.addCustomChart(new SimplePie("database", () -> String.valueOf(plugin.getDatabaseManager().getString("DATABASE.TYPE"))));
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public static void appendPie(String id, Callable<String> callable) {
        if (null == instance) {
            throw new RuntimeException("Metrics manager is not initialized!");
        }
        instance.getMetrics().addCustomChart(new SimplePie(id, callable));
    }

    public static void initService(Neon plugin) {
        if (null != instance) {
            return;
        }
        instance = new MetricsManager(plugin);
    }
}
