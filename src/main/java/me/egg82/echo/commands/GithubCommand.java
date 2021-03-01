package me.egg82.echo.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import flexjson.JSONDeserializer;
import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.utils.WebUtil;
import me.egg82.echo.web.models.GithubLicenseModel;
import me.egg82.echo.web.models.GithubSearchModel;
import me.egg82.echo.web.transformers.InstantTransformer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("github|gh")
public class GithubCommand extends AbstractCommand {
    private static final String SEARCH_URL = "https://api.github.com/search/repositories?q=%s";
    private static final String REPO_URL = "https://github.com/%s";
    private static final String WIKI_URL = "https://github.com/%s/wiki";
    private static final String ISSUES_URL = "https://github.com/%s/issues";
    private static final String CUSTOM_LICENSE_URL = "https://github.com/%s/blob/master/LICENSE.md";
    private static final String README_URL = "https://github.com/%s/blob/master/README.md";
    private static final String RAW_README_URL = "https://raw.githubusercontent.com/%s/master/README.md";

    // I am so very sorry.
    private static final Pattern RE_IMAGES = Pattern.compile("\\[\\!\\[([^\\[\\]\\(\\)]+)\\]\\s*\\((https?:\\/\\/[^\\[\\]\\(\\)]*)\\)\\]\\s*\\(([^\\[\\]\\(\\)]+)\\)");
    private static final Pattern RE_IMAGES_2 = Pattern.compile("\\[\\!\\[([^\\[\\]\\(\\)]+)\\]\\s*\\((https?:\\/\\/[^\\[\\]\\(\\)]*)\\)\\]\\s*\\[([^\\[\\]\\(\\)]+)\\]");
    private static final Pattern RE_IMAGES_3 = Pattern.compile("\\!\\[(.*)\\]\\s*\\((https?:\\/\\/[^\\[\\]\\(\\)]*)\\)");
    private static final Pattern RE_LINKS_GROUP = Pattern.compile("\\[\\[([^\\[\\]\\(\\)]+)\\]\\s*\\((https?:\\/\\/[^\\[\\]\\(\\)]*)\\)\\]\\s*\\((https?:\\/\\/[^\\[\\]\\(\\)]*)\\)");
    private static final Pattern RE_LINKS_GROUP_2 = Pattern.compile("\\[([^\\[\\]\\(\\)]+)\\]\\s*\\((https?:\\/\\/[^\\[\\]\\(\\)]*)\\)");
    private static final Pattern RE_LINKS_REPLACE_GROUP = Pattern.compile("\\[(.*)\\]:\\s*(https?:\\/\\/[^\\[\\]\\(\\)]*)");
    private static final Pattern RE_LINKS = Pattern.compile("(https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*))");
    private static final Pattern RE_LINKS_2 = Pattern.compile("(https?:\\/\\/.*)[\\!\\.\\?]");
    private static final Pattern RE_LINKS_REPLACE_GROUP_MATCH = Pattern.compile("\\[([^\\[\\]\\(\\)]*)\\]");
    private static final Pattern RE_URL_LINE = Pattern.compile("^#*?\\s*<(?:url|img)>\\s*$", Pattern.MULTILINE);
    private static final Pattern RE_MULTIPLE_LINES = Pattern.compile("\n{3,}");

    public GithubCommand() { }

    public boolean requiresAdmin() { return false; }

    public @Nullable EmbedBuilder getDescription() { return null; }

    @Default
    @Description("{@@description.github}")
    @Syntax("<search>")
    public void submit(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull String query) {
        CachedConfig cachedConfig = getCachedConfig(issuer);
        if (cachedConfig == null || !canRun(event, cachedConfig)) {
            return;
        }

        getModel(query).whenCompleteAsync((val, ex) -> {
            if (!canCompleteContinue(issuer, val, ex)) {
                return;
            }

            GithubSearchModel.GithubRepositoryModel first = null;
            for (GithubSearchModel.GithubRepositoryModel model : val.getItems()) {
                if (!model.isFork()) {
                    first = model;
                    break;
                }
            }
            if (first == null) {
                for (GithubSearchModel.GithubRepositoryModel model : val.getItems()) {
                    if (!model.isArchived() && !model.isDisabled()) {
                        first = model;
                        break;
                    }
                }
                if (first == null) {
                    first = val.getItems().get(0);
                }
            }

            GithubLicenseModel license;
            try {
                license = first.getLicense() == null || first.getLicense().getUrl() == null ? null : getLicense(first.getLicense().getUrl()).get();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                license = null;
            } catch (ExecutionException ex2) {
                if (cachedConfig.getDebug()) {
                    logger.error(ex2.getMessage(), ex2);
                } else {
                    logger.error(ex2.getMessage());
                }
                license = null;
            }

            String readme;
            try {
                readme = getReadme(first.getFullName()).get().replace("\r", "").replace("```", "");
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                readme = null;
            } catch (ExecutionException ignored) {
                readme = null;
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor(first.getOwner().getLogin(), String.format(REPO_URL, first.getOwner().getLogin()), first.getOwner().getAvatarUrl());
            embed.setTitle(first.getFullName() + (first.isFork() ? " (\u2442)" : ""), String.format(REPO_URL, first.getFullName()));
            embed.setColor(Color.YELLOW);
            embed.addField("Description", "```" + first.getDescription() + "```", false);
            if (first.isArchived()) {
                embed.addField("\u2757 Status", "ARCHIVED", false);
            } else if (first.isDisabled()) {
                embed.addField("\u2757 Status", "DISABLED", false);
            }
            embed.addField("\u2605 Stars", String.valueOf(first.getStargazers()), false);
            embed.addField("\uD83D\uDC41 Watchers", String.valueOf(first.getWatchers()), true);
            embed.addField("\u2442 Forks", String.valueOf(first.getForks()), false);
            embed.addField("\u2757 Issues", String.valueOf(first.getOpenIssues()), true);
            if (first.getLanguage() != null) {
                embed.addField("Language", first.getLanguage(), false);
            }
            if (first.isWiki() || first.isIssues() || first.getLicense() != null) {
                StringBuilder builder = new StringBuilder();
                if (first.isWiki()) {
                    builder.append("[Wiki](" + String.format(WIKI_URL, first.getFullName()) + ")");
                }
                if (first.isIssues()) {
                    if (first.isWiki()) {
                        builder.append(" \u2014 ");
                    }
                    builder.append("[Issues](" + String.format(ISSUES_URL, first.getFullName()) + ")");
                }
                if (first.getLicense() != null) {
                    if (first.isIssues() || first.isWiki()) {
                        builder.append(" \u2014 ");
                    }
                    String licenseName = license == null ? first.getLicense().getName() + " License" : license.getName();
                    builder.append("[" + licenseName + "](" + (license != null ? license.getHtmlUrl() : String.format(CUSTOM_LICENSE_URL, first.getFullName()) ) + ")");
                }
                if (readme != null) {
                    if (first.isIssues() || first.isWiki() || first.getLicense() != null) {
                        builder.append(" \u2014 ");
                    }
                    builder.append("[Readme](" + String.format(README_URL, first.getFullName()) + ")");
                }
                embed.addField("Links", builder.toString(), false);
            }
            if (readme != null) {
                Map<String, String> links = new HashMap<>();

                readme = RE_IMAGES.matcher(readme).replaceAll("<img>");
                readme = RE_IMAGES_2.matcher(readme).replaceAll("<img>");
                readme = RE_IMAGES_3.matcher(readme).replaceAll("<img>");

                Matcher matcher = RE_LINKS_GROUP.matcher(readme);
                while (matcher.find()) {
                    links.put(matcher.group(1).replace("`", ""), matcher.group(3).replace("`", ""));
                }
                readme = RE_LINKS_GROUP.matcher(readme).replaceAll("<url>");
                matcher = RE_LINKS_GROUP_2.matcher(readme);
                while (matcher.find()) {
                    links.put(matcher.group(1).replace("`", ""), matcher.group(2).replace("`", ""));
                }
                readme = RE_LINKS_GROUP_2.matcher(readme).replaceAll("<url>");
                matcher = RE_LINKS_REPLACE_GROUP.matcher(readme);
                while (matcher.find()) {
                    links.put(matcher.group(1).replace("`", ""), matcher.group(2).replace("`", ""));
                }
                readme = RE_LINKS_REPLACE_GROUP.matcher(readme).replaceAll("<url>");
                matcher = RE_LINKS.matcher(readme);
                while (matcher.find()) {
                    links.put(matcher.group(1).replace("`", ""), matcher.group(1).replace("`", ""));
                }
                readme = RE_LINKS.matcher(readme).replaceAll("<url>");
                matcher = RE_LINKS_2.matcher(readme);
                while (matcher.find()) {
                    links.put(matcher.group(1).replace("`", ""), matcher.group(1).replace("`", ""));
                }
                readme = RE_LINKS_2.matcher(readme).replaceAll("<url>");
                readme = RE_LINKS_REPLACE_GROUP_MATCH.matcher(readme).replaceAll("<url>");
                if (readme.length() > 250) {
                    readme = readme.substring(0, 250) + "...";
                }

                readme = RE_URL_LINE.matcher(readme).replaceAll("");
                readme = RE_MULTIPLE_LINES.matcher(readme).replaceAll("\n\n").trim();

                embed.addField("Readme", "```" + readme + "```", false);

                if (!links.isEmpty()) {
                    StringBuilder builder = new StringBuilder();
                    for (Map.Entry<String, String> kvp : links.entrySet()) {
                        if (kvp.getKey().equals(kvp.getValue())) {
                            if (builder.length() + kvp.getValue().length() > 1024) {
                                continue;
                            }
                            builder.append(kvp.getValue());
                        } else {
                            if (builder.length() + ("[" + kvp.getKey() + "](" + kvp.getValue() + ")").length() > 1024) {
                                continue;
                            }
                            builder.append("[" + kvp.getKey() + "](" + kvp.getValue() + ")");
                        }
                        builder.append(" \u2014 ");
                    }
                    builder.delete(builder.length() - 3, builder.length());
                    embed.addField("Readme Links", builder.toString(), false);
                }
            }
            embed.setFooter("For " + (event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getAsTag()));

            event.getChannel().sendMessage(embed.build()).queue();
        });
    }

    public static @NotNull CompletableFuture<GithubSearchModel> getModel(@NotNull String query) {
        return WebUtil.getUnclosedResponse(String.format(SEARCH_URL, WebUtil.urlEncode(query)), "application/json").thenApplyAsync(response -> {
            try (response) {
                JSONDeserializer<GithubSearchModel> modelDeserializer = new JSONDeserializer<>();
                modelDeserializer.use(Instant.class, new InstantTransformer());
                GithubSearchModel retVal = modelDeserializer.deserialize(response.body().charStream(), GithubSearchModel.class);
                return retVal == null || retVal.getItems().isEmpty() ? null : retVal;
            }
        });
    }

    public static @NotNull CompletableFuture<GithubLicenseModel> getLicense(@NotNull String url) {
        return WebUtil.getUnclosedResponse(url, "application/json").thenApplyAsync(response -> {
            try (response) {
                JSONDeserializer<GithubLicenseModel> modelDeserializer = new JSONDeserializer<>();
                return modelDeserializer.deserialize(response.body().charStream(), GithubLicenseModel.class);
            }
        });
    }

    public static @NotNull CompletableFuture<String> getReadme(@NotNull String repo) { return WebUtil.getString(String.format(RAW_README_URL, repo)); }
}
