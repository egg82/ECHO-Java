package me.egg82.echo.messaging.packets;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class LearnPacket extends AbstractPacket {
    private long user;
    private boolean learning;

    public byte getPacketId() { return 0x03; }

    public LearnPacket(@NotNull ByteBuf data) { read(data); }

    public LearnPacket() {
        this.user = -1L;
        this.learning = false;
    }

    public void read(@NotNull ByteBuf buffer) {
        if (!checkVersion(buffer)) {
            return;
        }

        this.user = buffer.readLong();
        this.learning = buffer.readBoolean();

        checkReadPacket(buffer);
    }

    public void write(@NotNull ByteBuf buffer) {
        buffer.writeByte(VERSION);

        buffer.writeLong(this.user);
        buffer.writeBoolean(this.learning);
    }

    public long getUser() { return user; }

    public void setUser(long user) { this.user = user; }

    public boolean isLearning() { return learning; }

    public void setLearning(boolean learn) { this.learning = learn; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LearnPacket)) return false;
        LearnPacket that = (LearnPacket) o;
        return user == that.user && learning == that.learning;
    }

    public int hashCode() { return Objects.hash(user, learning); }

    public String toString() {
        return "LearnPacket{" +
                "user=" + user +
                ", learning=" + learning +
                '}';
    }
}
