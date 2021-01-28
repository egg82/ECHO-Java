package me.egg82.echo.messaging.packets;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class MessagePacket extends AbstractPacket {
    private String message;

    public byte getPacketId() { return 0x01; }

    public MessagePacket(@NotNull ByteBuf data) { read(data); }

    public MessagePacket() {
        this.message = "";
    }

    public void read(@NotNull ByteBuf buffer) {
        if (!checkVersion(buffer)) {
            return;
        }

        this.message = readString(buffer);

        checkReadPacket(buffer);
    }

    public void write(@NotNull ByteBuf buffer) {
        buffer.writeByte(VERSION);

        writeString(this.message, buffer);
    }

    public @NotNull String getMessage() { return message; }

    public void setMessage(@NotNull String message) { this.message = message; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessagePacket)) return false;
        MessagePacket that = (MessagePacket) o;
        return message.equals(that.message);
    }

    public int hashCode() { return Objects.hash(message); }

    public String toString() {
        return "MessagePacket{" +
                "message='" + message + '\'' +
                '}';
    }
}
