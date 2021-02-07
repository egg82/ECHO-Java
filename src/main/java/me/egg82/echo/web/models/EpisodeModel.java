package me.egg82.echo.web.models;

import flexjson.JSON;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class EpisodeModel implements Serializable {
    private int season = -1;
    private int number = -1;
    private String title = "";
    private EpisodeIDListModel ids = new EpisodeIDListModel();
    @JSON(name = "number_abs")
    private long numAbs = -1L;
    private String overview = "";
    @JSON(name = "first_aired")
    private Instant firstAired = Instant.now();
    @JSON(name = "updated_at")
    private Instant updatedAt = Instant.now();
    private double rating = -1.0d;
    private long votes = -1L;
    @JSON(name = "comment_count")
    private long commentCount = -1L;
    @JSON(name = "available_translations")
    private List<String> translations = new ArrayList<>();
    private int runtime = -1;

    public EpisodeModel() { }

    public int getSeason() { return season; }

    public void setSeason(int season) { this.season = season; }

    public int getNumber() { return number; }

    public void setNumber(int number) { this.number = number; }

    public @NotNull String getTitle() { return title; }

    public void setTitle(@NotNull String title) { this.title = title; }

    public @NotNull EpisodeIDListModel getIds() { return ids; }

    public void setIds(@NotNull EpisodeIDListModel ids) { this.ids = ids; }

    @JSON(name = "number_abs")
    public long getNumAbs() { return numAbs; }

    @JSON(name = "number_abs")
    public void setNumAbs(long numAbs) { this.numAbs = numAbs; }

    public @NotNull String getOverview() { return overview; }

    public void setOverview(@NotNull String overview) { this.overview = overview; }

    @JSON(name = "first_aired")
    public @NotNull Instant getFirstAired() { return firstAired; }

    @JSON(name = "first_aired")
    public void setFirstAired(@NotNull Instant firstAired) { this.firstAired = firstAired; }

    @JSON(name = "updated_at")
    public @NotNull Instant getUpdatedAt() { return updatedAt; }

    @JSON(name = "updated_at")
    public void setUpdatedAt(@NotNull Instant updatedAt) { this.updatedAt = updatedAt; }

    public double getRating() { return rating; }

    public void setRating(double rating) { this.rating = rating; }

    public long getVotes() { return votes; }

    public void setVotes(long votes) { this.votes = votes; }

    @JSON(name = "comment_count")
    public long getCommentCount() { return commentCount; }

    @JSON(name = "comment_count")
    public void setCommentCount(long commentCount) { this.commentCount = commentCount; }

    @JSON(name = "available_translations")
    public @NotNull List<String> getTranslations() { return translations; }

    @JSON(name = "available_translations")
    public void setTranslations(@NotNull List<String> translations) { this.translations = translations; }

    public int getRuntime() { return runtime; }

    public void setRuntime(int runtime) { this.runtime = runtime; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EpisodeModel)) return false;
        EpisodeModel that = (EpisodeModel) o;
        return season == that.season && number == that.number && rating == that.rating && votes == that.votes && commentCount == that.commentCount && runtime == that.runtime && title.equals(that.title) && ids.equals(that.ids) && Objects.equals(numAbs, that.numAbs) && overview.equals(that.overview) && firstAired.equals(that.firstAired) && updatedAt.equals(that.updatedAt) && translations.equals(that.translations);
    }

    public int hashCode() { return Objects.hash(season, number, title, ids, numAbs, overview, firstAired, updatedAt, rating, votes, commentCount, translations, runtime); }

    public String toString() {
        return "EpisodeModel{" +
                "season=" + season +
                ", number=" + number +
                ", title='" + title + '\'' +
                ", ids=" + ids +
                ", numAbs='" + numAbs + '\'' +
                ", overview='" + overview + '\'' +
                ", firstAired=" + firstAired +
                ", updatedAt=" + updatedAt +
                ", rating=" + rating +
                ", votes=" + votes +
                ", commentCount=" + commentCount +
                ", translations=" + translations +
                ", runtime=" + runtime +
                '}';
    }

    public static final class EpisodeIDListModel implements Serializable {
        private long trakt = -1L;
        private long tvdb = -1L;
        private String imdb = "";
        private long tmdb = -1L;
        private long tvrange = -1L;

        public EpisodeIDListModel() { }

        public long getTrakt() { return trakt; }

        public void setTrakt(long trakt) { this.trakt = trakt; }

        public long getTvdb() { return tvdb; }

        public void setTvdb(long tvdb) { this.tvdb = tvdb; }

        public @NotNull String getImdb() { return imdb; }

        public void setImdb(@NotNull String imdb) { this.imdb = imdb; }

        public long getTmdb() { return tmdb; }

        public void setTmdb(long tmdb) { this.tmdb = tmdb; }

        public long getTvrange() { return tvrange; }

        public void setTvrange(long tvrange) { this.tvrange = tvrange; }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EpisodeIDListModel)) return false;
            EpisodeIDListModel that = (EpisodeIDListModel) o;
            return trakt == that.trakt && tvdb == that.tvdb && tmdb == that.tmdb && tvrange == that.tvrange && imdb.equals(that.imdb);
        }

        public int hashCode() { return Objects.hash(trakt, tvdb, imdb, tmdb, tvrange); }

        public String toString() {
            return "EpisodeIDListModel{" +
                    "trakt=" + trakt +
                    ", tvdb=" + tvdb +
                    ", imdb='" + imdb + '\'' +
                    ", tmdb=" + tmdb +
                    ", tvrange=" + tvrange +
                    '}';
        }
    }
}