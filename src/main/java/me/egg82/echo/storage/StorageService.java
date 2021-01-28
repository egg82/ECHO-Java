package me.egg82.echo.storage;

import java.util.Collection;
import me.egg82.echo.storage.models.BaseModel;
import me.egg82.echo.storage.models.DataModel;
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
    @NotNull DataModel getOrCreateDataModel(@NotNull String key, String value);
    @Nullable DataModel getDataModel(@NotNull String key);
    @Nullable DataModel getDataModel(long dataId);
}
