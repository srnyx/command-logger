package com.srnyx.commandlogger;

import com.srnyx.commandlogger.messages.CLMessagesProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.library.AnnoyingLibraryManager;


public class MockCommandLogger extends CommandLogger {
    public MockCommandLogger() {
        options.pluginOptions.applyMockTemplate();
    }

    @Override @Nullable
    protected AnnoyingLibraryManager createLibraryManager() {
        // Class loader can't be casted for the library manager to work
        return null;
    }

    @Override @NotNull
    public CLMessagesProvider getMessages() {
        // Registrables don't work in tests, so it would try to cast the default/anonymous provider to CLMessagesProvider
        return new CLMessagesProvider(this);
    }
}
