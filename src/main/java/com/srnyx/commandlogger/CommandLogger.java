package com.srnyx.commandlogger;

import com.srnyx.commandlogger.config.ConfigYml;
import com.srnyx.commandlogger.listeners.ConsoleCommandListener;
import com.srnyx.commandlogger.listeners.PlayerCommandListener;

import me.clip.placeholderapi.PlaceholderAPI;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.PluginPlatform;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Date;


public class CommandLogger extends AnnoyingPlugin {
    @NotNull public final Path logsFolder = getDataFolder().toPath().resolve("logs");
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
        playerCommandListener.setRegistered(config.players.enabled && (config.players.combined.enabled || !config.players.splits.isEmpty()));
        consoleCommandListener.setRegistered(config.console.enabled && (config.console.combined.enabled || !config.console.splits.isEmpty()));
    }

    @NotNull
    public String processFileNameVariables(@NotNull String string) {
        final Date now = new Date();
        return string
                .replace("{date}", config.variableFormats.date.fileNames.format(now))
                .replace("{time}", config.variableFormats.time.fileNames.format(now));
    }

    @NotNull
    public String processFormatVariables(@NotNull String string, @NotNull CommandSender sender, @NotNull String command) {
        // PlaceholderAPI (run first so placeholders in raw command aren't replaced)
        if (papiInstalled) string = PlaceholderAPI.setPlaceholders(sender instanceof Player ? (Player) sender : null, string);

        // Get plugin placeholders
        String playerName = sender.getName();
        String uuid = "";
        String ip = "";
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            final InetSocketAddress address = player.getAddress();
            uuid = player.getUniqueId().toString();
            ip = address != null ? address.getAddress().getHostAddress() : "";
        } else {
            playerName = "*" + playerName + "*";
        }
        // Get full command, base command, and arguments
        final String fullCommand = command.substring(1);
        final String[] split = fullCommand.split(" ", 2);
        final String baseCommand = split[0];
        final String arguments = split.length > 1 ? split[1] : "";

        // Replace plugin placeholders
        final Date now = new Date();
        return string
                .replace("{date}", config.variableFormats.date.formats.format(now))
                .replace("{time}", config.variableFormats.time.formats.format(now))
                .replace("{player}", playerName)
                .replace("{uuid}", uuid)
                .replace("{ip}", ip)
                .replace("{full_command}", fullCommand)
                .replace("{base_command}", baseCommand)
                .replace("{arguments}", arguments)
                .replace("{command}", command) // Legacy/old
                + "\n";
    }

    @NotNull
    public String processFormatVariables(@NotNull String format, @NotNull ServerCommandEvent event) {
        return processFormatVariables(format, event.getSender(), "/" + event.getCommand());
    }

    @NotNull
    public String processFormatVariables(@NotNull String format, @NotNull PlayerCommandPreprocessEvent event) {
        return processFormatVariables(format, event.getPlayer(), event.getMessage());
    }
}
