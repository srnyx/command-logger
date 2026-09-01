package com.srnyx.commandlogger.config;

import com.srnyx.commandlogger.CommandLogger;
import com.srnyx.commandlogger.InfoForVariables;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.annotation.Include;
import eu.okaeri.configs.annotation.IncludePosition;
import eu.okaeri.validator.annotation.NotNull;
import eu.okaeri.validator.annotation.Nullable;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import xyz.srnyx.annoyingapi.file.okaeri.AnnoyingConfig;
import xyz.srnyx.annoyingapi.file.okaeri.RootConfig;
import xyz.srnyx.annoyingapi.file.okaeri.SubConfig;
import xyz.srnyx.annoyingapi.stats.Stat;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;


@Header("Available placeholders for file names and formats:")
@Header("  {date} - Current date as variable_formats.date")
@Header("  {time} - Current time as variable_formats.time")
@Header("  {player} - The player's name (*CONSOLE* for console commands)")
@Header("  {uuid} - Player's UUID (empty for console commands)")
@Header("  {ip} - Player's IP address (empty for console commands or if not available)")
@Header("  {full_command} - The command executed excluding the '/'")
@Header("  {base_command} - The command executed excluding the '/' and any arguments")
@Header("  {arguments} - The arguments provided to the command, empty if none")
public class ConfigYml extends RootConfig {
    @Comment
    @Comment
    @Comment("Toggle all logging features (basically the entire plugin)")
    @Stat
    public boolean enabled = true;

    @Comment
    @Comment("These filters apply to ALL command loggers (combined, players, and console) and are considered first")
    @Comment("All filters use regex (see https://regex101.com). Set to null for no filter.")
    @Comment("Filters can be applied to any logger! Exclude filters override include filters!")
    @Comment(" ")
    @Comment("--- include ---")
    @Comment("If a full command (excluding the /) matches this regex, it WILL be logged")
    @Comment("Example: \"op.*\" would log all /op commands even if another filter excludes them")
    @Comment(" ")
    @Comment("--- exclude ---")
    @Comment("If a full command (excluding the /) matches this regex, it will NOT be logged")
    @Comment("Example: \"help.*\" would exclude all /help commands from being logged")
    @Nullable public Filters filters = new Filters();

    @Comment
    @Comment("Configurable formats for some variables, such as date and time")
    @NotNull public VariableFormats variable_formats = new VariableFormats(this);

    @Comment
    @Comment("Combined player and console loggers (fields: enabled, file_name, filter, format)")
    @Comment("Several examples are provided below:")
    @Comment("  - File for ALL commands")
    @Comment("  - Separate file for each day")
    @Comment("  - File for only /op commands")
    @Stat(sizeOnly = true)
    @NotNull public List<ConfigLogger> loggers = List.of(
            new ConfigLogger(this,
                    true, "all.log",
                    null,
                    "[{date} {time}] [{player}] /{full_command}"),
            new ConfigLogger(this,
                    false, "all/{date}.log",
                    null,
                    "[{time}] [{player}] /{full_command}"),
            new ConfigLogger(this,
                    false, "{base_command}.log",
                    new Filters("op.*", null),
                    "[{date} {time}] [{player}] /{full_command}"));

    @Comment
    @Comment("Commands executed by players")
    @NotNull public Players players = new Players(this);

    @Comment
    @Comment("Commands executed by the console")
    @NotNull public Console console = new Console(this);


    @org.jetbrains.annotations.NotNull public transient final CommandLogger plugin;

    public ConfigYml(@org.jetbrains.annotations.NotNull CommandLogger plugin) {
        this.plugin = plugin;
    }

    public static class Filters extends AnnoyingConfig {
        @Nullable public Pattern include = null;

        @Nullable public Pattern exclude = null;

        public boolean doesNotPass(@org.jetbrains.annotations.NotNull String command) {
            if (exclude != null && !exclude.pattern().isEmpty() && exclude.matcher(command).matches()) return true;
            return include != null && !include.pattern().isEmpty() && !include.matcher(command).matches();
        }


        public Filters(@org.jetbrains.annotations.Nullable String include, @org.jetbrains.annotations.Nullable String exclude) {
            if (include != null) this.include = Pattern.compile(include);
            if (exclude != null) this.exclude = Pattern.compile(exclude);
        }

        public Filters() {
            this(null, null);
        }
    }

    @org.jetbrains.annotations.NotNull
    protected String processVariables(@org.jetbrains.annotations.NotNull String string, @org.jetbrains.annotations.NotNull InfoForVariables info) {
        // PlaceholderAPI (run first so placeholders in raw command aren't replaced)
        if (plugin.papiInstalled) string = PlaceholderAPI.setPlaceholders(info.sender instanceof Player player ? player : null, string);

        // Get plugin placeholders
        String playerName = info.sender.getName();
        String uuid = "";
        String ip = "";
        if (info.sender instanceof Player player) {
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
                .replace("{date}", variable_formats.date.formats.format(now))
                .replace("{time}", variable_formats.time.formats.format(now))
                .replace("{player}", playerName)
                .replace("{uuid}", uuid)
                .replace("{ip}", ip)
                .replace("{full_command}", fullCommand)
                .replace("{base_command}", baseCommand)
                .replace("{arguments}", arguments)
                .replace("{command}", info.command); // Legacy/old
    }

    @org.jetbrains.annotations.NotNull
    public String processFormatVariables(@org.jetbrains.annotations.NotNull String format, @org.jetbrains.annotations.NotNull InfoForVariables info) {
        return processVariables(format, info) + "\n";
    }

    public static class VariableFormats extends SubConfig<ConfigYml, ConfigYml> {
        public VariableFormats(@org.jetbrains.annotations.NotNull ConfigYml defaultsParent) {
            super(defaultsParent);
        }

        @Comment("https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html")
        @NotNull public Date date = new Date(this);

        @Comment("https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html")
        @NotNull public Time time = new Time(this);

        public static class Date extends SubConfig<ConfigYml, VariableFormats> {
            public Date(@org.jetbrains.annotations.NotNull VariableFormats defaultsParent) {
                super(defaultsParent);
            }

            @Stat
            @NotNull public SimpleDateFormat file_names = new SimpleDateFormat("yyyy-MM-dd");

            @Stat
            @NotNull public SimpleDateFormat formats = new SimpleDateFormat("MM-dd-yyyy");
        }

        //TODO common class with Date
        public static class Time extends SubConfig<ConfigYml, VariableFormats> {
            public Time(@org.jetbrains.annotations.NotNull VariableFormats defaultsParent) {
                super(defaultsParent);
            }

            @Stat
            @NotNull public SimpleDateFormat file_names = new SimpleDateFormat("HH-mm-ss");

            @Stat
            @NotNull public SimpleDateFormat formats = new SimpleDateFormat("HH:mm:ss");
        }
    }

    public static class ConfigLogger extends SubConfig<ConfigYml, ConfigYml> {
        public boolean enabled;

        @NotNull public String file_name;

        @Nullable public Filters filters;

        @NotNull public String format;

        public ConfigLogger(@org.jetbrains.annotations.NotNull ConfigYml defaultsParent, boolean enabled, @org.jetbrains.annotations.NotNull String fileName, @org.jetbrains.annotations.Nullable Filters filters, @org.jetbrains.annotations.NotNull String format) {
            super(defaultsParent);
            this.enabled = enabled;
            this.file_name = fileName;
            this.filters = filters;
            this.format = format;
        }

        @org.jetbrains.annotations.NotNull
        public Path processFileNameVariables(@org.jetbrains.annotations.NotNull String fileName, @org.jetbrains.annotations.NotNull InfoForVariables info) {
            final ConfigYml root = getRoot();

            // Temporarily replace slashes with private use character to prevent sanitization of them
            fileName = fileName.replace("/", "\uE000");

            // File-specific plugin placeholders
            final Date now = new Date();
            fileName = fileName
                    .replace("{date}", root.variable_formats.date.file_names.format(now))
                    .replace("{time}", root.variable_formats.time.file_names.format(now));

            // Other plugin placeholders
            fileName = root.processVariables(fileName, info);

            // Sanitize file name
            return root.plugin.logsFolder.resolve(fileName
                    .replaceAll("[\\\\/:*?\"<>|]", "_")
                    .replaceAll("[. ]+$", "")
                    .replace("\uE000", "/")); // Restore slashes
        }

        @org.jetbrains.annotations.NotNull
        public Path filePath(@org.jetbrains.annotations.NotNull InfoForVariables info) {
            return processFileNameVariables(file_name, info);
        }

        @org.jetbrains.annotations.NotNull
        public String format(@org.jetbrains.annotations.NotNull InfoForVariables info) {
            return getRoot().processFormatVariables(format, info);
        }
    }

    public static class Players extends SubConfig<ConfigYml, ConfigYml> {
        public Players(@org.jetbrains.annotations.NotNull ConfigYml defaultsParent) {
            super(defaultsParent);
        }

        @Comment("Toggle all player command loggers")
        @Stat
        public boolean enabled = true;

        @Comment
        @Comment("These filters apply to all player-ran command loggers")
        @Comment("These are considered after the global filters but before individual logger filters")
        @Nullable public Filters filters = new Filters();

        @Comment
        @Comment("Loggers for player commands (fields: enabled, file_name, required_permission, filters, format)")
        @Comment("Several examples are provided below:")
        @Comment("  - File for ALL player commands")
        @Comment("  - Separate file for each player UUID")
        @Comment("  - Separate file for each player UUID for each day excluding /login")
        @Comment("  - File for all players with group.mod permission")
        @Stat(sizeOnly = true)
        @NotNull public List<PlayerLogger> loggers = List.of(
                new PlayerLogger(getRoot(),
                        true,
                        "players.log",
                        null,
                        "[{date} {time}] [{player}] /{full_command}",
                        null),
                new PlayerLogger(getRoot(),
                        true,
                        "players/{uuid}.log",
                        null,
                        "[{date} {time}] /{full_command}",
                        null),
                new PlayerLogger(getRoot(),
                        false,
                        "players/{uuid}/{date}.log",
                        new Filters(null, "login.*"),
                        "[{time}] /{full_command}",
                        null),
                new PlayerLogger(getRoot(),
                        false,
                        "players/mods.log",
                        null,
                        "[{date} {time}] [{player}] /{full_command}",
                        "group.mod"));

        @Include(value = ConfigLogger.class, position = IncludePosition.BEFORE)
        public static class PlayerLogger extends ConfigLogger {
            @Nullable public String required_permission;

            public PlayerLogger(@org.jetbrains.annotations.NotNull ConfigYml defaultsParent, boolean enabled, @org.jetbrains.annotations.NotNull String fileName, @org.jetbrains.annotations.Nullable Filters filters, @org.jetbrains.annotations.NotNull String format, @org.jetbrains.annotations.Nullable String requiredPermission) {
                super(defaultsParent, enabled, fileName, filters, format);
                this.required_permission = requiredPermission;
            }

            public boolean hasRequiredPermission(@org.jetbrains.annotations.NotNull Player player) {
                return required_permission == null || player.hasPermission(required_permission);
            }
        }
    }

    public static class Console extends SubConfig<ConfigYml, ConfigYml> {
        public Console(@org.jetbrains.annotations.NotNull ConfigYml defaultsParent) {
            super(defaultsParent);
        }

        @Comment("Toggle all console command loggers")
        @Stat
        public boolean enabled = true;

        @Comment
        @Comment("These filters apply to all console command loggers")
        @Comment("These are considered after the global filters but before individual logger filters")
        @Nullable public Filters filters = new Filters();

        @Comment
        @Comment("Loggers for console commands (fields: enabled, file_name, filters, format)")
        @Comment("Several examples are provided below:")
        @Comment("  - File for ALL console commands")
        @Comment("  - Separate file for each day")
        @Comment("  - File excluding /save-all")
        @Stat(sizeOnly = true)
        @NotNull public List<ConfigLogger> loggers = List.of(
                new ConfigLogger(getRoot(),
                        true,
                        "console.log",
                        null,
                        "[{date} {time}] /{full_command}"),
                new ConfigLogger(getRoot(),
                        true,
                        "console/{date}.log",
                        null,
                        "[{time}] /{full_command}"),
                new ConfigLogger(getRoot(),
                        false,
                        "console-no-saveall.log",
                        new Filters(null, "save-all.*"),
                        "[{date} {time}] /{full_command}"));
    }
}
