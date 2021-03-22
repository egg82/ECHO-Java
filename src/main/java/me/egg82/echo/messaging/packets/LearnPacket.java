package me.egg82.echo.messaging.packets;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LearnPacket extends AbstractPacket {
    private long user;
    private boolean learning;

    @Override
    public byte getPacketId() { return 0x03; }

    public LearnPacket(@NotNull ByteBuf data) { read(data); }

    public LearnPacket() {
        this.user = -1L;
        this.learning = false;
    }

    @Override
    public void read(@NotNull ByteBuf buffer) {
        if (!checkVersion(buffer)) {
            return;
        }

        this.user = buffer.readLong();
        this.learning = buffer.readBoolean();

        checkReadPacket(buffer);
    }

    @Override
    public void write(@NotNull ByteBuf buffer) {
        buffer.writeByte(VERSION);

        buffer.writeLong(this.user);
        buffer.writeBoolean(this.learning);
    }

    public long getUser() { return user; }

    public void setUser(long user) { this.user = user; }

    public boolean isLearning() { return learning; }

    public void setLearning(boolean learn) { this.learning = learn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearnPacket)) {
            return false;
        }
        LearnPacket that = (LearnPacket) o;
        return user == that.user && learning == that.learning;
    }

    @Override
    public int hashCode() { return Objects.hash(user, learning); }

    @Override
    public String toString() {
        return "LearnPacket{" +
                "user=" + user +
                ", learning=" + learning +
                '}';
    }
}
