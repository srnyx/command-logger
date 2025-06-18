package com.srnyx.commandlogger.listeners;

import com.srnyx.commandlogger.CommandLogger;
import com.srnyx.commandlogger.config.Split;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingListener;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.net.InetSocketAddress;
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

        // Combined
        if (plugin.config.combined.enabled) {
            try {
                Files.createDirectories(plugin.config.combined.file.getParent());
                Files.write(
                        plugin.config.combined.file,
                        plugin.config.combined.format(event).getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (final Exception e) {
                AnnoyingPlugin.log(Level.WARNING, "&cFailed to write to combined command log file for a player command!", e);
            }
        }

        final Player player = event.getPlayer();

        // Players combined
        if (plugin.config.players.combined.enabled && plugin.config.players.combined.hasRequiredPermission(player)) {
            try {
                Files.createDirectories(plugin.config.players.combined.file.getParent());
                Files.write(
                        plugin.config.players.combined.file,
                        plugin.config.players.combined.format(event).getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (final Exception e) {
                AnnoyingPlugin.log(Level.WARNING, "&cFailed to write to combined player command log file!", e);
            }
        }

        // Splits
        if (plugin.config.players.splits.isEmpty()) return;
        final String name = player.getName();
        final String uuid = player.getUniqueId().toString();
        final InetSocketAddress address = player.getAddress();
        final String ip = address != null ? address.getAddress().getHostAddress() : "";
        for (final Split.PlayerSplit split : plugin.config.players.splits) if (split.hasRequiredPermission(player)) try {
            final Path file = plugin.logsFolder.resolve(plugin.processFileNameVariables(split.fileName)
                    .replace("{player}", name)
                    .replace("{uuid}", uuid)
                    .replace("{ip}", ip));
            Files.createDirectories(file.getParent());
            Files.write(
                    file,
                    split.format(event).getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (final Exception e) {
            AnnoyingPlugin.log(Level.WARNING, "&cFailed to write to player command log file for " + name + "!", e);
        }
    }
}
