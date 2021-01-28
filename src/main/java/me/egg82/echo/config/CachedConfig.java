package me.egg82.echo.config;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import me.egg82.echo.messaging.MessagingService;
import me.egg82.echo.storage.StorageService;
import org.jetbrains.annotations.NotNull;

public class CachedConfig {
    private CachedConfig() { }

    private ImmutableList<StorageService> storage = ImmutableList.of();
    public @NotNull ImmutableList<StorageService> getStorage() { return storage; }

    private ImmutableList<MessagingService> messaging = ImmutableList.of();
    public @NotNull ImmutableList<MessagingService> getMessaging() { return messaging; }

    private boolean debug = false;
    public boolean getDebug() { return debug; }

    private Locale language = Locale.ENGLISH;
    public @NotNull Locale getLanguage() { return language; }

    private UUID serverId = UUID.randomUUID();
    public @NotNull UUID getServerId() { return serverId; }

    private String serverIdString = serverId.toString();
    public @NotNull String getServerIdString() { return serverIdString; }

    public static @NotNull CachedConfig.Builder builder() { return new CachedConfig.Builder(); }

    public static class Builder {
        private final CachedConfig values = new CachedConfig();

        private Builder() { }

        public CachedConfig.Builder debug(boolean value) {
            values.debug = value;
            return this;
        }

        public CachedConfig.Builder language(@NotNull Locale value) {
            values.language = value;
            return this;
        }

        public CachedConfig.Builder storage(@NotNull List<StorageService> value) {
            values.storage = ImmutableList.copyOf(value);
            return this;
        }

        public CachedConfig.Builder messaging(@NotNull List<MessagingService> value) {
            values.messaging = ImmutableList.copyOf(value);
            return this;
        }

        public CachedConfig.Builder serverId(@NotNull UUID value) {
            values.serverId = value;
            values.serverIdString = value.toString();
            return this;
        }

        public CachedConfig build() { return values; }
    }
}
