package me.egg82.echo.messaging;

import me.egg82.echo.messaging.packets.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface MessagingHandler {
    void handlePacket(@NotNull UUID messageId, @NotNull String fromService, @NotNull Packet packet);

    void cancel();
}
