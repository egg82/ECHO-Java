package me.egg82.echo.storage;

import java.util.Collection;
import java.util.Set;
import me.egg82.echo.storage.models.BaseModel;
import me.egg82.echo.storage.models.DataModel;
import me.egg82.echo.storage.models.LearnModel;
import me.egg82.echo.storage.models.MessageModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
