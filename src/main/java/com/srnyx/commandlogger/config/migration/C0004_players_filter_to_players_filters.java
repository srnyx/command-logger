package com.srnyx.commandlogger.config.migration;

import eu.okaeri.configs.migrate.builtin.NamedMigration;

import static eu.okaeri.configs.migrate.ConfigMigrationDsl.move;


/**
 * There was a typo in the default config.yml in previous versions.
 * <br>Some users may have used this incorrect key, so this migration will move it to the correct key.
 */
public class C0004_players_filter_to_players_filters extends NamedMigration {
    public C0004_players_filter_to_players_filters() {
        super("migrates incorrect players.filter to players.filters", move("players.filter", "players.filters"));
    }
}
