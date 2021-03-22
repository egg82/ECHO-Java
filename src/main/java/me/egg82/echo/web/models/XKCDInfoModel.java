package me.egg82.echo.web.models;

import flexjson.JSON;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class XKCDInfoModel implements Serializable {
    private int num = -1;

    private int day = -1;
    private int month = -1;
    private int year = -1;

    private String link = "";
    private String title = "";
    @JSON(name = "safe_title")
    private String safeTitle = "";
    private String news = "";

    private String img = "";
    private String alt = "";
    private String transcript = "";

    public XKCDInfoModel() { }

    public int getNum() { return num; }

    public void setNum(int num) { this.num = num; }

    public int getDay() { return day; }

    public void setDay(int day) { this.day = day; }

    public int getMonth() { return month; }

    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }

    public void setYear(int year) { this.year = year; }

    public @NotNull String getLink() { return link; }

    public void setLink(@NotNull String link) { this.link = link; }

    public @NotNull String getTitle() { return title; }

    public void setTitle(@NotNull String title) { this.title = title; }

    public @NotNull String getSafeTitle() { return safeTitle; }

    public void setSafeTitle(@NotNull String safeTitle) { this.safeTitle = safeTitle; }

    public @NotNull String getNews() { return news; }

    public void setNews(@NotNull String news) { this.news = news; }

    public @NotNull String getImg() { return img; }

    public void setImg(@NotNull String img) { this.img = img; }

    public @NotNull String getAlt() { return alt; }

    public void setAlt(@NotNull String alt) { this.alt = alt; }

    public @NotNull String getTranscript() { return transcript; }

    public void setTranscript(@NotNull String transcript) { this.transcript = transcript; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof XKCDInfoModel)) {
            return false;
        }
        XKCDInfoModel that = (XKCDInfoModel) o;
        return num == that.num && day == that.day && month == that.month && year == that.year && link.equals(that.link) && title.equals(that.title) && safeTitle.equals(
                that.safeTitle) && news.equals(that.news) && img.equals(that.img) && alt.equals(that.alt) && transcript.equals(that.transcript);
    }

    @Override
    public int hashCode() { return Objects.hash(num, day, month, year, link, title, safeTitle, news, img, alt, transcript); }

    @Override
    public String toString() {
        return "XKCDInfoModel{" +
                "num=" + num +
                ", day=" + day +
                ", month=" + month +
                ", year=" + year +
                ", link='" + link + '\'' +
                ", title='" + title + '\'' +
                ", safeTitle='" + safeTitle + '\'' +
                ", news='" + news + '\'' +
                ", img='" + img + '\'' +
                ", alt='" + alt + '\'' +
                ", transcript='" + transcript + '\'' +
                '}';
    }
}
