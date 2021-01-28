package me.egg82.echo.web.models;

import java.io.Serializable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class RoryModel implements Serializable {
    private int id = -1;
    private String url = "";

    public RoryModel() { }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public @NotNull String getUrl() { return url; }

    public void setUrl(@NotNull String url) { this.url = url; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoryModel)) return false;
        RoryModel roryModel = (RoryModel) o;
        return id == roryModel.id && url.equals(roryModel.url);
    }

    public int hashCode() { return Objects.hash(id, url); }

    public String toString() {
        return "RoryModel{" +
                "id=" + id +
                ", url='" + url + '\'' +
                '}';
    }
}
