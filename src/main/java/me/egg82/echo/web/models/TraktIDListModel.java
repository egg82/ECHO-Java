package me.egg82.echo.web.models;

import java.io.Serializable;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public class TraktIDListModel implements Serializable {
    private long trakt = -1L;
    private String slug = null;
    private Long tvdb = null;
    private String imdb = null;
    private Long tmdb = null;
    private Long tvrage = null;

    public TraktIDListModel() { }

    public long getTrakt() { return trakt; }

    public void setTrakt(long trakt) { this.trakt = trakt; }

    public @Nullable String getSlug() { return slug; }

    public void setSlug(String slug) { this.slug = slug; }

    public @Nullable Long getTvdb() { return tvdb; }

    public void setTvdb(Long tvdb) { this.tvdb = tvdb; }

    public @Nullable String getImdb() { return imdb; }

    public void setImdb(String imdb) { this.imdb = imdb; }

    public @Nullable Long getTmdb() { return tmdb; }

    public void setTmdb(Long tmdb) { this.tmdb = tmdb; }

    public @Nullable Long getTvrage() { return tvrage; }

    public void setTvrage(Long tvrage) { this.tvrage = tvrage; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TraktIDListModel)) return false;
        TraktIDListModel that = (TraktIDListModel) o;
        return trakt == that.trakt && Objects.equals(slug, that.slug) && Objects.equals(tvdb, that.tvdb) && Objects.equals(imdb, that.imdb) && Objects.equals(tmdb, that.tmdb) && Objects.equals(tvrage, that.tvrage);
    }

    public int hashCode() { return Objects.hash(trakt, slug, tvdb, imdb, tmdb, tvrage); }

    public String toString() {
        return "TraktIDListModel{" +
                "trakt=" + trakt +
                ", slug='" + slug + '\'' +
                ", tvdb=" + tvdb +
                ", imdb='" + imdb + '\'' +
                ", tmdb=" + tmdb +
                ", tvrage=" + tvrage +
                '}';
    }
}
