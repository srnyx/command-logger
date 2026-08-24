package com.srnyx.commandlogger.messages;

import com.srnyx.commandlogger.CommandLogger;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Include;
import eu.okaeri.configs.annotation.IncludePosition;
import eu.okaeri.validator.annotation.NotNull;
import xyz.srnyx.annoyingapi.message.AnnoyingMessages;
import xyz.srnyx.annoyingapi.message.json.message.JsonChatMessage;


@Include(value = AnnoyingMessages.class, position = IncludePosition.BEFORE)
public class CLMessages extends AnnoyingMessages {
    public CLMessages(@org.jetbrains.annotations.NotNull CommandLogger annoyingPlugin) {
        super(annoyingPlugin);
    }

    @Comment
    @NotNull public JsonChatMessage reload = defaultMessage("%prefix%%p%Configuration reloaded successfully!@@%p%%command%@@%command%");
}
