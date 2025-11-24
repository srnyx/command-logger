package com.srnyx.commandlogger.config;

import com.srnyx.commandlogger.CommandLogger;
import com.srnyx.commandlogger.InfoForVariables;

import me.clip.placeholderapi.PlaceholderAPI;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;


public class ConfigYml {
    @NotNull private final CommandLogger plugin;
    @NotNull private final AnnoyingResource config;
    @NotNull public final Path logsFolder;

    public final boolean enabled;
    @Nullable public final Filters filters;
    @NotNull public final VariableFormats variableFormats;
    @NotNull public final List<ConfigLogger> loggers = new ArrayList<>();
    @NotNull public final Players players;
    @NotNull public final Console console;

    public ConfigYml(@NotNull CommandLogger plugin) {
        this.plugin = plugin;
        config = new AnnoyingResource(plugin, "config.yml");
        logsFolder = plugin.getDataFolder().toPath().resolve("logs");

        enabled = config.getBoolean("enabled", true);
        filters = Filters.getFilters(config.getConfigurationSection("filters"));
        variableFormats = new VariableFormats();
        players = new Players();
        console = new Console();

        // loggers
        for (final Map<?, ?> map : config.getMapList("loggers")) {
            try {
                final Boolean loggerEnabled = (Boolean) map.get("enabled");
                if (loggerEnabled != null && loggerEnabled) loggers.add(new ConfigLogger(this, map));
            } catch (final ClassCastException | IllegalArgumentException e) {
                AnnoyingPlugin.log(Level.WARNING, "&cFailed to load a command logger from config.yml due to invalid options! Skipping it...", e);
            }
        }

        // LEGACY: combined
        if (config.contains("combined")) {
            final Combined combined = new Combined();
            if (combined.enabled) loggers.add(new ConfigLogger(this, combined.file, combined.format));
        }
    }

    public static class Filters {
        @Nullable public final Pattern include;
        @Nullable public final Pattern exclude;

        public Filters(@Nullable String include, @Nullable String exclude) {
            this.include = getFilter(include);
            this.exclude = getFilter(exclude);
        }

        public boolean doesNotPass(@NotNull String command) {
            if (exclude != null && exclude.matcher(command).matches()) return true;
            return include != null && !include.matcher(command).matches();
        }

        @Nullable
        public static Filters getFilters(@Nullable ConfigurationSection section) {
            return section != null ? new Filters(section.getString("include"), section.getString("exclude")) : null;
        }

        @Nullable
        public static Filters getFilters(@Nullable Map<?, ?> map) {
            try {
                return map != null ? new Filters((String) map.get("include"), (String) map.get("exclude")) : null;
            } catch (final ClassCastException e) {
                AnnoyingPlugin.log(Level.WARNING, "&cFailed to load a command filter from config.yml due to invalid options! Skipping these filters...", e);
                return null;
            }
        }

        @Nullable
        private static Pattern getFilter(@Nullable String filter) {
            return filter != null && !filter.trim().isEmpty() ? Pattern.compile(filter) : null;
        }
    }

    @NotNull
    protected String processVariables(@NotNull String string, @NotNull InfoForVariables info) {
        // PlaceholderAPI (run first so placeholders in raw command aren't replaced)
        if (plugin.papiInstalled) string = PlaceholderAPI.setPlaceholders(info.sender instanceof Player ? (Player) info.sender : null, string);

        // Get plugin placeholders
        String playerName = info.sender.getName();
        String uuid = "";
        String ip = "";
        if (info.sender instanceof Player) {
            final Player player = (Player) info.sender;
            final InetSocketAddress address = player.getAddress();
            uuid = player.getUniqueId().toString();
            ip = address != null ? address.getAddress().getHostAddress() : "";
        } else {
            playerName = "*" + playerName + "*";
        }
        // Get full command, base command, and arguments
        final String fullCommand = info.command.substring(1);
        final String[] split = fullCommand.split(" ", 2);
        final String baseCommand = split[0];
        final String arguments = split.length > 1 ? split[1] : "";

        // Replace plugin placeholders
        final Date now = new Date();
        return string
                .replace("{date}", variableFormats.date.formats.format(now))
                .replace("{time}", variableFormats.time.formats.format(now))
                .replace("{player}", playerName)
                .replace("{uuid}", uuid)
                .replace("{ip}", ip)
                .replace("{full_command}", fullCommand)
                .replace("{base_command}", baseCommand)
                .replace("{arguments}", arguments)
                .replace("{command}", info.command); // Legacy/old
    }

    @NotNull
    public String processFormatVariables(@NotNull String format, @NotNull InfoForVariables info) {
        return processVariables(format, info) + "\n";
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

    @Deprecated
    public class Combined {
        public final boolean enabled = config.getBoolean("combined.enabled", false);
        @NotNull public final String file = config.getString("combined.file", "commands.log");
        @NotNull public final String format = config.getString("combined.format", "[{date} {time}] [{player}] /{full_command}");

        @NotNull
        public String format(@NotNull InfoForVariables info) {
            return processFormatVariables(format, info);
        }
    }

    public class Players {
        public final boolean enabled = config.getBoolean("players.enabled", true);
        @Nullable public final Filters filters = Filters.getFilters(config.getConfigurationSection("players.filters"));
        @NotNull public final List<ConfigLogger.PlayerLogger> loggers = new ArrayList<>();

        public Players() {
            // loggers
            for (final Map<?, ?> map : config.getMapList("players.loggers")) {
                try {
                    final Boolean enabled = (Boolean) map.get("enabled");
                    if (enabled != null && enabled) loggers.add(new ConfigLogger.PlayerLogger(ConfigYml.this, map));
                } catch (final ClassCastException | IllegalArgumentException e) {
                    AnnoyingPlugin.log(Level.WARNING, "&cFailed to load a player command logger from config.yml due to invalid options! Skipping it...", e);
                }
            }

            // LEGACY: combined
            if (config.contains("players.combined")) {
                final Combined combined = new Combined();
                if (combined.enabled) loggers.add(new ConfigLogger.PlayerLogger(ConfigYml.this, combined.file, combined.format, combined.requiredPermission));
            }
            // LEGACY: splits
            for (final Map<?, ?> map : config.getMapList("players.splits")) {
                try {
                    final Boolean enabled = (Boolean) map.get("enabled");
                    if (enabled != null && enabled) loggers.add(new ConfigLogger.PlayerLogger(ConfigYml.this, map));
                } catch (final ClassCastException | IllegalArgumentException e) {
                    AnnoyingPlugin.log(Level.WARNING, "&cFailed to load a player command split from config.yml due to invalid options! Skipping it...", e);
                }
            }
        }

        @Deprecated
        private class Combined {
            public final boolean enabled = config.getBoolean("players.combined.enabled", false);
            @NotNull public final String file = config.getString("players.combined.file", "players.log");
            @Nullable public final String requiredPermission = config.getString("players.combined.required-permission");
            @NotNull public final String format = config.getString("players.combined.format", "[{date} {time}] [{player}] /{full_command}");
        }
    }

    public class Console {
        public final boolean enabled = config.getBoolean("console.enabled", true);
        @Nullable public final Filters filters = Filters.getFilters(config.getConfigurationSection("console.filters"));
        @NotNull public final List<ConfigLogger> loggers = new ArrayList<>();

        public Console() {
            // loggers
            for (final Map<?, ?> map : config.getMapList("console.loggers")) {
                try {
                    final Boolean enabled = (Boolean) map.get("enabled");
                    if (enabled != null && enabled) loggers.add(new ConfigLogger(ConfigYml.this, map));
                } catch (final ClassCastException | IllegalArgumentException e) {
                    AnnoyingPlugin.log(Level.WARNING, "&cFailed to load a console command logger from config.yml due to invalid options! Skipping it...", e);
                }
            }

            // LEGACY: combined
            if (config.contains("console.combined")) {
                final Combined combined = new Combined();
                if (combined.enabled) loggers.add(new ConfigLogger(ConfigYml.this, combined.file, combined.format));
            }
            // LEGACY: splits
            for (final Map<?, ?> map : config.getMapList("console.splits")) {
                try {
                    final Boolean enabled = (Boolean) map.get("enabled");
                    if (enabled != null && enabled) loggers.add(new ConfigLogger(ConfigYml.this, map));
                } catch (final ClassCastException | IllegalArgumentException e) {
                    AnnoyingPlugin.log(Level.WARNING, "&cFailed to load a console split from config.yml due to invalid options! Skipping it...", e);
                }
            }
        }

        @Deprecated
        private class Combined {
            public final boolean enabled = config.getBoolean("console.combined.enabled", false);
            @NotNull public final String file = config.getString("console.combined.file", "console.log");
            @NotNull public final String format = config.getString("console.combined.format", "[{date} {time}] /{full_command}");
        }
    }
}
