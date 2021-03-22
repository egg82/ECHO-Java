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
    ERROR__USER_NOT_EXIST,
    ERROR__COMMAND_NOT_EXIST,

    IMPORT__BEGIN,
    IMPORT__MESSAGES,
    IMPORT__END,

    LEARN__BEGIN,
    LEARN__END,

    NO_LEARN__BEGIN,
    NO_LEARN__LEARNING,
    NO_LEARN__NOT_LEARNING,

    RELOAD__BEGIN,
    RELOAD__END;

    private final MessageKey key = MessageKey.of(name().toLowerCase().replace("__", "."));

    @Override
    public @NotNull MessageKey getMessageKey() { return key; }
}
