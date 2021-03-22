package me.egg82.echo.storage.models;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;
import java.time.Instant;

@MappedSuperclass
public abstract class BaseModel extends Model implements Serializable {
    @Id
    protected Long id;
    @Version
    protected Long version;
    @WhenCreated
    protected Instant created;
    @WhenModified
    protected Instant modified;

    protected BaseModel() {
        super();
        this.id = null;
        this.version = null;
        this.created = null;
        this.modified = null;
    }

    protected BaseModel(String dbName) {
        super(dbName);
        this.id = null;
        this.version = null;
        this.created = null;
        this.modified = null;
    }

    public @Nullable Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public @Nullable Long getVersion() { return version; }

    public void setVersion(Long version) { this.version = version; }

    public @Nullable Instant getCreated() {  return created; }

    public void setCreated(Instant created) { this.created = created; }

    public @Nullable Instant getModified() { return modified; }

    public void setModified(Instant modified) { this.modified = modified; }

    @Override
    public String toString() {
        return "BaseModel{" +
                "id=" + id +
                ", version=" + version +
                ", created=" + created +
                ", modified=" + modified +
                '}';
    }
}
