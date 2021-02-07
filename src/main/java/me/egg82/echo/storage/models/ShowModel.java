package me.egg82.echo.storage.models;

import io.ebean.annotation.NotNull;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "echo_show")
public class ShowModel extends BaseModel {
    @NotNull
    private long tvdb;
    @NotNull
    private int season;
    @NotNull
    private int episode;

    public ShowModel() {
        super();
        this.tvdb = -1L;
        this.season = -1;
        this.episode = -1;
    }

    public ShowModel(@org.jetbrains.annotations.NotNull String dbName) {
        super(dbName);
        this.tvdb = -1L;
        this.season = -1;
        this.episode = -1;
    }

    public long getTvdb() { return tvdb; }

    public void setTvdb(long tvdb) { this.tvdb = tvdb; }

    public int getSeason() { return season; }

    public void setSeason(int season) { this.season = season; }

    public int getEpisode() { return episode; }

    public void setEpisode(int episode) { this.episode = episode; }

    public String toString() {
        return "ShowModel{" +
                "id=" + id +
                ", version=" + version +
                ", created=" + created +
                ", modified=" + modified +
                ", tvdb=" + tvdb +
                ", season=" + season +
                ", episode=" + episode +
                '}';
    }
}
