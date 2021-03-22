package me.egg82.echo.messaging.packets;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MessageUpdatePacket extends AbstractPacket {
    private String oldMessage;
    private String newMessage;

    @Override
    public byte getPacketId() { return 0x02; }

    public MessageUpdatePacket(@NotNull ByteBuf data) { read(data); }

    public MessageUpdatePacket() {
        this.oldMessage = "";
        this.newMessage = "";
    }

    @Override
    public void read(@NotNull ByteBuf buffer) {
        if (!checkVersion(buffer)) {
            return;
        }

        this.oldMessage = readString(buffer);
        this.newMessage = readString(buffer);

        checkReadPacket(buffer);
    }

    @Override
    public void write(@NotNull ByteBuf buffer) {
        buffer.writeByte(VERSION);

        writeString(this.oldMessage, buffer);
        writeString(this.newMessage, buffer);
    }

    public @NotNull String getOldMessage() { return oldMessage; }

    public void setOldMessage(@NotNull String oldMessage) { this.oldMessage = oldMessage; }

    public @NotNull String getNewMessage() { return newMessage; }

    public void setNewMessage(@NotNull String newMessage) { this.newMessage = newMessage; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageUpdatePacket)) return false;
        MessageUpdatePacket that = (MessageUpdatePacket) o;
        return oldMessage.equals(that.oldMessage) && newMessage.equals(that.newMessage);
    }

    @Override
    public int hashCode() { return Objects.hash(oldMessage, newMessage); }

    @Override
    public String toString() {
        return "MessageUpdatePacket{" +
                "oldMessage='" + oldMessage + '\'' +
                ", newMessage='" + newMessage + '\'' +
                '}';
    }
}
