package me.egg82.echo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import flexjson.JSONDeserializer;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.lang.Message;
import me.egg82.echo.utils.EmoteUtil;
import me.egg82.echo.utils.RoleUtil;
import me.egg82.echo.utils.WebUtil;
import me.egg82.echo.web.models.GithubLicenseModel;
import me.egg82.echo.web.models.GithubSearchModel;
import me.egg82.echo.web.transformers.InstantTransformer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandAlias("github|gh")
public class GithubCommand extends BaseCommand {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SEARCH_URL = "https://api.github.com/search/repositories?q=%s";
    private static final String REPO_URL = "https://github.com/%s";
    private static final String WIKI_URL = "https://github.com/%s/wiki";
    private static final String ISSUES_URL = "https://github.com/%s/issues";
    private static final String CUSTOM_LICENSE_URL = "https://github.com/%s/blob/master/LICENSE.md";
    private static final String README_URL = "https://github.com/%s/blob/master/README.md";
    private static final String RAW_README_URL = "https://raw.githubusercontent.com/%s/master/README.md";

    private static final Pattern RE_LINKS_GROUP = Pattern.compile("\\[\\!\\[(.+)\\]\\s*\\((https?:\\/\\/.*)\\)\\]\\s*\\((https?:\\/\\/.*)\\)");
    private static final Pattern RE_LINKS_GROUP_2 = Pattern.compile("\\[+(.+)\\]+\\s*\\(+(https?:\\/\\/.*)\\)+");
    private static final Pattern RE_IMAGES = Pattern.compile("\\!\\[+(.*)\\]+\\s*\\(+(https?:\\/\\/.*)\\)+");
    private static final Pattern RE_LINKS = Pattern.compile("(https?:\\/\\/.*)");
    private static final Pattern RE_URL_LINE = Pattern.compile("^<(?:url|img)>$", Pattern.MULTILINE);
    private static final Pattern RE_MULTIPLE_LINES = Pattern.compile("\n{3,}");

    public GithubCommand() { }

    @Default
    @Description("{@@description.github}")
    @Syntax("<search>")
    public void submit(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull String query) {
        if (event.getAuthor().isBot()) {
            return;
        }

        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            issuer.sendError(Message.ERROR__INTERNAL);
            return;
        }

        if (cachedConfig.getDisabledCommands().contains(getName())) {
            return;
        }

        if (event.getMember() != null && !RoleUtil.isAllowed(event.getMember())) {
            Emote emote = EmoteUtil.getEmote(cachedConfig.getDisallowedEmote(), event.getGuild());
            if (emote == null) {
                logger.warn("Could not find disallowed emote \"" + cachedConfig.getAlotEmote() + "\" for guild \"" + event.getGuild().getName() + "\".");
                return;
            }
            event.getMessage().addReaction(emote).queue();
            return;
        }

        getModel(query).whenCompleteAsync((val, ex) -> {
            if (ex != null) {
                if (ConfigUtil.getDebugOrFalse()) {
                    logger.error(ex.getMessage(), ex);
                } else {
                    logger.error(ex.getMessage());
                }
                issuer.sendError(Message.ERROR__INTERNAL);
                return;
            }

            if (val == null) {
                issuer.sendError(Message.ERROR__INTERNAL);
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
                embed.addField("\u2757 Status", "ARCHIVED", true);
            } else if (first.isDisabled()) {
                embed.addField("\u2757 Status", "DISABLED", true);
            }
            embed.addField("\u2605 Stars", String.valueOf(first.getStargazers()), true);
            embed.addField("\uD83D\uDC41 Watchers", String.valueOf(first.getWatchers()), true);
            embed.addField("\u2442 Forks", String.valueOf(first.getForks()), true);
            embed.addField("\u2757 Issues", String.valueOf(first.getOpenIssues()), true);
            if (first.getLanguage() != null) {
                embed.addField("Language", first.getLanguage(), true);
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
                Matcher matcher = RE_LINKS_GROUP.matcher(readme);
                while (matcher.find()) {
                    links.put(matcher.group(1).replace("`", ""), matcher.group(3));
                }
                readme = RE_LINKS_GROUP.matcher(readme).replaceAll("<url>");
                matcher = RE_LINKS_GROUP_2.matcher(readme);
                while (matcher.find()) {
                    links.put(matcher.group(1).replace("`", ""), matcher.group(2));
                }
                readme = RE_LINKS_GROUP_2.matcher(readme).replaceAll("<url>");
                readme = RE_IMAGES.matcher(readme).replaceAll("<img>");
                matcher = RE_LINKS.matcher(readme);
                while (matcher.find()) {
                    links.put(matcher.group(1), matcher.group(1));
                }
                readme = RE_LINKS.matcher(readme).replaceAll("<url>");
                if (readme.length() > 500) {
                    readme = readme.substring(0, 500) + "...";
                }

                readme = RE_URL_LINE.matcher(readme).replaceAll("");
                readme = RE_MULTIPLE_LINES.matcher(readme).replaceAll("\n\n").trim();

                embed.addField("Readme", "```" + readme + "```", false);

                if (!links.isEmpty()) {
                    StringBuilder builder = new StringBuilder();
                    for (Map.Entry<String, String> kvp : links.entrySet()) {
                        if (kvp.getKey().equals(kvp.getValue())) {
                            builder.append(kvp.getValue());
                        } else {
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
        return CompletableFuture.supplyAsync(() -> {
            try {
                Request request = WebUtil.getDefaultRequestBuilder(new URL(String.format(SEARCH_URL, WebUtil.urlEncode(query))))
                        .header("Accept", "application/json")
                        .build();

                try (Response response = WebUtil.getResponse(request)) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
                    }

                    JSONDeserializer<GithubSearchModel> modelDeserializer = new JSONDeserializer<>();
                    modelDeserializer.use(Instant.class, new InstantTransformer());
                    GithubSearchModel retVal = modelDeserializer.deserialize(response.body().charStream(), GithubSearchModel.class);
                    return retVal == null || retVal.getItems().isEmpty() ? null : retVal;
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }

    public static @NotNull CompletableFuture<GithubLicenseModel> getLicense(@NotNull String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Request request = WebUtil.getDefaultRequestBuilder(new URL(url))
                        .header("Accept", "application/json")
                        .build();

                try (Response response = WebUtil.getResponse(request)) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
                    }

                    JSONDeserializer<GithubLicenseModel> modelDeserializer = new JSONDeserializer<>();
                    return modelDeserializer.deserialize(response.body().charStream(), GithubLicenseModel.class);
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }

    public static @NotNull CompletableFuture<String> getReadme(@NotNull String repo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Request request = WebUtil.getDefaultRequestBuilder(new URL(String.format(RAW_README_URL, repo)))
                        .header("Accept", "text/plain")
                        .build();

                try (Response response = WebUtil.getResponse(request)) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
                    }

                    return response.body().string();
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }
}
