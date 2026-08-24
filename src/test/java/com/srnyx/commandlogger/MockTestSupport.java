package com.srnyx.commandlogger;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;


public class MockTestSupport {
    protected static MockCommandLogger PLUGIN;

    @BeforeAll
    static void setUpMockBukkit() {
        MockBukkit.mock();
        PLUGIN = MockBukkit.load(MockCommandLogger.class);
    }

    @AfterAll
    static void tearDownMockBukkit() {
        MockBukkit.unmock();
    }
}
