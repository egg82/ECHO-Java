package me.egg82.echo.services.lookup;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.jetbrains.annotations.NotNull;

public class PlayerLookup {
    private PlayerLookup() { }

    public static @NotNull CompletableFuture<PlayerInfo> get(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new BotPlayerInfo(uuid);
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }

    public static @NotNull CompletableFuture<PlayerInfo> get(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new BotPlayerInfo(name);
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }
}
