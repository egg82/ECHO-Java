package me.egg82.echo.web.models;

import flexjson.JSON;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExtractionModel implements Serializable {
    private String url = "";
    private String status = "";
    private String domain = "";
    private String title = "";
    private List<String> author = new ArrayList<>();
    @JSON(name = "date_published")
    private String datePublished = null;
    private List<String> images = new ArrayList<>();
    private List<String> videos = new ArrayList<>();
    private String text = "";
    private String html = "";

    public ExtractionModel() { }

    public @NotNull String getUrl() { return url; }

    public void setUrl(@NotNull String url) { this.url = url; }

    public @NotNull String getStatus() { return status; }

    public void setStatus(@NotNull String status) { this.status = status; }

    public @NotNull String getDomain() { return domain; }

    public void setDomain(@NotNull String domain) { this.domain = domain; }

    public @NotNull String getTitle() { return title; }

    public void setTitle(@NotNull String title) { this.title = title; }

    public @NotNull List<String> getAuthor() { return author; }

    public void setAuthor(@NotNull List<String> author) { this.author = author; }

    public @Nullable String getDatePublished() { return datePublished; }

    public void setDatePublished(String datePublished) { this.datePublished = datePublished; }

    public @NotNull List<String> getImages() { return images; }

    public void setImages(@NotNull List<String> images) { this.images = images; }

    public @NotNull List<String> getVideos() { return videos; }

    public void setVideos(@NotNull List<String> videos) { this.videos = videos; }

    public @NotNull String getText() { return text; }

    public void setText(@NotNull String text) { this.text = text; }

    public @NotNull String getHtml() { return html; }

    public void setHtml(@NotNull String html) { this.html = html; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtractionModel)) return false;
        ExtractionModel that = (ExtractionModel) o;
        return url.equals(that.url) && status.equals(that.status) && domain.equals(that.domain) && title.equals(that.title) && author.equals(that.author) && Objects.equals(datePublished, that.datePublished) && images.equals(that.images) && videos.equals(that.videos) && text.equals(that.text) && html.equals(that.html);
    }

    public int hashCode() { return Objects.hash(url, status, domain, title, author, datePublished, images, videos, text, html); }

    public String toString() {
        return "ExtractionModel{" +
                "url='" + url + '\'' +
                ", status='" + status + '\'' +
                ", domain='" + domain + '\'' +
                ", title='" + title + '\'' +
                ", author=" + author +
                ", datePublished='" + datePublished + '\'' +
                ", images=" + images +
                ", videos=" + videos +
                ", text='" + text + '\'' +
                ", html='" + html + '\'' +
                '}';
    }
}
