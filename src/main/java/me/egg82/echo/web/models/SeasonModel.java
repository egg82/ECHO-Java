package me.egg82.echo.web.models;

import flexjson.JSON;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SeasonModel implements Serializable {
    private int number = -1;
    private SeasonIDListModel ids = new SeasonIDListModel();
    private double rating = -1.0d;
    private long votes = -1L;
    @JSON(name = "episode_count")
    private int episodeCount = -1;
    @JSON(name = "aired_episodes")
    private int airedEpisodes = -1;
    private String title = "";
    private String overview = null;
    @JSON(name = "first_aired")
    private Instant firstAired = Instant.now();
    @JSON(name = "updated_at")
    private Instant updatedAt = Instant.now();
    private String network = "";

    public SeasonModel() { }

    public int getNumber() { return number; }

    public void setNumber(int number) { this.number = number; }

    public @NotNull SeasonIDListModel getIds() { return ids; }

    public void setIds(@NotNull SeasonIDListModel ids) { this.ids = ids; }

    public double getRating() { return rating; }

    public void setRating(double rating) { this.rating = rating; }

    public long getVotes() { return votes; }

    public void setVotes(long votes) { this.votes = votes; }

    @JSON(name = "episode_count")
    public int getEpisodeCount() { return episodeCount; }

    @JSON(name = "episode_count")
    public void setEpisodeCount(int episodeCount) { this.episodeCount = episodeCount; }

    @JSON(name = "aired_episodes")
    public int getAiredEpisodes() { return airedEpisodes; }

    @JSON(name = "aired_episodes")
    public void setAiredEpisodes(int airedEpisodes) { this.airedEpisodes = airedEpisodes; }

    public @NotNull String getTitle() { return title; }

    public void setTitle(@NotNull String title) { this.title = title; }

    public @Nullable String getOverview() { return overview; }

    public void setOverview(String overview) { this.overview = overview; }

    @JSON(name = "first_aired")
    public @NotNull Instant getFirstAired() { return firstAired; }

    @JSON(name = "first_aired")
    public void setFirstAired(@NotNull Instant firstAired) { this.firstAired = firstAired; }

    @JSON(name = "updated_at")
    public @NotNull Instant getUpdatedAt() { return updatedAt; }

    @JSON(name = "updated_at")
    public void setUpdatedAt(@NotNull Instant updatedAt) { this.updatedAt = updatedAt; }

    public @NotNull String getNetwork() { return network; }

    public void setNetwork(@NotNull String network) { this.network = network; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeasonModel)) return false;
        SeasonModel that = (SeasonModel) o;
        return number == that.number && rating == that.rating && votes == that.votes && episodeCount == that.episodeCount && airedEpisodes == that.airedEpisodes && ids.equals(that.ids) && title.equals(that.title) && Objects.equals(overview, that.overview) && firstAired.equals(that.firstAired) && updatedAt.equals(that.updatedAt) && network.equals(that.network);
    }

    public int hashCode() { return Objects.hash(number, ids, rating, votes, episodeCount, airedEpisodes, title, overview, firstAired, updatedAt, network); }

    public String toString() {
        return "SeasonModel{" +
                "number=" + number +
                ", ids=" + ids +
                ", rating=" + rating +
                ", votes=" + votes +
                ", episodeCount=" + episodeCount +
                ", airedEpisodes=" + airedEpisodes +
                ", title='" + title + '\'' +
                ", overview='" + overview + '\'' +
                ", firstAired=" + firstAired +
                ", updatedAt=" + updatedAt +
                ", network='" + network + '\'' +
                '}';
    }

    public static final class SeasonIDListModel implements Serializable {
        private long trakt = -1L;
        private long tvdb = -1L;
        private long tmdb = -1L;

        public SeasonIDListModel() { }

        public long getTrakt() { return trakt; }

        public void setTrakt(long trakt) { this.trakt = trakt; }

        public long getTvdb() { return tvdb; }

        public void setTvdb(long tvdb) { this.tvdb = tvdb; }

        public long getTmdb() { return tmdb; }

        public void setTmdb(long tmdb) { this.tmdb = tmdb; }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SeasonIDListModel)) return false;
            SeasonIDListModel that = (SeasonIDListModel) o;
            return trakt == that.trakt && tvdb == that.tvdb && tmdb == that.tmdb;
        }

        public int hashCode() { return Objects.hash(trakt, tvdb, tmdb); }

        public String toString() {
            return "SeasonIDListModel{" +
                    "trakt=" + trakt +
                    ", tvdb=" + tvdb +
                    ", tmdb=" + tmdb +
                    '}';
        }
    }
}
