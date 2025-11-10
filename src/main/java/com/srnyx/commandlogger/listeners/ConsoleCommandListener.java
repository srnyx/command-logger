package com.srnyx.commandlogger.listeners;

import com.srnyx.commandlogger.CommandLogger;
import com.srnyx.commandlogger.config.Split;

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
        if (plugin.config.filter != null && plugin.config.filter.matcher(command).matches()) return;

        // Check console filter
        if (plugin.config.console.filter != null && plugin.config.console.filter.matcher(command).matches()) return;

        // Combined
        if (plugin.config.combined.enabled) {
            // Check filter
            if (plugin.config.combined.filter != null && plugin.config.combined.filter.matcher(command).matches()) return;

            // Add to log
            try {
                Files.createDirectories(plugin.config.combined.file.getParent());
                Files.write(
                        plugin.config.combined.file,
                        plugin.config.combined.format(event).getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (final Exception e) {
                AnnoyingPlugin.log(Level.WARNING, "&cFailed to write to combined command log file for a console command!", e);
            }
        }

        // Combined
        if (plugin.config.console.combined.enabled) {
            // Check filter
            if (plugin.config.console.combined.filter != null && plugin.config.console.combined.filter.matcher(command).matches()) return;

            // Add to log
            try {
                Files.createDirectories(plugin.config.console.combined.file.getParent());
                Files.write(
                        plugin.config.console.combined.file,
                        plugin.config.console.combined.format(event).getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (final Exception e) {
                AnnoyingPlugin.log(Level.WARNING, "&cFailed to write to combined console command log file!", e);
            }
        }

        // Splits
        for (final Split split : plugin.config.console.splits) {
            // Check filter
            if (split.filter != null && split.filter.matcher(command).matches()) continue;

            // Add to log
            try {
                final Path file = plugin.logsFolder.resolve(plugin.processFileNameVariables(split.fileName));
                Files.createDirectories(file.getParent());
                Files.write(
                        file,
                        split.format(event).getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (final Exception e) {
                AnnoyingPlugin.log(Level.WARNING, "&cFailed to write to console command log file!", e);
            }
        }
    }
}
