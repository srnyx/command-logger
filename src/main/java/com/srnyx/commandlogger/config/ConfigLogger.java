package com.srnyx.commandlogger.config;

import com.srnyx.commandlogger.InfoForVariables;

import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Date;
import java.util.Map;


public class ConfigLogger {
    @NotNull public final ConfigYml config;
    @NotNull public final String fileName;
    @Nullable public final ConfigYml.Filters filters;
    @NotNull public final String format;

    public ConfigLogger(@NotNull ConfigYml config, @NotNull Map<?, ?> map) {
        this.config = config;
        final String fileName = (String) map.get("file-name");
        if (fileName == null || fileName.trim().isEmpty()) throw new IllegalArgumentException("file-name cannot be null or empty");
        this.fileName = fileName;
        filters = ConfigYml.Filters.getFilters((Map<?, ?>) map.get("filters"));
        final String format = (String) map.get("format");
        if (format == null || format.trim().isEmpty()) throw new IllegalArgumentException("format cannot be null or empty");
        this.format = format;
    }

    public ConfigLogger(@NotNull ConfigYml config, @NotNull String fileName, @NotNull String format) {
        this.config = config;
        this.fileName = fileName;
        this.filters = null;
        this.format = format;
    }

    @NotNull
    public Path processFileNameVariables(@NotNull String fileName, @NotNull InfoForVariables info) {
        // Temporarily replace slashes with private use character to prevent sanitization of them
        fileName = fileName.replace("/", "\uE000");

        // File-specific plugin placeholders
        final Date now = new Date();
        fileName = fileName
                .replace("{date}", config.variableFormats.date.fileNames.format(now))
                .replace("{time}", config.variableFormats.time.fileNames.format(now));

        // Other plugin placeholders
        fileName = config.processVariables(fileName, info);

        // Sanitize file name
        return config.logsFolder.resolve(fileName
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("[. ]+$", "")
                .replace("\uE000", "/")); // Restore slashes
    }

    @NotNull
    public Path filePath(@NotNull InfoForVariables info) {
        return processFileNameVariables(fileName, info);
    }

    @NotNull
    public String format(@NotNull InfoForVariables info) {
        return config.processFormatVariables(format, info);
    }

    public static class PlayerLogger extends ConfigLogger {
        @Nullable public final String requiredPermission;

        public PlayerLogger(@NotNull ConfigYml config, @NotNull Map<?, ?> map) {
            super(config, map);
            final String requiredPermission = (String) map.get("required-permission");
            this.requiredPermission = requiredPermission != null && requiredPermission.trim().isEmpty() ? null : requiredPermission;
        }

        public PlayerLogger(@NotNull ConfigYml config, @NotNull String fileName, @NotNull String format, @Nullable String requiredPermission) {
            super(config, fileName, format);
            this.requiredPermission = requiredPermission != null && requiredPermission.trim().isEmpty() ? null : requiredPermission;
        }

        public boolean hasRequiredPermission(@NotNull Player player) {
            return requiredPermission == null || player.hasPermission(requiredPermission);
        }
    }
}
