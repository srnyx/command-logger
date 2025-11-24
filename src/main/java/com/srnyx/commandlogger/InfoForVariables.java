package com.srnyx.commandlogger;

import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import org.jetbrains.annotations.NotNull;


public class InfoForVariables {
    @NotNull public final CommandSender sender;
    @NotNull public final String command;

    public InfoForVariables(@NotNull ServerCommandEvent event) {
        this.sender = event.getSender();
        this.command = "/" + event.getCommand();
    }

    public InfoForVariables(@NotNull PlayerCommandPreprocessEvent event) {
        this.sender = event.getPlayer();
        this.command = event.getMessage();
    }
}
