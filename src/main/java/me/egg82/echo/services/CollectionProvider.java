package me.egg82.echo.services;

import co.aikar.commands.JDACommandManager;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import me.egg82.echo.commands.AbstractCommand;
import me.egg82.echo.commands.ECHOCommand;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.reflect.PackageFilter;
import me.egg82.echo.storage.StorageService;
import me.egg82.echo.storage.models.LearnModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CollectionProvider {
    private static final Logger logger = LoggerFactory.getLogger(CollectionProvider.class);

    private CollectionProvider() { }

    private static final LoadingCache<Long, Boolean> canLearnCache = Caffeine.newBuilder()
            .expireAfterWrite(1L, TimeUnit.HOURS)
            .expireAfterAccess(30L, TimeUnit.MINUTES)
            .build(CollectionProvider::canLearnExpensive);

    public static @NotNull LoadingCache<Long, Boolean> getCanLearnCache() { return canLearnCache; }

    private static ImmutableList<Class<AbstractCommand>> commandClasses = null;
    private static final Object commandClassesLock = new Object();

    public static @NotNull List<Class<AbstractCommand>> getCommandClasses() {
        ImmutableList<Class<AbstractCommand>> retVal = commandClasses;
        if (retVal == null) {
            synchronized (commandClassesLock) {
                retVal = commandClasses;
                if (retVal == null) {
                    commandClasses = retVal = ImmutableList.copyOf(PackageFilter.getClasses(AbstractCommand.class, "me.egg82.echo.commands", false, false, false));
                }
            }
        }
        return retVal;
    }

    private static ImmutableList<AbstractCommand> commands = null;
    private static final Object commandsLock = new Object();

    public static @NotNull List<AbstractCommand> getCommands(@NotNull JDACommandManager commandManager) {
        ImmutableList<AbstractCommand> retVal = commands;
        if (retVal == null) {
            synchronized (commandsLock) {
                retVal = commands;
                if (retVal == null) {
                    List<AbstractCommand> internalCommands = new ArrayList<>();
                    List<Class<AbstractCommand>> commandClasses = getCommandClasses();
                    for (Class<AbstractCommand> command : commandClasses) {
                        if (!ECHOCommand.class.equals(command)) {
                            try {
                                internalCommands.add(command.newInstance());
                            } catch (InstantiationException | IllegalAccessException ex) {
                                logger.warn(ex.getMessage(), ex);
                            }
                        }
                    }
                    internalCommands.add(new ECHOCommand(commandManager.getJDA(), commandManager));
                    commands = retVal = ImmutableList.copyOf(internalCommands);
                }
            }
        }
        return retVal;
    }

    public static @Nullable AbstractCommand getCommand(@NotNull JDACommandManager commandManager, @NotNull String name, boolean caseSensitive) {
        for (AbstractCommand command : getCommands(commandManager)) {
            if (
                    (!caseSensitive && command.getName().equalsIgnoreCase(name))
                            || (caseSensitive && command.getName().equals(name))
            ) {
                return command;
            }
        }
        return null;
    }

    private static boolean canLearnExpensive(long id) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            throw new IllegalStateException("Could not get cached config.");
        }

        for (StorageService service : cachedConfig.getStorage()) {
            LearnModel model = service.getLearnModel(id);
            if (model != null) {
                return model.isLearning();
            }
        }
        return true;
    }
}
