package me.egg82.echo.storage.models;

import io.ebean.annotation.NotNull;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "echo_web", uniqueConstraints = @UniqueConstraint(columnNames = { "hash", "service" }))
public class WebModel extends BaseModel {
    @NotNull
    private String hash;
    @NotNull
    private String service;
    @NotNull
    private String path;

    public WebModel() {
        super();
        this.hash = "";
        this.service = "";
        this.path = "";
    }

    public WebModel(@org.jetbrains.annotations.NotNull String dbName) {
        super(dbName);
        this.hash = "";
        this.service = "";
        this.path = "";
    }

    public @org.jetbrains.annotations.NotNull String getHash() { return hash; }

    public void setHash(@org.jetbrains.annotations.NotNull String hash) { this.hash = hash; }

    public @org.jetbrains.annotations.NotNull String getService() { return service; }

    public void setService(@org.jetbrains.annotations.NotNull String service) { this.service = service; }

    public @org.jetbrains.annotations.NotNull String getPath() { return path; }

    public void setPath(@org.jetbrains.annotations.NotNull String path) { this.path = path; }

    public String toString() {
        return "WebModel{" +
                "id=" + id +
                ", version=" + version +
                ", created=" + created +
                ", modified=" + modified +
                ", hash='" + hash + '\'' +
                ", service='" + service + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
