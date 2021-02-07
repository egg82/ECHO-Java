package me.egg82.echo.web.models;

import java.io.Serializable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class ShowModel implements Serializable {
    private String title = "";
    private short year = -1;
    private TraktIDListModel ids = new TraktIDListModel();

    public ShowModel() { }

    public @NotNull String getTitle() { return title; }

    public void setTitle(@NotNull String title) { this.title = title; }

    public short getYear() { return year; }

    public void setYear(short year) { this.year = year; }

    public @NotNull TraktIDListModel getIds() { return ids; }

    public void setIds(@NotNull TraktIDListModel ids) { this.ids = ids; }

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
}
