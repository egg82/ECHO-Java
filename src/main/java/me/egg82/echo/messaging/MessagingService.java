package me.egg82.echo.messaging;

import me.egg82.echo.messaging.packets.Packet;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public interface MessagingService {
    String getName();

    void close();
    boolean isClosed();

    void sendPacket(@NotNull UUID messageId, @NotNull Packet packet) throws IOException, TimeoutException;
}
