package me.egg82.echo.utils;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.CommandManager;
import co.aikar.commands.MessageFormatter;
import co.aikar.commands.MessageType;
import co.aikar.locales.MessageKeyProvider;
import me.egg82.echo.logging.AnsiColor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class BotLogUtil {
    private BotLogUtil() { }

    public static final String HEADING = AnsiColor.BRIGHT_YELLOW + "[" + AnsiColor.BRIGHT_BLUE + "ECHO" + AnsiColor.BRIGHT_YELLOW + "] " + AnsiColor.RESET;

    public static void sendInfo(@NotNull Logger logger, @NotNull CommandManager manager, @NotNull MessageKeyProvider key, String... replacements) { sendMessage(logger, manager, MessageType.INFO, key, replacements); }

    public static void sendError(@NotNull Logger logger, @NotNull CommandManager manager, @NotNull MessageKeyProvider key, String... replacements) { sendMessage(logger, manager, MessageType.ERROR, key, replacements); }

    public static void sendMessage(@NotNull Logger logger, @NotNull CommandManager manager, @NotNull MessageType type, @NotNull MessageKeyProvider key, String... replacements) {
        logger.info(manager.formatMessage(null, type, key, replacements));
    }

    public static void sendInfo(@NotNull Logger logger, @NotNull CommandManager manager, @NotNull String message, String... replacements) { sendMessage(logger, manager, MessageType.INFO, message, replacements); }

    public static void sendError(@NotNull Logger logger, @NotNull CommandManager manager, @NotNull String message, String... replacements) { sendMessage(logger, manager, MessageType.ERROR, message, replacements); }

    public static void sendMessage(@NotNull Logger logger, @NotNull CommandManager manager, @NotNull MessageType type, @NotNull String message, String... replacements) {
        if (replacements.length > 0) {
            message = ACFUtil.replaceStrings(message, replacements);
        }

        message = manager.getCommandReplacements().replace(message);
        message = manager.getLocales().replaceI18NStrings(message);
        MessageFormatter formatter = manager.getFormat(type);
        if (formatter != null) {
            message = formatter.format(message);
        }

        logger.info(message);
    }
}
