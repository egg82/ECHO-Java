package me.egg82.echo.lang;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum Message implements MessageKeyProvider {
    GENERAL__HEADER,
    GENERAL__ENABLED,
    GENERAL__DISABLED,
    GENERAL__LOAD,

    ERROR__INTERNAL,

    RELOAD__BEGIN,
    RELOAD__END;

    private final MessageKey key = MessageKey.of(name().toLowerCase().replace("__", "."));
    public @NonNull MessageKey getMessageKey() { return key; }
}
