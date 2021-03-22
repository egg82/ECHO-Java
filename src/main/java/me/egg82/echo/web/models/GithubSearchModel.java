package me.egg82.echo.web.models;

import flexjson.JSON;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GithubSearchModel implements Serializable {
    @JSON(name = "total_count")
    private long totalCount = -1L;
    @JSON(name = "incomplete_results")
    private boolean incomplete = false;
    private List<GithubRepositoryModel> items = new ArrayList<>();

    @JSON(name = "total_count")
    public long getTotalCount() { return totalCount; }

    @JSON(name = "total_count")
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

    @JSON(name = "incomplete_results")
    public boolean isIncomplete() { return incomplete; }

    @JSON(name = "incomplete_results")
    public void setIncomplete(boolean incomplete) { this.incomplete = incomplete; }

    public @NotNull List<GithubRepositoryModel> getItems() { return items; }

    public void setItems(@NotNull List<GithubRepositoryModel> items) { this.items = items; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GithubSearchModel)) return false;
        GithubSearchModel that = (GithubSearchModel) o;
        return totalCount == that.totalCount && incomplete == that.incomplete && items.equals(that.items);
    }

    @Override
    public int hashCode() { return Objects.hash(totalCount, incomplete, items); }

    @Override
    public String toString() {
        return "GithubSearchModel{" +
                "totalCount=" + totalCount +
                ", incomplete=" + incomplete +
                ", items=" + items +
                '}';
    }

    public static class GithubRepositoryModel implements Serializable {
        private long id = -1L;
        @JSON(name = "node_id")
        private String nodeId = "";
        private String name = "";
        @JSON(name = "full_name")
        private String fullName = "";
        @JSON(name = "private")
        private boolean privateRepo = false;
        private GithubRepositoryOwnerModel owner = new GithubRepositoryOwnerModel();
        private String description = "";
        private boolean fork = false;
        @JSON(name = "created_at")
        private Instant createdAt = Instant.now();
        @JSON(name = "updated_at")
        private Instant updatedAt = Instant.now();
        @JSON(name = "pushed_at")
        private Instant pushedAt = Instant.now();
        private String homepage = null;
        private long size = -1L;
        @JSON(name = "stargazers_count")
        private long stargazers = -1L;
        @JSON(name = "watchers_count")
        private long watchers = -1L;
        @JSON(name = "forks_count")
        private long forks = -1L;
        private String language = null;
        @JSON(name = "has_issues")
        private boolean issues;
        @JSON(name = "has_projects")
        private boolean projects;
        @JSON(name = "has_downloads")
        private boolean downloads;
        @JSON(name = "has_wiki")
        private boolean wiki;
        @JSON(name = "has_pages")
        private boolean pages;
        @JSON(name = "is_archived")
        private boolean archived;
        @JSON(name = "is_disabled")
        private boolean disabled;
        @JSON(name = "open_issues_count")
        private long openIssues = -1L;
        private GithubRepositoryLicenseModel license = null;
        @JSON(name = "default_branch")
        private String defaultBranch = "";
        private double score = -1.0d;

        public GithubRepositoryModel() { }

        public long getId() { return id; }

        public void setId(long id) { this.id = id; }

        @JSON(name = "node_id")
        public @NotNull String getNodeId() { return nodeId; }

        @JSON(name = "node_id")
        public void setNodeId(@NotNull String nodeId) { this.nodeId = nodeId; }

        public @NotNull String getName() { return name; }

        public void setName(@NotNull String name) { this.name = name; }

        @JSON(name = "full_name")
        public @NotNull String getFullName() { return fullName; }

        @JSON(name = "full_name")
        public void setFullName(@NotNull String fullName) { this.fullName = fullName; }

        @JSON(name = "private")
        public boolean isPrivateRepo() { return privateRepo; }

        @JSON(name = "private")
        public void setPrivateRepo(boolean privateRepo) { this.privateRepo = privateRepo; }

        public @NotNull GithubRepositoryOwnerModel getOwner() { return owner; }

        public void setOwner(@NotNull GithubRepositoryOwnerModel owner) { this.owner = owner; }

        public @NotNull String getDescription() { return description; }

        public void setDescription(@NotNull String description) { this.description = description; }

        public boolean isFork() { return fork; }

        public void setFork(boolean fork) { this.fork = fork; }

        @JSON(name = "created_at")
        public @NotNull Instant getCreatedAt() { return createdAt; }

        @JSON(name = "created_at")
        public void setCreatedAt(@NotNull Instant createdAt) { this.createdAt = createdAt; }

        @JSON(name = "updated_at")
        public @NotNull Instant getUpdatedAt() { return updatedAt; }

        @JSON(name = "updated_at")
        public void setUpdatedAt(@NotNull Instant updatedAt) { this.updatedAt = updatedAt; }

        @JSON(name = "pushed_at")
        public @NotNull Instant getPushedAt() { return pushedAt; }

        @JSON(name = "pushed_at")
        public void setPushedAt(@NotNull Instant pushedAt) { this.pushedAt = pushedAt; }

        public @Nullable String getHomepage() { return homepage; }

        public void setHomepage(String homepage) { this.homepage = homepage; }

        public long getSize() { return size; }

        public void setSize(long size) { this.size = size; }

        @JSON(name = "stargazers_count")
        public long getStargazers() { return stargazers; }

        @JSON(name = "stargazers_count")
        public void setStargazers(long stargazers) { this.stargazers = stargazers; }

        @JSON(name = "watchers_count")
        public long getWatchers() { return watchers; }

        @JSON(name = "watchers_count")
        public void setWatchers(long watchers) { this.watchers = watchers; }

        @JSON(name = "forks_count")
        public long getForks() { return forks; }

        @JSON(name = "forks_count")
        public void setForks(long forks) { this.forks = forks; }

        public @Nullable String getLanguage() { return language; }

        public void setLanguage(String language) { this.language = language; }

        @JSON(name = "has_issues")
        public boolean isIssues() { return issues; }

        @JSON(name = "has_issues")
        public void setIssues(boolean issues) { this.issues = issues; }

        @JSON(name = "has_projects")
        public boolean isProjects() { return projects; }

        @JSON(name = "has_projects")
        public void setProjects(boolean projects) { this.projects = projects; }

        @JSON(name = "has_downloads")
        public boolean isDownloads() { return downloads; }

        @JSON(name = "has_downloads")
        public void setDownloads(boolean downloads) { this.downloads = downloads; }

        @JSON(name = "has_wiki")
        public boolean isWiki() { return wiki; }

        @JSON(name = "has_wiki")
        public void setWiki(boolean wiki) { this.wiki = wiki; }

        @JSON(name = "has_pages")
        public boolean isPages() { return pages; }

        @JSON(name = "has_pages")
        public void setPages(boolean pages) { this.pages = pages; }

        @JSON(name = "is_archived")
        public boolean isArchived() { return archived; }

        @JSON(name = "is_archived")
        public void setArchived(boolean archived) { this.archived = archived; }

        @JSON(name = "is_disabled")
        public boolean isDisabled() { return disabled; }

        @JSON(name = "is_disabled")
        public void setDisabled(boolean disabled) { this.disabled = disabled; }

        @JSON(name = "open_issues_count")
        public long getOpenIssues() { return openIssues; }

        @JSON(name = "open_issues_count")
        public void setOpenIssues(long openIssues) { this.openIssues = openIssues; }

        public @Nullable GithubRepositoryLicenseModel getLicense() { return license; }

        public void setLicense(GithubRepositoryLicenseModel license) { this.license = license; }

        @JSON(name = "default_branch")
        public @NotNull String getDefaultBranch() { return defaultBranch; }

        @JSON(name = "default_branch")
        public void setDefaultBranch(@NotNull String defaultBranch) { this.defaultBranch = defaultBranch; }

        public double getScore() { return score; }

        public void setScore(double score) { this.score = score; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GithubRepositoryModel)) return false;
            GithubRepositoryModel that = (GithubRepositoryModel) o;
            return id == that.id && privateRepo == that.privateRepo && fork == that.fork && size == that.size && stargazers == that.stargazers && watchers == that.watchers && forks == that.forks && issues == that.issues && projects == that.projects && downloads == that.downloads && wiki == that.wiki && pages == that.pages && archived == that.archived && disabled == that.disabled && openIssues == that.openIssues && Double.compare(that.score, score) == 0 && nodeId.equals(that.nodeId) && name.equals(that.name) && fullName.equals(that.fullName) && owner.equals(that.owner) && description.equals(that.description) && createdAt.equals(that.createdAt) && updatedAt.equals(that.updatedAt) && pushedAt.equals(that.pushedAt) && Objects.equals(homepage, that.homepage) && Objects.equals(language, that.language) && Objects.equals(license, that.license) && defaultBranch.equals(that.defaultBranch);
        }

        @Override
        public int hashCode() { return Objects.hash(id, nodeId, name, fullName, privateRepo, owner, description, fork, createdAt, updatedAt, pushedAt, homepage, size, stargazers, watchers, forks, language, issues, projects, downloads, wiki, pages, archived, disabled, openIssues, license, defaultBranch, score); }

        @Override
        public String toString() {
            return "GithubRepositoryModel{" +
                    "id=" + id +
                    ", nodeId='" + nodeId + '\'' +
                    ", name='" + name + '\'' +
                    ", fullName='" + fullName + '\'' +
                    ", privateRepo=" + privateRepo +
                    ", owner=" + owner +
                    ", description='" + description + '\'' +
                    ", fork=" + fork +
                    ", createdAt=" + createdAt +
                    ", updatedAt=" + updatedAt +
                    ", pushedAt=" + pushedAt +
                    ", homepage='" + homepage + '\'' +
                    ", size=" + size +
                    ", stargazers=" + stargazers +
                    ", watchers=" + watchers +
                    ", forks=" + forks +
                    ", language='" + language + '\'' +
                    ", issues=" + issues +
                    ", projects=" + projects +
                    ", downloads=" + downloads +
                    ", wiki=" + wiki +
                    ", pages=" + pages +
                    ", archived=" + archived +
                    ", disabled=" + disabled +
                    ", openIssues=" + openIssues +
                    ", license=" + license +
                    ", defaultBranch='" + defaultBranch + '\'' +
                    ", score=" + score +
                    '}';
        }

        public static class GithubRepositoryOwnerModel implements Serializable {
            private String login = "";
            private long id = -1L;
            @JSON(name = "node_id")
            private String nodeId = "";
            @JSON(name = "avatar_url")
            private String avatarUrl = "";
            @JSON(name = "gravatar_id")
            private String gravatarId = "";
            private String type = "";
            @JSON(name = "site_admin")
            private boolean siteAdmin = false;

            public GithubRepositoryOwnerModel() { }

            public @NotNull String getLogin() { return login; }

            public void setLogin(@NotNull String login) { this.login = login; }

            public long getId() { return id; }

            public void setId(long id) { this.id = id; }

            public @NotNull String getNodeId() { return nodeId; }

            public void setNodeId(@NotNull String nodeId) { this.nodeId = nodeId; }

            public @NotNull String getAvatarUrl() { return avatarUrl; }

            public void setAvatarUrl(@NotNull String avatarUrl) { this.avatarUrl = avatarUrl; }

            public @NotNull String getGravatarId() { return gravatarId; }

            public void setGravatarId(@NotNull String gravatarId) { this.gravatarId = gravatarId; }

            public @NotNull String getType() { return type; }

            public void setType(@NotNull String type) { this.type = type; }

            public boolean isSiteAdmin() { return siteAdmin; }

            public void setSiteAdmin(boolean siteAdmin) { this.siteAdmin = siteAdmin; }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof GithubRepositoryOwnerModel)) return false;
                GithubRepositoryOwnerModel that = (GithubRepositoryOwnerModel) o;
                return id == that.id && siteAdmin == that.siteAdmin && login.equals(that.login) && nodeId.equals(that.nodeId) && avatarUrl.equals(that.avatarUrl) && gravatarId.equals(that.gravatarId) && type.equals(that.type);
            }

            @Override
            public int hashCode() { return Objects.hash(login, id, nodeId, avatarUrl, gravatarId, type, siteAdmin); }

            @Override
            public String toString() {
                return "GithubRepositoryOwnerModel{" +
                        "login='" + login + '\'' +
                        ", id=" + id +
                        ", nodeId='" + nodeId + '\'' +
                        ", avatarUrl='" + avatarUrl + '\'' +
                        ", gravatarId='" + gravatarId + '\'' +
                        ", type='" + type + '\'' +
                        ", siteAdmin=" + siteAdmin +
                        '}';
            }
        }

        public static class GithubRepositoryLicenseModel implements Serializable {
            private String key = "";
            private String name = "";
            @JSON(name = "spdx_id")
            private String id = "";
            private String url = "";
            @JSON(name = "node_id")
            private String nodeId = "";

            public GithubRepositoryLicenseModel() { }

            public @NotNull String getKey() { return key; }

            public void setKey(@NotNull String key) { this.key = key; }

            public @NotNull String getName() { return name; }

            public void setName(@NotNull String name) { this.name = name; }

            @JSON(name = "spdx_id")
            public @NotNull String getId() { return id; }

            @JSON(name = "spdx_id")
            public void setId(@NotNull String id) { this.id = id; }

            public @Nullable String getUrl() { return url; }

            public void setUrl(String url) { this.url = url; }

            @JSON(name = "node_id")
            public @NotNull String getNodeId() { return nodeId; }

            @JSON(name = "node_id")
            public void setNodeId(@NotNull String nodeId) { this.nodeId = nodeId; }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof GithubRepositoryLicenseModel)) return false;
                GithubRepositoryLicenseModel that = (GithubRepositoryLicenseModel) o;
                return key.equals(that.key) && name.equals(that.name) && id.equals(that.id) && Objects.equals(url, that.url) && nodeId.equals(that.nodeId);
            }

            @Override
            public int hashCode() { return Objects.hash(key, name, id, url, nodeId); }

            @Override
            public String toString() {
                return "GithubRepositoryLicenseModel{" +
                        "key='" + key + '\'' +
                        ", name='" + name + '\'' +
                        ", id='" + id + '\'' +
                        ", url='" + url + '\'' +
                        ", nodeId='" + nodeId + '\'' +
                        '}';
            }
        }
    }
}
