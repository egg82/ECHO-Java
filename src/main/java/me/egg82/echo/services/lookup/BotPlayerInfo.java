package me.egg82.echo.services.lookup;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableList;
import flexjson.JSONDeserializer;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.egg82.echo.services.lookup.models.PlayerNameModel;
import me.egg82.echo.services.lookup.models.PlayerUUIDModel;
import me.egg82.echo.services.lookup.models.ProfileModel;
import me.egg82.echo.utils.WebUtil;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BotPlayerInfo implements PlayerInfo {
    private final UUID uuid;
    private final String name;
    private List<ProfileModel.ProfilePropertyModel> properties;

    private static final Cache<UUID, String> uuidCache = Caffeine.newBuilder().expireAfterWrite(1L, TimeUnit.HOURS).build();
    private static final Cache<String, UUID> nameCache = Caffeine.newBuilder().expireAfterWrite(1L, TimeUnit.HOURS).build();
    private static final Cache<UUID, List<ProfileModel.ProfilePropertyModel>> propertiesCache = Caffeine.newBuilder().expireAfterWrite(1L, TimeUnit.DAYS).build();

    private static final Object uuidCacheLock = new Object();
    private static final Object nameCacheLock = new Object();
    private static final Object propertiesCacheLock = new Object();

    private static final String NAME_URL = "https://api.mojang.com/user/profiles/%s/names";
    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    BotPlayerInfo(@NotNull UUID uuid) throws IOException {
        this.uuid = uuid;

        Optional<String> name = Optional.ofNullable(uuidCache.getIfPresent(uuid));
        if (!name.isPresent()) {
            synchronized (uuidCacheLock) {
                name = Optional.ofNullable(uuidCache.getIfPresent(uuid));
                if (!name.isPresent()) {
                    name = Optional.ofNullable(nameExpensive(uuid));
                    name.ifPresent(v -> uuidCache.put(uuid, v));
                }
            }
        }

        this.name = name.orElse(null);

        if (this.name != null) {
            Optional<List<ProfileModel.ProfilePropertyModel>> properties = Optional.ofNullable(propertiesCache.getIfPresent(uuid));
            if (!properties.isPresent()) {
                synchronized (propertiesCacheLock) {
                    properties = Optional.ofNullable(propertiesCache.getIfPresent(uuid));
                    if (!properties.isPresent()) {
                        properties = Optional.ofNullable(propertiesExpensive(uuid));
                        properties.ifPresent(v -> propertiesCache.put(uuid, v));
                    }
                }
            }
            this.properties = properties.orElse(null);
        }
    }

    BotPlayerInfo(@NotNull String name) throws IOException {
        this.name = name;

        Optional<UUID> uuid = Optional.ofNullable(nameCache.getIfPresent(name));
        if (!uuid.isPresent()) {
            synchronized (nameCacheLock) {
                uuid = Optional.ofNullable(nameCache.getIfPresent(name));
                if (!uuid.isPresent()) {
                    uuid = Optional.ofNullable(uuidExpensive(name));
                    uuid.ifPresent(v -> nameCache.put(name, v));
                }
            }
        }

        this.uuid = uuid.orElse(null);

        if (this.uuid != null) {
            Optional<List<ProfileModel.ProfilePropertyModel>> properties = Optional.ofNullable(propertiesCache.getIfPresent(this.uuid));
            if (!properties.isPresent()) {
                synchronized (propertiesCacheLock) {
                    properties = Optional.ofNullable(propertiesCache.getIfPresent(this.uuid));
                    if (!properties.isPresent()) {
                        properties = Optional.ofNullable(propertiesExpensive(this.uuid));
                        properties.ifPresent(v -> propertiesCache.put(this.uuid, v));
                    }
                }
            }
            this.properties = properties.orElse(null);
        }
    }

    public @NotNull UUID getUUID() { return uuid; }

    public @NotNull String getName() { return name; }

    public @NotNull ImmutableList<ProfileModel.ProfilePropertyModel> getProperties() { return ImmutableList.copyOf(properties); }

    private static @Nullable String nameExpensive(@NotNull UUID uuid) throws IOException {
        // Network lookup
        Request request = WebUtil.getDefaultRequestBuilder(new URL(String.format(NAME_URL, uuid.toString().replace("-", ""))))
                .header("Accept", "application/json")
                .build();

        try (Response response = WebUtil.getResponse(request)) {
            if (!response.isSuccessful()) {
                throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
            }

            if (response.code() == 204) {
                // No data exists
                return null;
            } else if (response.code() == 200) {
                JSONDeserializer<List<PlayerNameModel>> modelDeserializer = new JSONDeserializer<>();
                modelDeserializer.use("values", PlayerNameModel.class);
                List<PlayerNameModel> model = modelDeserializer.deserialize(response.body().string());

                String name = model.get(model.size() - 1).getName();
                synchronized (nameCacheLock) {
                    nameCache.put(name, uuid);
                }
                return name;
            }

            throw new IOException("Mojang API response code: " + response.code());
        }
    }

    private static @Nullable UUID uuidExpensive(@NotNull String name) throws IOException {
        // Network lookup
        Request request = WebUtil.getDefaultRequestBuilder(new URL(String.format(UUID_URL, WebUtil.urlEncode(name))))
                .header("Accept", "application/json")
                .build();

        try (Response response = WebUtil.getResponse(request)) {
            if (!response.isSuccessful()) {
                throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
            }

            if (response.code() == 204) {
                // No data exists
                return null;
            } else if (response.code() == 200) {
                JSONDeserializer<PlayerUUIDModel> modelDeserializer = new JSONDeserializer<>();
                PlayerUUIDModel model = modelDeserializer.deserialize(response.body().string(), PlayerUUIDModel.class);

                UUID uuid = UUID.fromString(model.getId().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
                synchronized (uuidCacheLock) {
                    uuidCache.put(uuid, name);
                }
                return uuid;
            }

            throw new IOException("Mojang API response code: " + response.code());
        }
    }

    private static @Nullable List<ProfileModel.ProfilePropertyModel> propertiesExpensive(@NotNull UUID uuid) throws IOException {
        // Network lookup
        Request request = WebUtil.getDefaultRequestBuilder(new URL(String.format(PROFILE_URL, uuid.toString().replace("-", ""))))
                .header("Accept", "application/json")
                .build();

        try (Response response = WebUtil.getResponse(request)) {
            if (!response.isSuccessful()) {
                throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
            }

            if (response.code() == 204) {
                // No data exists
                return null;
            } else if (response.code() == 200) {
                JSONDeserializer<ProfileModel> modelDeserializer = new JSONDeserializer<>();
                return modelDeserializer.deserialize(response.body().string(), ProfileModel.class).getProperties();
            }

            throw new IOException("Mojang API response code: " + response.code());
        }
    }
}
