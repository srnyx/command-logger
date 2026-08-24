package com.srnyx.commandlogger;

import com.srnyx.commandlogger.config.ConfigYml;
import com.srnyx.commandlogger.config.migration.C0001_Combined_to_loggers;
import com.srnyx.commandlogger.config.migration.C0002_Splits_to_loggers;
import com.srnyx.commandlogger.config.migration.C0003_String_filters_to_objects;
import com.srnyx.commandlogger.config.migration.C0004_players_filter_to_players_filters;
import com.srnyx.commandlogger.config.serdes.SimpleDateFormatSerializer;
import com.srnyx.commandlogger.listeners.ConsoleCommandListener;
import com.srnyx.commandlogger.listeners.PlayerCommandListener;
import com.srnyx.commandlogger.messages.CLMessagesProvider;
import com.srnyx.commandlogger.stats.FastStats;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.nio.file.Path;


public class CommandLogger extends AnnoyingPlugin {
    public ConfigYml config;
    @NotNull public final Path logsFolder = getDataFolder().toPath().resolve("logs");
    @NotNull public final PlayerCommandListener playerCommandListener = new PlayerCommandListener(this);
    @NotNull public final ConsoleCommandListener consoleCommandListener = new ConsoleCommandListener(this);

    public CommandLogger() {
        options.statsOptions(statsOptions -> statsOptions
                .bStats(bStatsOptions -> bStatsOptions.id(26170))
                .fastStats(fastStatsOptions -> fastStatsOptions.loader(FastStats.class)));
    }

    @Override @NotNull
    public CLMessagesProvider getMessages() {
        return (CLMessagesProvider) super.getMessages();
    }

    @Override
    public void load() {
        config = configLoader.build(builder -> builder
                .config(new ConfigYml(this))
                .configure(configure -> configure.serdes(new SimpleDateFormatSerializer()))
                .internalStateMigrations(
                        new C0001_Combined_to_loggers(),
                        new C0002_Splits_to_loggers(),
                        new C0003_String_filters_to_objects(),
                        new C0004_players_filter_to_players_filters()));
    }

    @Override
    public void enable() {
        registerListeners();
    }

    @Override
    public void reload() {
        config.reload();
        registerListeners();
    }

    private void registerListeners() {
        playerCommandListener.setRegistered(config.enabled && config.players.enabled && !config.players.loggers.isEmpty());
        consoleCommandListener.setRegistered(config.enabled && config.console.enabled && !config.console.loggers.isEmpty());
    }
}
