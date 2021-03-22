package me.egg82.echo.web.models;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class TrendingShowModel implements Serializable {
    private long watchers = -1L;
    private ShowModel show = new ShowModel();

    public TrendingShowModel() { }

    public long getWatchers() { return watchers; }

    public void setWatchers(long watchers) { this.watchers = watchers; }

    public @NotNull ShowModel getShow() { return show; }

    public void setShow(@NotNull ShowModel show) { this.show = show; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrendingShowModel)) return false;
        TrendingShowModel showModel = (TrendingShowModel) o;
        return watchers == showModel.watchers && show.equals(showModel.show);
    }

    @Override
    public int hashCode() { return Objects.hash(watchers, show); }

    @Override
    public String toString() {
        return "TrendingShowModel{" +
                "watchers=" + watchers +
                ", show=" + show +
                '}';
    }
}
