package me.egg82.echo.web.models;

import flexjson.JSON;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImgurUploadModel implements Serializable {
    private ImgurUploadDataModel data = new ImgurUploadDataModel();
    private boolean success = false;
    private int status = -1;

    public ImgurUploadModel() { }

    public @NotNull ImgurUploadDataModel getData() { return data; }

    public void setData(@NotNull ImgurUploadDataModel data) { this.data = data; }

    public boolean isSuccess() { return success; }

    public void setSuccess(boolean success) { this.success = success; }

    public int getStatus() { return status; }

    public void setStatus(int status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImgurUploadModel)) {
            return false;
        }
        ImgurUploadModel that = (ImgurUploadModel) o;
        return success == that.success && status == that.status && data.equals(that.data);
    }

    @Override
    public int hashCode() { return Objects.hash(data, success, status); }

    @Override
    public String toString() {
        return "ImgurUploadModel{" +
                "data=" + data +
                ", success=" + success +
                ", status=" + status +
                '}';
    }

    public static class ImgurUploadDataModel implements Serializable {
        private String id = "";
        private String title = null;
        private String description = null;
        @JSON(name = "datetime")
        private Instant dateTime = Instant.now();
        private String type = "";
        private boolean animated = false;
        private long width = -1L;
        private long height = -1L;
        private long size = -1L;
        private long views = -1L;
        private long bandwidth = -1L;
        private String vote = null;
        private boolean favorite = false;
        private String nsfw = null;
        private String section = null;
        @JSON(name = "account_url")
        private String accountUrl = null;
        @JSON(name = "is_ad")
        private boolean ad = false;
        @JSON(name = "in_most_viral")
        private boolean inMostViral = false;
        private List<String> tags = new ArrayList<>();
        @JSON(name = "ad_url")
        private String adUrl = null;
        @JSON(name = "in_gallery")
        private boolean inGallery;
        @JSON(name = "deletehash")
        private String deleteHash = "";
        private String name = "";
        private String link = "";

        public ImgurUploadDataModel() { }

        public @NotNull String getId() { return id; }

        public void setId(@NotNull String id) { this.id = id; }

        public @Nullable String getTitle() { return title; }

        public void setTitle(String title) { this.title = title; }

        public @Nullable String getDescription() { return description; }

        public void setDescription(String description) { this.description = description; }

        @JSON(name = "datetime")
        public @NotNull Instant getDateTime() { return dateTime; }

        @JSON(name = "datetime")
        public void setDateTime(@NotNull Instant dateTime) { this.dateTime = dateTime; }

        public @NotNull String getType() { return type; }

        public void setType(@NotNull String type) { this.type = type; }

        public boolean isAnimated() { return animated; }

        public void setAnimated(boolean animated) { this.animated = animated; }

        public long getWidth() { return width; }

        public void setWidth(long width) { this.width = width; }

        public long getHeight() { return height; }

        public void setHeight(long height) { this.height = height; }

        public long getSize() { return size; }

        public void setSize(long size) { this.size = size; }

        public long getViews() { return views; }

        public void setViews(long views) { this.views = views; }

        public long getBandwidth() { return bandwidth; }

        public void setBandwidth(long bandwidth) { this.bandwidth = bandwidth; }

        public @Nullable String getVote() { return vote; }

        public void setVote(String vote) { this.vote = vote; }

        public boolean isFavorite() { return favorite; }

        public void setFavorite(boolean favorite) { this.favorite = favorite; }

        public @Nullable String getNsfw() { return nsfw; }

        public void setNsfw(String nsfw) { this.nsfw = nsfw; }

        public @Nullable String getSection() { return section; }

        public void setSection(String section) { this.section = section; }

        @JSON(name = "account_url")
        public @Nullable String getAccountUrl() { return accountUrl; }

        @JSON(name = "account_url")
        public void setAccountUrl(String accountUrl) { this.accountUrl = accountUrl; }

        @JSON(name = "is_ad")
        public boolean isAd() { return ad; }

        @JSON(name = "is_ad")
        public void setAd(boolean ad) { this.ad = ad; }

        @JSON(name = "in_most_viral")
        public boolean isInMostViral() { return inMostViral; }

        @JSON(name = "in_most_viral")
        public void setInMostViral(boolean inMostViral) { this.inMostViral = inMostViral; }

        public @NotNull List<String> getTags() { return tags; }

        public void setTags(@NotNull List<String> tags) { this.tags = tags; }

        @JSON(name = "ad_url")
        public @Nullable String getAdUrl() { return adUrl; }

        @JSON(name = "ad_url")
        public void setAdUrl(String adUrl) { this.adUrl = adUrl; }

        @JSON(name = "in_gallery")
        public boolean isInGallery() { return inGallery; }

        @JSON(name = "in_gallery")
        public void setInGallery(boolean inGallery) { this.inGallery = inGallery; }

        @JSON(name = "deletehash")
        public @NotNull String getDeleteHash() { return deleteHash; }

        @JSON(name = "deletehash")
        public void setDeleteHash(@NotNull String deleteHash) { this.deleteHash = deleteHash; }

        public @NotNull String getName() { return name; }

        public void setName(@NotNull String name) { this.name = name; }

        public @NotNull String getLink() { return link; }

        public void setLink(@NotNull String link) { this.link = link; }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ImgurUploadDataModel)) {
                return false;
            }
            ImgurUploadDataModel that = (ImgurUploadDataModel) o;
            return dateTime == that.dateTime && animated == that.animated && width == that.width && height == that.height && size == that.size && views == that.views && bandwidth == that.bandwidth && favorite == that.favorite && ad == that.ad && inMostViral == that.inMostViral && inGallery == that.inGallery && id
                    .equals(that.id) && Objects.equals(title, that.title) && Objects.equals(description, that.description) && type.equals(that.type) && Objects.equals(
                    vote,
                    that.vote
            ) && Objects.equals(nsfw, that.nsfw) && Objects.equals(section, that.section) && Objects.equals(
                    accountUrl,
                    that.accountUrl
            ) && tags.equals(that.tags) && Objects.equals(adUrl, that.adUrl) && deleteHash.equals(that.deleteHash) && name.equals(that.name) && link.equals(that.link);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    id,
                    title,
                    description,
                    dateTime,
                    type,
                    animated,
                    width,
                    height,
                    size,
                    views,
                    bandwidth,
                    vote,
                    favorite,
                    nsfw,
                    section,
                    accountUrl,
                    ad,
                    inMostViral,
                    tags,
                    adUrl,
                    inGallery,
                    deleteHash,
                    name,
                    link
            );
        }

        @Override
        public String toString() {
            return "ImgurUploadDataModel{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", dateTime=" + dateTime +
                    ", type='" + type + '\'' +
                    ", animated=" + animated +
                    ", width=" + width +
                    ", height=" + height +
                    ", size=" + size +
                    ", views=" + views +
                    ", bandwidth=" + bandwidth +
                    ", vote='" + vote + '\'' +
                    ", favorite=" + favorite +
                    ", nsfw='" + nsfw + '\'' +
                    ", section='" + section + '\'' +
                    ", accountUrl='" + accountUrl + '\'' +
                    ", ad=" + ad +
                    ", inMostViral=" + inMostViral +
                    ", tags=" + tags +
                    ", adUrl='" + adUrl + '\'' +
                    ", inGallery=" + inGallery +
                    ", deleteHash='" + deleteHash + '\'' +
                    ", name='" + name + '\'' +
                    ", link='" + link + '\'' +
                    '}';
        }
    }
}
