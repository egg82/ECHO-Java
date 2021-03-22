package me.egg82.echo.storage;

import me.egg82.echo.storage.models.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public interface StorageService {
    @NotNull String getName();

    void close();

    boolean isClosed();

    void storeModel(@NotNull BaseModel model);

    void storeModels(@NotNull Collection<? extends BaseModel> models);

    void deleteModel(@NotNull BaseModel model);

    /*
   Note: Can be an expensive operation
    */
    @NotNull ShowModel getOrCreateShowModel(long tvdbId, int season, int episode);

    @Nullable ShowModel getShowModel(long idOrTvdbId);

    @NotNull Set<ShowModel> getAllShows(int start, int max);

    /*
   Note: Can be an expensive operation
    */
    @NotNull WebModel getOrCreateWebModel(@NotNull String hash, @NotNull String service, @NotNull String path);

    @Nullable WebModel getWebModel(@NotNull String hash, @NotNull String service);

    @Nullable WebModel getWebModel(@NotNull String hash, @NotNull String service, long cacheTimeMillis);

    @Nullable WebModel getWebModel(long uploadId);

    @Nullable WebModel getWebModel(long uploadId, long cacheTimeMillis);

    @NotNull Set<WebModel> getAllWebs(long cacheTimeMillis);

    @NotNull Set<WebModel> getAllWebs(int start, int max);

    /*
   Note: Can be an expensive operation
    */
    @NotNull MessageModel getOrCreateMessageModel(@NotNull String message);

    @Nullable MessageModel getMessageModel(@NotNull String message);

    @Nullable MessageModel getMessageModel(long messageId);

    @NotNull Set<MessageModel> getAllMessages(int start, int max);

    /*
   Note: Can be an expensive operation
    */
    @NotNull LearnModel getOrCreateLearnModel(long user, boolean learn);

    @Nullable LearnModel getLearnModel(long userOrId);

    @NotNull Set<LearnModel> getAllLearning(int start, int max);

    /*
   Note: Can be an expensive operation
    */
    @NotNull DataModel getOrCreateDataModel(@NotNull String key, String value);

    @Nullable DataModel getDataModel(@NotNull String key);

    @Nullable DataModel getDataModel(long dataId);
}
