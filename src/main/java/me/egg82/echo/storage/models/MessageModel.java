package me.egg82.echo.storage.models;

import io.ebean.annotation.NotNull;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "echo_message")
public class MessageModel extends BaseModel {
    @NotNull
    private String message;

    public MessageModel() {
        this.message = "";
    }

    public MessageModel(@org.jetbrains.annotations.NotNull String dbName) {
        super(dbName);
        this.message = "";
    }

    public @org.jetbrains.annotations.NotNull String getMessage() { return message; }

    public void setMessage(@org.jetbrains.annotations.NotNull String message) { this.message = message; }

    public String toString() {
        return "MessageModel{" +
                "id=" + id +
                ", version=" + version +
                ", created=" + created +
                ", modified=" + modified +
                ", message='" + message + '\'' +
                '}';
    }
}
