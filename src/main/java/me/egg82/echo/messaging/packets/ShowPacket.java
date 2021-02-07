package me.egg82.echo.messaging.packets;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class ShowPacket extends AbstractPacket {
    private long tvdb;
    private int season;
    private int episode;

    public byte getPacketId() { return 0x04; }

    public ShowPacket(@NotNull ByteBuf data) { read(data); }

    public ShowPacket() {
        this.tvdb = -1L;
        this.season = -1;
        this.episode = -1;
    }

    public void read(@NotNull ByteBuf buffer) {
        if (!checkVersion(buffer)) {
            return;
        }

        this.tvdb = buffer.readLong();
        this.season = readVarInt(buffer);
        this.episode = readVarInt(buffer);

        checkReadPacket(buffer);
    }

    public void write(@NotNull ByteBuf buffer) {
        buffer.writeByte(VERSION);

        buffer.writeLong(this.tvdb);
        writeVarInt(this.season, buffer);
        writeVarInt(this.episode, buffer);
    }

    public long getTvdb() { return tvdb; }

    public void setTvdb(long tvdb) { this.tvdb = tvdb; }

    public int getSeason() { return season; }

    public void setSeason(int season) { this.season = season; }

    public int getEpisode() { return episode; }

    public void setEpisode(int episode) { this.episode = episode; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShowPacket)) return false;
        ShowPacket that = (ShowPacket) o;
        return tvdb == that.tvdb && season == that.season && episode == that.episode;
    }

    public int hashCode() { return Objects.hash(tvdb, season, episode); }

    public String toString() {
        return "ShowPacket{" +
                "tvdb=" + tvdb +
                ", season=" + season +
                ", episode=" + episode +
                '}';
    }
}
