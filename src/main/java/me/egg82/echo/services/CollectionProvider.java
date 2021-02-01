package me.egg82.echo.services;

import co.aikar.commands.BaseCommand;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.reflect.PackageFilter;
import me.egg82.echo.storage.StorageService;
import me.egg82.echo.storage.models.LearnModel;
import org.jetbrains.annotations.NotNull;

public class CollectionProvider {
    private CollectionProvider() { }

    private static final LoadingCache<Long, Boolean> canLearnCache = Caffeine.newBuilder().expireAfterWrite(1L, TimeUnit.HOURS).expireAfterAccess(30L, TimeUnit.MINUTES).build(CollectionProvider::canLearnExpensive);
    public static @NotNull LoadingCache<Long, Boolean> getCanLearnCache() { return canLearnCache; }

    private static ImmutableList<Class<BaseCommand>> commandClasses = ImmutableList.of();
    private static final Object commandClassesLock = new Object();
    public static @NotNull List<Class<BaseCommand>> getCommandClasses() {
        ImmutableList<Class<BaseCommand>> retVal = commandClasses;
        if (retVal == null) {
            synchronized (commandClassesLock) {
                retVal = commandClasses;
                if (retVal == null) {
                    commandClasses = retVal = ImmutableList.copyOf(PackageFilter.getClasses(BaseCommand.class, "me.egg82.echo.commands", false, false, false));
                }
            }
        }
        return retVal;
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
