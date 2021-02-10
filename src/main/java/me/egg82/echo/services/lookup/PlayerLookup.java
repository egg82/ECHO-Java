package me.egg82.echo.services.lookup;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import me.egg82.echo.utils.ExceptionUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerLookup {
    private static final Logger logger = LoggerFactory.getLogger(PlayerLookup.class);

    private PlayerLookup() { }

    public static @NotNull CompletableFuture<PlayerInfo> get(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new BotPlayerInfo(uuid);
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        }).exceptionally(ex -> ExceptionUtil.handleException(ex, logger)).thenApply(i -> i);
    }

    public static @NotNull CompletableFuture<PlayerInfo> get(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new BotPlayerInfo(name);
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        }).exceptionally(ex -> ExceptionUtil.handleException(ex, logger)).thenApply(i -> i);
    }
}
