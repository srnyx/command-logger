package com.srnyx.commandlogger.commands;

import com.srnyx.commandlogger.CommandLogger;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.command.AnnoyingCommand;
import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.message.AnnoyingMessage;


public class ReloadCmd extends AnnoyingCommand {
    @NotNull private final CommandLogger plugin;

    public ReloadCmd(@NotNull CommandLogger plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public CommandLogger getAnnoyingPlugin() {
        return plugin;
    }

    @Override @NotNull
    public String getName() {
        return "commandloggerreload";
    }

    @Override @NotNull
    public String getPermission() {
        return "commandlogger.reload";
    }

    @Override
    public void onCommand(@NotNull AnnoyingSender sender) {
        plugin.reloadPlugin();
        new AnnoyingMessage(plugin, "reload").send(sender);
    }
}
