package me.egg82.echo.storage.models;

import io.ebean.annotation.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "echo_data")
public class DataModel extends BaseModel {
    @NotNull
    private String key;
    private String value;

    public DataModel() {
        super();
        this.key = "";
        this.value = null;
    }

    public DataModel(@org.jetbrains.annotations.NotNull String dbName) {
        super(dbName);
        this.key = "";
        this.value = null;
    }

    public @org.jetbrains.annotations.NotNull String getKey() { return key; }

    public void setKey(@org.jetbrains.annotations.NotNull String key) { this.key = key; }

    public @Nullable String getValue() { return value; }

    public void setValue(String value) { this.value = value; }

    @Override
    public String toString() {
        return "DataModel{" +
                "id=" + id +
                ", version=" + version +
                ", created=" + created +
                ", modified=" + modified +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
