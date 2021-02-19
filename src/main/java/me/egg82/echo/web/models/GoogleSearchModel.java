package me.egg82.echo.web.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GoogleSearchModel implements Serializable {
    private List<GoogleSearchItemModel> items = new ArrayList<>();

    public GoogleSearchModel() { }

    public @NotNull List<GoogleSearchItemModel> getItems() { return items; }

    public void setItems(@NotNull List<GoogleSearchItemModel> items) { this.items = items; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoogleSearchModel)) return false;
        GoogleSearchModel that = (GoogleSearchModel) o;
        return items.equals(that.items);
    }

    public int hashCode() { return Objects.hash(items); }

    public String toString() {
        return "GoogleSearchModel{" +
                "items=" + items +
                '}';
    }

    public static class GoogleSearchItemModel implements Serializable {
        private String title = null;
        private String link = "";

        public GoogleSearchItemModel() { }

        public @Nullable String getTitle() { return title; }

        public void setTitle(String title) { this.title = title; }

        public @NotNull String getLink() { return link; }

        public void setLink(@NotNull String link) { this.link = link; }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GoogleSearchItemModel)) return false;
            GoogleSearchItemModel that = (GoogleSearchItemModel) o;
            return Objects.equals(title, that.title) && link.equals(that.link);
        }

        public int hashCode() { return Objects.hash(title, link); }

        public String toString() {
            return "GoogleSearchItemModel{" +
                    "title='" + title + '\'' +
                    ", link='" + link + '\'' +
                    '}';
        }
    }
}
