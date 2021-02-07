package me.egg82.echo.tasks;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import me.egg82.echo.utils.TimeUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TaskScheduler.class);

    private static final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() / 2), new ThreadFactoryBuilder().setNameFormat("ECHO-%d").build());
    private static final IntSet aliveIds = new IntArraySet();

    private static final AtomicInteger currentTaskId = new AtomicInteger(0);

    private TaskScheduler() { }

    public static int createTask(@NotNull Runnable task, @NotNull TimeUtil.Time initialDelay) {
        int retVal = currentTaskId.getAndIncrement();
        aliveIds.add(retVal);

        threadPool.schedule(() -> {
            if (!aliveIds.contains(retVal)) {
                return;
            }

            try {
                task.run();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }, initialDelay.getTime(), initialDelay.getUnit());

        return retVal;
    }

    public static int createTask(@NotNull IntConsumer task, @NotNull TimeUtil.Time initialDelay) {
        int retVal = currentTaskId.getAndIncrement();
        aliveIds.add(retVal);

        threadPool.schedule(() -> {
            if (!aliveIds.contains(retVal)) {
                return;
            }

            try {
                task.accept(retVal);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }, initialDelay.getTime(), initialDelay.getUnit());

        return retVal;
    }

    public static int createRepeatingTask(@NotNull Runnable task, @NotNull TimeUtil.Time initialDelay, @NotNull TimeUtil.Time repeatedDelay) {
        int retVal = currentTaskId.getAndIncrement();
        aliveIds.add(retVal);

        threadPool.schedule(() -> {
            if (!aliveIds.contains(retVal)) {
                return;
            }

            try {
                task.run();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }

            if (aliveIds.contains(retVal)) {
                repeatTask(retVal, task, repeatedDelay);
            }
        }, initialDelay.getTime(), initialDelay.getUnit());

        return retVal;
    }

    public static int createRepeatingTask(@NotNull IntConsumer task, @NotNull TimeUtil.Time initialDelay, @NotNull TimeUtil.Time repeatedDelay) {
        int retVal = currentTaskId.getAndIncrement();
        aliveIds.add(retVal);

        threadPool.schedule(() -> {
            if (!aliveIds.contains(retVal)) {
                return;
            }

            try {
                task.accept(retVal);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }

            if (aliveIds.contains(retVal)) {
                repeatTask(retVal, task, repeatedDelay);
            }
        }, initialDelay.getTime(), initialDelay.getUnit());

        return retVal;
    }

    public static void cancelTask(int id) {
        aliveIds.remove(id);
    }

    private static void repeatTask(int id, @NotNull Runnable task, @NotNull TimeUtil.Time time) {
        threadPool.schedule(() -> {
            if (!aliveIds.contains(id)) {
                return;
            }

            try {
                task.run();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }

            if (aliveIds.contains(id)) {
                repeatTask(id, task, time);
            }
        }, time.getTime(), time.getUnit());
    }

    private static void repeatTask(int id, @NotNull IntConsumer task, @NotNull TimeUtil.Time time) {
        threadPool.schedule(() -> {
            if (!aliveIds.contains(id)) {
                return;
            }

            try {
                task.accept(id);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }

            if (aliveIds.contains(id)) {
                repeatTask(id, task, time);
            }
        }, time.getTime(), time.getUnit());
    }
}
