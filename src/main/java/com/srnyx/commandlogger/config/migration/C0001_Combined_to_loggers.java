package com.srnyx.commandlogger.config.migration;

import eu.okaeri.configs.migrate.builtin.NamedMigration;
import xyz.srnyx.annoyingapi.libs.javautilities.MapGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static eu.okaeri.configs.migrate.ConfigMigrationDsl.*;


public class C0001_Combined_to_loggers extends NamedMigration {
    public C0001_Combined_to_loggers() {
        super("migrates combined loggers to advanced loggers",
                any(
                        when(
                                exists("combined"),
                                ((config, view) -> {
                                    // Get existing loggers
                                    List<Object> existing = view.getAsList("loggers", Object.class);
                                    if (existing == null) existing = new ArrayList<>();

                                    // enabled, file, format
                                    final boolean enabled = view.getOr("combined.enabled", Boolean.class, false);
                                    final String file = view.getOr("combined.file", String.class, "commands.log");
                                    final String format = view.getOr("combined.format", String.class, "[{date} {time}] [{player}] /{full_command}");

                                    // filter -> filters
                                    final String filter = view.get("combined.filter", String.class);
                                    final Map<String, String> filters = filter == null || filter.isEmpty() ? null : MapGenerator.LINKED_HASH_MAP.mapOf(
                                            "include", null,
                                            "exclude", filter);

                                    // Add new logger
                                    existing.add(MapGenerator.LINKED_HASH_MAP.mapOf(
                                            "enabled", enabled,
                                            "file_name", file,
                                            "filters", filters,
                                            "format", format));

                                    // Remove old combined logger
                                    view.set("loggers", existing);
                                    view.remove("combined");
                                    return true;
                                })),

                        when(
                                exists("players.combined"),
                                ((config, view) -> {
                                    // Get existing loggers
                                    List<Object> existing = view.getAsList("players.loggers", Object.class);
                                    if (existing == null) existing = new ArrayList<>();

                                    // enabled, file, format, required_permission
                                    final boolean enabled = view.getOr("players.combined.enabled", Boolean.class, false);
                                    final String file = view.getOr("players.combined.file", String.class, "players.log");
                                    final String format = view.getOr("players.combined.format", String.class, "[{date} {time}] [{player}] /{full_command}");

                                    // required_permission
                                    String requiredPermission = view.get("players.combined.required_permission", String.class);
                                    if (requiredPermission != null && requiredPermission.isEmpty()) requiredPermission = null;

                                    // filter -> filters
                                    final String filter = view.get("players.combined.filter", String.class);
                                    final Map<String, String> filters = filter == null || filter.isEmpty() ? null : MapGenerator.LINKED_HASH_MAP.mapOf(
                                            "include", null,
                                            "exclude", filter);

                                    // Add new logger
                                    existing.add(MapGenerator.LINKED_HASH_MAP.mapOf(
                                            "enabled", enabled,
                                            "file_name", file,
                                            "required_permission", requiredPermission,
                                            "filters", filters,
                                            "format", format));

                                    // Remove old combined logger
                                    view.set("players.loggers", existing);
                                    view.remove("players.combined");
                                    return true;
                                })),

                        when(
                                exists("console.combined"),
                                ((config, view) -> {
                                    // Get existing loggers
                                    List<Object> existing = view.getAsList("console.loggers", Object.class);
                                    if (existing == null) existing = new ArrayList<>();

                                    // enabled, file, format, filter
                                    final boolean enabled = view.getOr("console.combined.enabled", Boolean.class, false);
                                    final String file = view.getOr("console.combined.file", String.class, "console.log");
                                    final String format = view.getOr("console.combined.format", String.class, "[{date} {time}] /{full_command}");

                                    // filter -> filters
                                    final String filter = view.get("console.combined.filter", String.class);
                                    final Map<String, String> filters = filter == null || filter.isEmpty() ? null : MapGenerator.LINKED_HASH_MAP.mapOf(
                                            "include", null,
                                            "exclude", filter);

                                    // Add new logger
                                    existing.add(MapGenerator.LINKED_HASH_MAP.mapOf(
                                            "enabled", enabled,
                                            "file_name", file,
                                            "filters", filters,
                                            "format", format));

                                    // Remove old combined logger
                                    view.set("console.loggers", existing);
                                    view.remove("console.combined");
                                    return true;
                                }))));
    }
}
