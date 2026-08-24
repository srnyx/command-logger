package com.srnyx.commandlogger.commands;

import com.srnyx.commandlogger.CommandLogger;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.command.AnnoyingSender;


public class ReloadCmd extends com.srnyx.commandlogger.commands.generated.CommandloggerreloadCmdGen {
    public ReloadCmd(@NotNull CommandLogger plugin) {
        super(plugin);
    }

    @Override
    public void onCommand(@NotNull AnnoyingSender sender) {
        plugin.reloadPlugin();
        plugin.getMessages().get().reload.newMessage().send(sender);
    }
}
