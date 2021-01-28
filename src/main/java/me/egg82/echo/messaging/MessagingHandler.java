package me.egg82.echo.messaging;

import java.util.UUID;
import me.egg82.echo.messaging.packets.Packet;
import org.jetbrains.annotations.NotNull;

public interface MessagingHandler {
    void handlePacket(@NotNull UUID messageId, @NotNull String fromService, @NotNull Packet packet);

    void cancel();
}
