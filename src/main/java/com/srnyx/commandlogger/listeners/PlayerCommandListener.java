package com.srnyx.commandlogger.listeners;

import com.srnyx.commandlogger.CommandLogger;
import com.srnyx.commandlogger.InfoForVariables;
import com.srnyx.commandlogger.config.ConfigLogger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingListener;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;


public class PlayerCommandListener extends AnnoyingListener {
    @NotNull private final CommandLogger plugin;

    public PlayerCommandListener(@NotNull CommandLogger plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public CommandLogger getAnnoyingPlugin() {
        return plugin;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(@NotNull PlayerCommandPreprocessEvent event) {
        if (!plugin.config.players.enabled) {
            unregister();
            return;
        }
        final String command = event.getMessage().substring(1);

        // Check all filter
        if (plugin.config.filters != null && plugin.config.filters.doesNotPass(command)) return;

        // Check players filter
        if (plugin.config.players.filters != null && plugin.config.players.filters.doesNotPass(command)) return;

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
                AnnoyingPlugin.log(Level.WARNING, "&cFailed to write to combined command log file for a player command!", e);
            }
        }

        // Player loggers
        final Player player = event.getPlayer();
        final String name = player.getName();
        for (final ConfigLogger.PlayerLogger split : plugin.config.players.loggers) {
            // Check filter
            if (split.filters != null && split.filters.doesNotPass(command)) continue;

            // Check permission
            if (!split.hasRequiredPermission(player)) continue;

            // Add to log
            try {
                final Path file = split.filePath(info);
                Files.createDirectories(file.getParent());
                Files.write(
                        file,
                        split.format(info).getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (final Exception e) {
                AnnoyingPlugin.log(Level.WARNING, "&cFailed to write to player command log file for " + name + "!", e);
            }
        }
    }
}
