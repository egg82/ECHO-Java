package me.egg82.echo.lang;

import co.aikar.commands.CommandManager;
import co.aikar.commands.JDAMessageFormatter;
import co.aikar.locales.MessageKeyProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BotMessageFormatter extends JDAMessageFormatter {
    private final String header;

    public BotMessageFormatter(@NonNull CommandManager manager, @NonNull MessageKeyProvider header) { this(manager.getLocales().getMessage(null, header)); }

    public BotMessageFormatter(@NonNull String header) {
        super();
        this.header = header;
    }

    public @NonNull String format(@NonNull String message) {
        message = header + message;
        return super.format(message);
    }
}
