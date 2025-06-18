package com.srnyx.commandlogger.config;

import com.srnyx.commandlogger.CommandLogger;
import com.srnyx.commandlogger.listeners.ConsoleCommandListener;
import com.srnyx.commandlogger.listeners.PlayerCommandListener;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.file.AnnoyingResource;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ConfigYml {
    @NotNull private final CommandLogger plugin;
    @NotNull private final AnnoyingResource config;

    public final boolean enabled;
    @NotNull public final VariableFormats variableFormats;
    @NotNull public final Combined combined;
    @NotNull public final Players players;
    @NotNull public final Console console;

    public ConfigYml(@NotNull CommandLogger plugin) {
        this.plugin = plugin;
        config = new AnnoyingResource(plugin, "config.yml");

        enabled = config.getBoolean("enabled", true);
        variableFormats = new VariableFormats();
        combined = new Combined();
        players = new Players();
        console = new Console();
    }

    public class VariableFormats {
        @NotNull public final Date date = new Date();
        @NotNull public final Time time = new Time();

        public class Date {
            @NotNull public final SimpleDateFormat fileNames = new SimpleDateFormat(config.getString("variable-formats.date.file-names", "yyyy-MM-dd"));
            @NotNull public final SimpleDateFormat formats = new SimpleDateFormat(config.getString("variable-formats.date.formats", "MM-dd-yyyy"));
        }

        public class Time {
            @NotNull public final SimpleDateFormat fileNames = new SimpleDateFormat(config.getString("variable-formats.time.file-names", "HH-mm-ss"));
            @NotNull public final SimpleDateFormat formats = new SimpleDateFormat(config.getString("variable-formats.time.formats", "HH:mm:ss"));
        }
    }

    public class Combined {
        public final boolean enabled = config.getBoolean("combined.enabled", true);
        @NotNull public final Path file = plugin.logsFolder.resolve(config.getString("combined.file", "commands.log"));
        @NotNull public final String format = config.getString("combined.format", "[{date} {time}] [{player}] {command}");

        @NotNull
        public String format(@NotNull ServerCommandEvent event) {
            return plugin.processFormatVariables(format, event);
        }

        @NotNull
        public String format(@NotNull PlayerCommandPreprocessEvent event) {
            return plugin.processFormatVariables(format, event);
        }
    }

    public class Players {
        public final boolean enabled = config.getBoolean("players.enabled", true);
        @NotNull public final Combined combined = new Combined();
        @NotNull public final List<Split.PlayerSplit> splits = new ArrayList<>();

        public Players() {
            for (final Map<?, ?> map : config.getMapList("players.splits")) {
                final Boolean enabled = (Boolean) map.get("enabled");
                if (enabled != null && enabled) splits.add(new Split.PlayerSplit(plugin, map, "players/{uuid}.log", "[{date} {time}] {command}"));
            }
        }

        public class Combined {
            public final boolean enabled = config.getBoolean("players.combined.enabled", true);
            @NotNull public final Path file = plugin.logsFolder.resolve(config.getString("players.combined.file", "players.log"));
            @Nullable public final String requiredPermission;
            @NotNull public final String format = config.getString("players.combined.format", "[{date} {time}] [{player}] {command}");

            public Combined() {
                final String requiredPermission = config.getString("players.combined.required-permission");
                this.requiredPermission = requiredPermission != null && requiredPermission.trim().isEmpty() ? null : requiredPermission;
            }

            public boolean hasRequiredPermission(@NotNull Player player) {
                return requiredPermission == null || player.hasPermission(requiredPermission);
            }

            @NotNull
            public String format(@NotNull PlayerCommandPreprocessEvent event) {
                return plugin.processFormatVariables(format, event);
            }
        }
    }

    public class Console {
        public final boolean enabled = config.getBoolean("console.enabled", true);
        @NotNull public final Combined combined = new Combined();
        @NotNull public final List<Split> splits = new ArrayList<>();

        public Console() {
            for (final Map<?, ?> map : config.getMapList("console.splits")) {
                final Boolean enabled = (Boolean) map.get("enabled");
                if (enabled != null && enabled) splits.add(new Split(plugin, map, "console/{date}.log", "[{time}] {command}"));
            }
        }

        public class Combined {
            public final boolean enabled = config.getBoolean("console.combined.enabled", true);
            @NotNull public final Path file = plugin.logsFolder.resolve(config.getString("console.combined.file", "console.log"));
            @NotNull public final String format = config.getString("console.combined.format", "[{date} {time}] {command}");

            @NotNull
            public String format(@NotNull ServerCommandEvent event) {
                return plugin.processFormatVariables(format, event);
            }
        }
    }
}
