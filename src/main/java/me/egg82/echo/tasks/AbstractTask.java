package me.egg82.echo.tasks;

import it.unimi.dsi.fastutil.ints.IntList;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.core.NullablePair;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.IntConsumer;

public abstract class AbstractTask implements IntConsumer {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final JDA jda;
    protected final IntList tasks;

    protected AbstractTask(@NotNull JDA jda, @NotNull IntList tasks) {
        this.jda = jda;
        this.tasks = tasks;
    }

    protected final @Nullable CachedConfig getCachedConfig() {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
        }
        return cachedConfig;
    }

    protected final boolean canCompleteContinue(Object val, Throwable ex) {
        if (ex != null) {
            if (ConfigUtil.getDebugOrFalse()) {
                logger.error(ex.getMessage(), ex);
            } else {
                logger.error(ex.getMessage());
            }
            return false;
        }

        if (val == null) {
            logger.warn("val is null.");
            return false;
        }

        if (val instanceof NullablePair && (((NullablePair<?, ?>) val).getT1() == null || ((NullablePair<?, ?>) val).getT2() == null)) {
            if (((NullablePair<?, ?>) val).getT1() == null) {
                logger.warn("Pair T1 is null.");
            }
            if (((NullablePair<?, ?>) val).getT2() == null) {
                logger.warn("Pair T2 is null.");
            }
            return false;
        }

        return true;
    }
}
