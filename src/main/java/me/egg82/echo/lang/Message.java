package me.egg82.echo.lang;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;
import org.jetbrains.annotations.NotNull;

public enum Message implements MessageKeyProvider {
    GENERAL__HEADER,
    GENERAL__ENABLED,
    GENERAL__DISABLED,
    GENERAL__LOAD,

    ERROR__INTERNAL,

    IMPORT__BEGIN,
    IMPORT__MESSAGES,
    IMPORT__END,

    LEARN__BEGIN,
    LEARN__END,

    RELOAD__BEGIN,
    RELOAD__END;

    private final MessageKey key = MessageKey.of(name().toLowerCase().replace("__", "."));
    public @NotNull MessageKey getMessageKey() { return key; }
}
