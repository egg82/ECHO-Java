package me.egg82.echo.web.models;

import java.io.Serializable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class ShowModel implements Serializable {
    private String title = "";
    private short year = -1;
    private ShowIDListModel ids = new ShowIDListModel();

    public ShowModel() { }

    public @NotNull String getTitle() { return title; }

    public void setTitle(@NotNull String title) { this.title = title; }

    public short getYear() { return year; }

    public void setYear(short year) { this.year = year; }

    public @NotNull ShowIDListModel getIds() { return ids; }

    public void setIds(@NotNull ShowIDListModel ids) { this.ids = ids; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShowModel)) return false;
        ShowModel that = (ShowModel) o;
        return year == that.year && title.equals(that.title) && ids.equals(that.ids);
    }

    public int hashCode() { return Objects.hash(title, year, ids); }

    public String toString() {
        return "ShowModel{" +
                "title='" + title + '\'' +
                ", year=" + year +
                ", ids=" + ids +
                '}';
    }

    public static final class ShowIDListModel implements Serializable {
        private long trakt = -1L;
        private String slug = "";
        private long tvdb = -1L;
        private String imdb = "";
        private long tmdb = -1L;

        public ShowIDListModel() { }

        public long getTrakt() { return trakt; }

        public void setTrakt(long trakt) { this.trakt = trakt; }

        public @NotNull String getSlug() { return slug; }

        public void setSlug(@NotNull String slug) { this.slug = slug; }

        public long getTvdb() { return tvdb; }

        public void setTvdb(long tvdb) { this.tvdb = tvdb; }

        public @NotNull String getImdb() { return imdb; }

        public void setImdb(@NotNull String imdb) { this.imdb = imdb; }

        public long getTmdb() { return tmdb; }

        public void setTmdb(long tmdb) { this.tmdb = tmdb; }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ShowIDListModel)) return false;
            ShowIDListModel that = (ShowIDListModel) o;
            return trakt == that.trakt && tvdb == that.tvdb && tmdb == that.tmdb && slug.equals(that.slug) && imdb.equals(that.imdb);
        }

        public int hashCode() { return Objects.hash(trakt, slug, tvdb, imdb, tmdb); }

        public String toString() {
            return "ShowIDListModel{" +
                    "trakt=" + trakt +
                    ", slug='" + slug + '\'' +
                    ", tvdb=" + tvdb +
                    ", imdb='" + imdb + '\'' +
                    ", tmdb=" + tmdb +
                    '}';
        }
    }
}
