package me.egg82.echo.utils;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import me.egg82.echo.config.ConfigUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ExceptionUtil {
    private ExceptionUtil() { }

    public static <T> T handleException(@NotNull Throwable ex, @NotNull Logger logger) {
        Throwable oldEx = null;
        if (ex instanceof CompletionException || ex instanceof ExecutionException) {
            oldEx = ex;
            ex = ex.getCause();
        }
        while (ex instanceof CompletionException || ex instanceof ExecutionException) {
            ex = ex.getCause();
        }

        if (ex instanceof CancellationException) {
            if (ConfigUtil.getDebugOrFalse()) {
                logger.warn(ex.getMessage(), oldEx != null ? oldEx : ex);
            } else {
                logger.warn(ex.getMessage());
            }
        } else {
            if (ConfigUtil.getDebugOrFalse()) {
                logger.error(ex.getMessage(), oldEx != null ? oldEx : ex);
            } else {
                logger.error(ex.getMessage());
            }
        }

        return null;
    }
}
