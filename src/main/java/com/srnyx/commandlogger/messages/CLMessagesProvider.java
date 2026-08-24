package com.srnyx.commandlogger.messages;

import com.srnyx.commandlogger.CommandLogger;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigBuilder;
import xyz.srnyx.annoyingapi.message.MessagesProvider;


public class CLMessagesProvider extends MessagesProvider {
    @NotNull private final CommandLogger plugin;

    public CLMessagesProvider(@NotNull CommandLogger plugin) {
        this.plugin = plugin;

        defaults
                .prefix("&3&lCMD LOGGER &8&l| &b")
                .p("&b")
                .s("&3");
    }

    @Override @NotNull
    public CommandLogger getAnnoyingPlugin() {
        return plugin;
    }

    @Override
    public void mutateBuilder(@NotNull ConfigBuilder builder) {
        builder.config(new CLMessages(plugin));
    }

    @Override @NotNull
    public CLMessages get() {
        return (CLMessages) messages;
    }
}
