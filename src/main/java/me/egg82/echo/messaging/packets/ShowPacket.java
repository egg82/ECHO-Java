package me.egg82.echo.messaging.packets;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ShowPacket extends AbstractPacket {
    private long tvdb;
    private int season;
    private int episode;
    private String overview;

    @Override
    public byte getPacketId() { return 0x04; }

    public ShowPacket(@NotNull ByteBuf data) { read(data); }

    public ShowPacket() {
        this.tvdb = -1L;
        this.season = -1;
        this.episode = -1;
        this.overview = "";
    }

    @Override
    public void read(@NotNull ByteBuf buffer) {
        if (!checkVersion(buffer)) {
            return;
        }

        this.tvdb = buffer.readLong();
        this.season = readVarInt(buffer);
        this.episode = readVarInt(buffer);
        this.overview = readString(buffer);

        checkReadPacket(buffer);
    }

    @Override
    public void write(@NotNull ByteBuf buffer) {
        buffer.writeByte(VERSION);

        buffer.writeLong(this.tvdb);
        writeVarInt(this.season, buffer);
        writeVarInt(this.episode, buffer);
        writeString(this.overview, buffer);
    }

    public long getTvdb() { return tvdb; }

    public void setTvdb(long tvdb) { this.tvdb = tvdb; }

    public int getSeason() { return season; }

    public void setSeason(int season) { this.season = season; }

    public int getEpisode() { return episode; }

    public void setEpisode(int episode) { this.episode = episode; }

    public @NotNull String getOverview() { return overview; }

    public void setOverview(@NotNull String overview) { this.overview = overview; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShowPacket)) return false;
        ShowPacket that = (ShowPacket) o;
        return tvdb == that.tvdb && season == that.season && episode == that.episode && overview.equals(that.overview);
    }

    @Override
    public int hashCode() { return Objects.hash(tvdb, season, episode, overview); }

    @Override
    public String toString() {
        return "ShowPacket{" +
                "tvdb=" + tvdb +
                ", season=" + season +
                ", episode=" + episode +
                ", overview='" + overview + '\'' +
                '}';
    }
}
