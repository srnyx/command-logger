package com.srnyx.commandlogger.stats;

import com.srnyx.commandlogger.CommandLogger;
import dev.faststats.Metrics;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.stats.loader.FastStatsLoader;


public class FastStats extends FastStatsLoader {
    @NotNull private final CommandLogger plugin;

    public FastStats(@NotNull CommandLogger plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public CommandLogger getAnnoyingPlugin() {
        return plugin;
    }

    @Override @NotNull
    public String getId() {
        return "0d6d5373dedf2d5e41048a1f3e2616ad";
    }

    @Override
    public void mutateMetricsFactory(@NotNull Metrics.Factory factory) {
        factory.addMetric(config("config", () -> plugin.config));
    }
}
