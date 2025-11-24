package com.srnyx.commandlogger;

import com.srnyx.commandlogger.config.ConfigYml;
import com.srnyx.commandlogger.listeners.ConsoleCommandListener;
import com.srnyx.commandlogger.listeners.PlayerCommandListener;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.PluginPlatform;


public class CommandLogger extends AnnoyingPlugin {
    public ConfigYml config;
    @NotNull private final PlayerCommandListener playerCommandListener = new PlayerCommandListener(this);
    @NotNull private final ConsoleCommandListener consoleCommandListener = new ConsoleCommandListener(this);

    public CommandLogger() {
        options
                .pluginOptions(pluginOptions -> pluginOptions.updatePlatforms(new PluginPlatform.Multi(
                        PluginPlatform.modrinth("PMWx2eoO"),
                        PluginPlatform.hangar(this),
                        PluginPlatform.spigot("126150"))))
                .bStatsOptions(bStatsOptions -> bStatsOptions.id(26170))
                .registrationOptions.automaticRegistration.packages("com.srnyx.commandlogger.commands");
    }

    @Override
    public void enable() {
        reload();
    }

    @Override
    public void reload() {
        config = new ConfigYml(this);
        playerCommandListener.setRegistered(config.players.enabled && !config.players.loggers.isEmpty());
        consoleCommandListener.setRegistered(config.console.enabled && !config.console.loggers.isEmpty());
    }
}
