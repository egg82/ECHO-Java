package me.egg82.echo.storage.models;

import io.ebean.annotation.NotNull;
import java.util.Arrays;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "echo_upload", uniqueConstraints = @UniqueConstraint(columnNames = { "hash", "service" }))
public class UploadModel extends BaseModel {
    @NotNull
    private String hash;
    @NotNull
    private String service;
    @NotNull
    private byte[] data;

    public UploadModel() {
        super();
        this.hash = "";
        this.service = "";
        this.data = new byte[0];
    }

    public UploadModel(@org.jetbrains.annotations.NotNull String dbName) {
        super(dbName);
        this.hash = "";
        this.service = "";
        this.data = new byte[0];
    }

    public @org.jetbrains.annotations.NotNull String getHash() { return hash; }

    public void setHash(@org.jetbrains.annotations.NotNull  String hash) { this.hash = hash; }

    public @org.jetbrains.annotations.NotNull String getService() { return service; }

    public void setService(@org.jetbrains.annotations.NotNull String service) { this.service = service; }

    public byte @org.jetbrains.annotations.NotNull [] getData() { return data; }

    public void setData(byte @org.jetbrains.annotations.NotNull [] data) { this.data = data; }

    public String toString() {
        return "UploadModel{" +
                "id=" + id +
                ", version=" + version +
                ", created=" + created +
                ", modified=" + modified +
                ", hash='" + hash + '\'' +
                ", service='" + service + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
