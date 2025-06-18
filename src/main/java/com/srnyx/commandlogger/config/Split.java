package com.srnyx.commandlogger.config;

import com.srnyx.commandlogger.CommandLogger;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public class Split {
    @NotNull public final CommandLogger plugin;
    @NotNull public final String fileName;
    @NotNull public final String format;

    public Split(@NotNull CommandLogger plugin, @NotNull Map<?, ?> map, @NotNull String defaultFileName, @NotNull String defaultFormat) {
        this.plugin = plugin;
        final String fileName = (String) map.get("fileName");
        this.fileName = fileName != null ? fileName : defaultFileName;
        final String format = (String) map.get("format");
        this.format = format != null && !format.trim().isEmpty() ? format : defaultFormat;
    }

    @NotNull
    public String format(@NotNull ServerCommandEvent event) {
        return plugin.processFormatVariables(format, event);
    }

    public static class PlayerSplit extends Split {
        @Nullable public final String requiredPermission;

        public PlayerSplit(@NotNull CommandLogger plugin, @NotNull Map<?, ?> map, @NotNull String defaultFileName, @NotNull String defaultFormat) {
            super(plugin, map, defaultFileName, defaultFormat);
            final String requiredPermission = (String) map.get("required-permission");
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
