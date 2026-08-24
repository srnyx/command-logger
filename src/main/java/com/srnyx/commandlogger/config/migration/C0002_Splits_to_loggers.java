package com.srnyx.commandlogger.config.migration;

import eu.okaeri.configs.migrate.builtin.NamedMigration;
import xyz.srnyx.annoyingapi.libs.javautilities.MapGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static eu.okaeri.configs.migrate.ConfigMigrationDsl.*;


public class C0002_Splits_to_loggers extends NamedMigration {
    public C0002_Splits_to_loggers() {
        super("migrates splits to advanced loggers",
                any(
                        when(
                                exists("players.splits"),
                                ((config, view) -> {
                                    List<Map<String, Object>> existing = (List<Map<String, Object>>) view.getRaw("players.loggers");
                                    if (existing == null) existing = new ArrayList<>();

                                    final List<Map<String, Object>> splits = (List<Map<String, Object>>) view.getRaw("players.splits");
                                    for (final Map<String, Object> split : splits) {
                                        // required_permission (remove if empty)
                                        if (!(split.get("required_permission") instanceof String string) || string.isEmpty()) {
                                            split.remove("required_permission");
                                        }

                                        // filter -> filters
                                        final Object filter = split.remove("filter");
                                        if (filter instanceof String string && !string.isEmpty()) {
                                            split.put("filters", MapGenerator.LINKED_HASH_MAP.mapOf(
                                                    "include", null,
                                                    "exclude", filter));
                                        }

                                        existing.add(split);
                                    }

                                    view.set("players.loggers", existing);
                                    view.remove("players.splits");
                                    return true;
                                })),

                        when(
                                exists("console.splits"),
                                ((config, view) -> {
                                    List<Map<String, Object>> existing = (List<Map<String, Object>>) view.getRaw("console.loggers");
                                    if (existing == null) existing = new ArrayList<>();

                                    final List<Map<String, Object>> splits = (List<Map<String, Object>>) view.getRaw("console.splits");
                                    for (final Map<String, Object> split : splits) {
                                        // filter -> filters
                                        final Object filter = split.remove("filter");
                                        if (filter instanceof String string && !string.isEmpty()) {
                                            split.put("filters", MapGenerator.LINKED_HASH_MAP.mapOf(
                                                    "include", null,
                                                    "exclude", filter));
                                        }

                                        existing.add(split);
                                    }

                                    view.set("console.loggers", existing);
                                    view.remove("console.splits");
                                    return true;
                                }))));
    }
}
