package me.egg82.echo.storage.models;

import io.ebean.annotation.NotNull;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "echo_learn")
public class LearnModel extends BaseModel {
    @NotNull
    private long user;
    @NotNull
    private boolean learning;

    public LearnModel() {
        super();
        this.user = -1L;
        this.learning = false;
    }

    public LearnModel(@org.jetbrains.annotations.NotNull String dbName) {
        super(dbName);
        this.user = -1L;
        this.learning = false;
    }

    public long getUser() { return user; }

    public void setUser(long user) { this.user = user; }

    public boolean isLearning() { return learning; }

    public void setLearning(boolean learn) { this.learning = learn; }

    public String toString() {
        return "LearnModel{" +
                "id=" + id +
                ", version=" + version +
                ", created=" + created +
                ", modified=" + modified +
                ", user=" + user +
                ", learning=" + learning +
                '}';
    }
}
