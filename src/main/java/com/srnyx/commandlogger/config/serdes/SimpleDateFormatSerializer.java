package com.srnyx.commandlogger.config.serdes;

import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;


public class SimpleDateFormatSerializer extends BidirectionalTransformer<String, SimpleDateFormat> {
    @Override @NotNull
    public GenericsPair<String, SimpleDateFormat> getPair() {
        return genericsPair(String.class, SimpleDateFormat.class);
    }

    @Override @NotNull
    public SimpleDateFormat leftToRight(@NotNull String data, @NotNull SerdesContext serdesContext) {
        return new SimpleDateFormat(data);
    }

    @Override @NotNull
    public String rightToLeft(@NotNull SimpleDateFormat data, @NotNull SerdesContext serdesContext) {
        return data.toPattern();
    }
}
