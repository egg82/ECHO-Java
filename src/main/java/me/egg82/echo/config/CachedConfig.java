package me.egg82.echo.config;

import com.google.common.collect.ImmutableList;
import io.paradaux.ai.MarkovMegaHal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import me.egg82.echo.messaging.MessagingService;
import me.egg82.echo.storage.StorageService;
import org.jetbrains.annotations.NotNull;

public class CachedConfig {
    private CachedConfig() { }

    private MarkovMegaHal megaHal = new MarkovMegaHal();
    public @NotNull MarkovMegaHal getMegaHal() { return megaHal; }

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

    private String googleKey = "";
    public @NotNull String getGoogleKey() { return googleKey; }

    public static @NotNull CachedConfig.Builder builder() { return new CachedConfig.Builder(); }

    public static class Builder {
        private final CachedConfig values = new CachedConfig();

        private Builder() { }

        public @NotNull CachedConfig.Builder debug(boolean value) {
            values.debug = value;
            return this;
        }

        public @NotNull CachedConfig.Builder language(@NotNull Locale value) {
            values.language = value;
            return this;
        }

        public @NotNull CachedConfig.Builder megaHal(@NotNull MarkovMegaHal value) {
            values.megaHal = value;
            return this;
        }

        public @NotNull CachedConfig.Builder storage(@NotNull List<StorageService> value) {
            values.storage = ImmutableList.copyOf(value);
            return this;
        }

        public @NotNull CachedConfig.Builder messaging(@NotNull List<MessagingService> value) {
            values.messaging = ImmutableList.copyOf(value);
            return this;
        }

        public @NotNull CachedConfig.Builder serverId(@NotNull UUID value) {
            values.serverId = value;
            values.serverIdString = value.toString();
            return this;
        }

        public @NotNull CachedConfig.Builder googleKey(@NotNull String value) {
            values.googleKey = value;
            return this;
        }

        public CachedConfig build() { return values; }
    }
}
