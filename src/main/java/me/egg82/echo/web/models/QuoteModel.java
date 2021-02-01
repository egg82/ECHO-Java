package me.egg82.echo.web.models;

import flexjson.JSON;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class QuoteModel implements Serializable {
    @JSON(name = "_id")
    private String id = "";
    private List<String> tags = new ArrayList<>();
    private String content = "";
    private String author = "";
    private long length = -1L;

    public QuoteModel() { }

    public @NotNull String getId() { return id; }

    public void setId(@NotNull String id) { this.id = id; }

    public @NotNull List<String> getTags() { return tags; }

    public void setTags(@NotNull List<String> tags) { this.tags = tags; }

    public @NotNull String getContent() { return content; }

    public void setContent(@NotNull String content) { this.content = content; }

    public @NotNull String getAuthor() { return author; }

    public void setAuthor(@NotNull String author) { this.author = author; }

    public long getLength() { return length; }

    public void setLength(long length) { this.length = length; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuoteModel)) return false;
        QuoteModel that = (QuoteModel) o;
        return length == that.length && id.equals(that.id) && tags.equals(that.tags) && content.equals(that.content) && author.equals(that.author);
    }

    public int hashCode() { return Objects.hash(id, tags, content, author, length); }

    public String toString() {
        return "QuoteModel{" +
                "id='" + id + '\'' +
                ", tags=" + tags +
                ", content='" + content + '\'' +
                ", author='" + author + '\'' +
                ", length=" + length +
                '}';
    }
}
