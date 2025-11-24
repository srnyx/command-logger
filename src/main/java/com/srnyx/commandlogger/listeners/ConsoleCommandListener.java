package com.srnyx.commandlogger.listeners;

import com.srnyx.commandlogger.CommandLogger;
import com.srnyx.commandlogger.InfoForVariables;
import com.srnyx.commandlogger.config.ConfigLogger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerCommandEvent;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingListener;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;


public class ConsoleCommandListener extends AnnoyingListener {
    @NotNull private final CommandLogger plugin;

    public ConsoleCommandListener(@NotNull CommandLogger plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public CommandLogger getAnnoyingPlugin() {
        return plugin;
    }

    @EventHandler
    public void onServerCommand(@NotNull ServerCommandEvent event) {
        if (!plugin.config.console.enabled) {
            unregister();
            return;
        }
        final String command = event.getCommand();

        // Check all filter
        if (plugin.config.filters != null && plugin.config.filters.doesNotPass(command)) return;

        // Check console filter
        if (plugin.config.console.filters != null && plugin.config.console.filters.doesNotPass(command)) return;

        final InfoForVariables info = new InfoForVariables(event);

        // Combined loggers
        for (final ConfigLogger logger : plugin.config.loggers) {
            // Check filter
            if (logger.filters != null && logger.filters.doesNotPass(command)) return;

            // Add to log
            try {
                final Path file = logger.filePath(info);
                Files.createDirectories(file.getParent());
                Files.write(
                        file,
                        logger.format(info).getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (final Exception e) {
                AnnoyingPlugin.log(Level.WARNING, "&cFailed to write to combined command log file for a console command!", e);
            }
        }

        // Console loggers
        for (final ConfigLogger logger : plugin.config.console.loggers) {
            // Check filter
            if (logger.filters != null && logger.filters.doesNotPass(command)) continue;

            // Add to log
            try {
                final Path file = logger.filePath(info);
                Files.createDirectories(file.getParent());
                Files.write(
                        file,
                        logger.format(info).getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (final Exception e) {
                AnnoyingPlugin.log(Level.WARNING, "&cFailed to write to console command log file!", e);
            }
        }
    }
}
