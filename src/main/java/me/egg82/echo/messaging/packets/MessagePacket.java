package me.egg82.echo.messaging.packets;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MessagePacket extends AbstractPacket {
    private String message;

    @Override
    public byte getPacketId() { return 0x01; }

    public MessagePacket(@NotNull ByteBuf data) { read(data); }

    public MessagePacket() {
        this.message = "";
    }

    @Override
    public void read(@NotNull ByteBuf buffer) {
        if (!checkVersion(buffer)) {
            return;
        }

        this.message = readString(buffer);

        checkReadPacket(buffer);
    }

    @Override
    public void write(@NotNull ByteBuf buffer) {
        buffer.writeByte(VERSION);

        writeString(this.message, buffer);
    }

    public @NotNull String getMessage() { return message; }

    public void setMessage(@NotNull String message) { this.message = message; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessagePacket)) return false;
        MessagePacket that = (MessagePacket) o;
        return message.equals(that.message);
    }

    @Override
    public int hashCode() { return Objects.hash(message); }

    @Override
    public String toString() {
        return "MessagePacket{" +
                "message='" + message + '\'' +
                '}';
    }
}
