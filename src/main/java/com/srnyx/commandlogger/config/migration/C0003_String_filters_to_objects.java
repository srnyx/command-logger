package com.srnyx.commandlogger.config.migration;

import eu.okaeri.configs.migrate.builtin.NamedMigration;
import eu.okaeri.configs.migrate.view.ConfigView;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.libs.javautilities.MapGenerator;

import java.util.Map;

import static eu.okaeri.configs.migrate.ConfigMigrationDsl.*;


public class C0003_String_filters_to_objects extends NamedMigration {
    public C0003_String_filters_to_objects() {
        super("migrates players and console filter string to filters include + exclude",
                any(
                        when(
                                all(
                                        exists("filter"),
                                        match("filter", o -> o instanceof String)),
                                ((config, view) -> migrate(view, "filter"))),
                        when(
                                all(
                                        exists("players.filter"),
                                        match("players.filter", o -> o instanceof String)),
                                ((config, view) -> migrate(view, "players.filter"))),
                        when(
                                all(
                                        exists("console.filter"),
                                        match("console.filter", o -> o instanceof String)),
                                ((config, view) -> migrate(view, "console.filter")))));
    }

    private static boolean migrate(@NotNull ConfigView view, @NotNull String oldKey) {
        final String filter = view.get(oldKey, String.class);
        final Map<String, String> filters = filter == null || filter.isEmpty() ? null : MapGenerator.LINKED_HASH_MAP.mapOf(
                "include", null,
                "exclude", filter);
        view.set(oldKey + "s", filters);
        view.remove(oldKey);
        return true;
    }
}
