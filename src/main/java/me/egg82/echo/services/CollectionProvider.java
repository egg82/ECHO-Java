package me.egg82.echo.services;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.util.concurrent.TimeUnit;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.storage.StorageService;
import me.egg82.echo.storage.models.LearnModel;
import org.jetbrains.annotations.NotNull;

public class CollectionProvider {
    private CollectionProvider() { }

    private static final LoadingCache<Long, Boolean> canLearnCache = Caffeine.newBuilder().expireAfterWrite(1L, TimeUnit.HOURS).expireAfterAccess(30L, TimeUnit.MINUTES).build(CollectionProvider::canLearnExpensive);
    public static @NotNull LoadingCache<Long, Boolean> getCanLearnCache() { return canLearnCache; }

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
