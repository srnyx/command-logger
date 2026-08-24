package com.srnyx.commandlogger.config.migration;

import com.srnyx.commandlogger.MockTestSupport;
import com.srnyx.commandlogger.config.ConfigYml;
import com.srnyx.commandlogger.config.serdes.SimpleDateFormatSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class ConfigMigrationTest extends MockTestSupport {
    @TempDir Path tempDir;

    @Test
    void legacyConfigMigratesWithoutError() {
        assertDoesNotThrow(() -> loadConfig("legacy-config.yml"));
    }

    @Test
    void legacyConfigMigratesCorrectValues() {
        final ConfigYml config = loadConfig("legacy-config.yml");

        assertTrue(config.enabled);
        // Legacy root "filter" was empty, and C0003 maps empty/blank legacy filters to a null
        // "filters" object rather than an empty-but-present one
        assertNull(config.filters);

        // Root loggers: only the combined logger
        assertEquals(1, config.loggers.size());
        final ConfigYml.ConfigLogger rootLogger = config.loggers.get(0);
        assertTrue(rootLogger.enabled);
        assertEquals("all.log", rootLogger.file_name);
        assertEquals("[{date} {time}] [{player}] /{full_command}", rootLogger.format);
        assertNull(rootLogger.filters);

        // Players loggers: combined + 4 splits, in order
        final List<ConfigYml.Players.PlayerLogger> playerLoggers = config.players.loggers;
        assertEquals(5, playerLoggers.size());

        final ConfigYml.Players.PlayerLogger combined = playerLoggers.get(0);
        assertTrue(combined.enabled);
        assertEquals("players.log", combined.file_name);
        assertNull(combined.required_permission);
        assertNull(combined.filters);

        final ConfigYml.Players.PlayerLogger split1 = playerLoggers.get(1);
        assertTrue(split1.enabled);
        assertEquals("players/{uuid}.log", split1.file_name);
        assertNull(split1.required_permission);
        assertNull(split1.filters);

        final ConfigYml.Players.PlayerLogger split2 = playerLoggers.get(2);
        assertFalse(split2.enabled);
        assertEquals("players/{uuid}/{date}.log", split2.file_name);
        assertNotNull(split2.filters);
        assertNotNull(split2.filters.exclude);
        assertEquals("login.*", split2.filters.exclude.pattern());

        final ConfigYml.Players.PlayerLogger split3 = playerLoggers.get(3);
        assertFalse(split3.enabled);
        assertEquals("players/builders.log", split3.file_name);
        assertEquals("group.builder", split3.required_permission);

        final ConfigYml.Players.PlayerLogger split4 = playerLoggers.get(4);
        assertFalse(split4.enabled);
        assertEquals("players/mods.log", split4.file_name);
        assertEquals("group.mod", split4.required_permission);

        // Console loggers: combined + 1 split
        final List<ConfigYml.ConfigLogger> consoleLoggers = config.console.loggers;
        assertEquals(2, consoleLoggers.size());

        final ConfigYml.ConfigLogger consoleCombined = consoleLoggers.get(0);
        assertTrue(consoleCombined.enabled);
        assertEquals("console.log", consoleCombined.file_name);
        assertNull(consoleCombined.filters);

        final ConfigYml.ConfigLogger consoleSplit = consoleLoggers.get(1);
        assertTrue(consoleSplit.enabled);
        assertEquals("console/{date}.log", consoleSplit.file_name);
        assertNull(consoleSplit.filters);
    }

    @Test
    void typoPlayersFilterMigratesToFilters() {
        final ConfigYml config = loadConfig("typo-players-filter.yml");

        assertNotNull(config.players.filters);
        assertNotNull(config.players.filters.exclude);
        assertEquals("test", config.players.filters.exclude.pattern());
    }

    @Test
    void defaultsConfigMigratesWithoutErrorAndIsUnchanged() {
        final ConfigYml config = loadConfig(null);

        assertTrue(config.enabled);
        assertEquals(3, config.loggers.size());
        assertEquals(4, config.players.loggers.size());
        assertEquals(3, config.console.loggers.size());
    }

    @NotNull
    private ConfigYml loadConfig(@Nullable String resourceName) {
        final Path configFile = tempDir.resolve("config.yml");
        if (resourceName != null) {
            try (InputStream in = getClass().getResourceAsStream("/migration/" + resourceName)) {
                assertNotNull(in, "Missing test resource: " + resourceName);
                Files.copy(in, configFile);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new ConfigBuilder(PLUGIN, configFile.toFile())
                .config(new ConfigYml(PLUGIN))
                .configure(configure -> configure.serdes(new SimpleDateFormatSerializer()))
                .internalStateMigrations(
                        new C0001_Combined_to_loggers(),
                        new C0002_Splits_to_loggers(),
                        new C0003_String_filters_to_objects(),
                        new C0004_players_filter_to_players_filters())
                .build();
    }
}
