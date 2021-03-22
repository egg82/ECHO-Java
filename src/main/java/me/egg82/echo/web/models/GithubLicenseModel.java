package me.egg82.echo.web.models;

import flexjson.JSON;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GithubLicenseModel implements Serializable {
    private String key = "";
    private String name = "";
    @JSON(name = "spdx_id")
    private String id = "";
    private String url = "";
    @JSON(name = "node_id")
    private String nodeId = "";
    @JSON(name = "html_url")
    private String htmlUrl = "";
    private String description = "";
    private String implementation = "";
    private List<String> permissions = new ArrayList<>();
    private List<String> conditions = new ArrayList<>();
    private List<String> limitations = new ArrayList<>();
    private String body = "";
    private boolean featured = false;

    public GithubLicenseModel() { }

    public @NotNull String getKey() { return key; }

    public void setKey(@NotNull String key) { this.key = key; }

    public @NotNull String getName() { return name; }

    public void setName(@NotNull String name) { this.name = name; }

    @JSON(name = "spdx_id")
    public @NotNull String getId() { return id; }

    @JSON(name = "spdx_id")
    public void setId(@NotNull String id) { this.id = id; }

    public @NotNull String getUrl() { return url; }

    public void setUrl(@NotNull String url) { this.url = url; }

    @JSON(name = "node_id")
    public @NotNull String getNodeId() { return nodeId; }

    @JSON(name = "node_id")
    public void setNodeId(@NotNull String nodeId) { this.nodeId = nodeId; }

    @JSON(name = "html_url")
    public @NotNull String getHtmlUrl() { return htmlUrl; }

    @JSON(name = "html_url")
    public void setHtmlUrl(@NotNull String htmlUrl) { this.htmlUrl = htmlUrl; }

    public @NotNull String getDescription() { return description; }

    public void setDescription(@NotNull String description) { this.description = description; }

    public @NotNull String getImplementation() { return implementation; }

    public void setImplementation(@NotNull String implementation) { this.implementation = implementation; }

    public @NotNull List<String> getPermissions() { return permissions; }

    public void setPermissions(@NotNull List<String> permissions) { this.permissions = permissions; }

    public @NotNull List<String> getConditions() { return conditions; }

    public void setConditions(@NotNull List<String> conditions) { this.conditions = conditions; }

    public @NotNull List<String> getLimitations() { return limitations; }

    public void setLimitations(@NotNull List<String> limitations) { this.limitations = limitations; }

    public @NotNull String getBody() { return body; }

    public void setBody(@NotNull String body) { this.body = body; }

    public boolean isFeatured() { return featured; }

    public void setFeatured(boolean featured) { this.featured = featured; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GithubLicenseModel)) return false;
        GithubLicenseModel that = (GithubLicenseModel) o;
        return featured == that.featured && key.equals(that.key) && name.equals(that.name) && id.equals(that.id) && url.equals(that.url) && nodeId.equals(that.nodeId) && htmlUrl.equals(that.htmlUrl) && description.equals(that.description) && implementation.equals(that.implementation) && permissions.equals(that.permissions) && conditions.equals(that.conditions) && limitations.equals(that.limitations) && body.equals(that.body);
    }

    @Override
    public int hashCode() { return Objects.hash(key, name, id, url, nodeId, htmlUrl, description, implementation, permissions, conditions, limitations, body, featured); }

    @Override
    public String toString() {
        return "GithubLicenseModel{" +
                "key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", nodeId='" + nodeId + '\'' +
                ", htmlUrl='" + htmlUrl + '\'' +
                ", description='" + description + '\'' +
                ", implementation='" + implementation + '\'' +
                ", permissions=" + permissions +
                ", conditions=" + conditions +
                ", limitations=" + limitations +
                ", body='" + body + '\'' +
                ", featured=" + featured +
                '}';
    }
}
