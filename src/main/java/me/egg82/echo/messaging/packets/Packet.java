package me.egg82.echo.messaging.packets;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public interface Packet extends Serializable {
    byte VERSION = 0x01;

    byte getPacketId();

    void read(@NotNull ByteBuf buffer);

    void write(@NotNull ByteBuf buffer);
}
