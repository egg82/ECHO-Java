package me.egg82.echo.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import flexjson.JSONDeserializer;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.core.Pair;
import me.egg82.echo.lang.Message;
import me.egg82.echo.utils.WebUtil;
import me.egg82.echo.web.models.ExtractionModel;
import me.egg82.echo.web.models.SummaryModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("summarize|summary")
public class SummarizeCommand extends AbstractCommand {
    private static final String EXTRACT_URL = "https://extractorapi.com/api/v1/extractor/?apikey=%s&url=%s";
    private static final String SUMMARIZE_URL = "https://api.deepai.org/api/summarization";

    private static final Pattern RE_DOT_PATTERN = Pattern.compile("\\.\\s*");
    private static final Pattern RE_URL_PATTERN = Pattern.compile("(https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*))");
    private static final Pattern RE_VERSION_PATTERN = Pattern.compile("\\b(\\d+\\.\\d+(?:[\\.\\d]*))\\b");
    private static final Pattern RE_DOT_PATTERN_2 = Pattern.compile("([\\.?!])+\\s*([^\\)])");

    public SummarizeCommand() { }

    public boolean requiresAdmin() { return false; }

    public @Nullable EmbedBuilder getDescription() { return null; }

    @Default
    @Description("{@@description.summarize}")
    @Syntax("<text|url>")
    public void submit(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull String text) {
        CachedConfig cachedConfig = getCachedConfig(issuer);
        if (cachedConfig == null || !canRun(event, cachedConfig)) {
            return;
        }

        if (cachedConfig.getDeepAiKey().isEmpty()) {
            logger.error("DeepAI key was not defined.");
            issuer.sendError(Message.ERROR__INTERNAL);
            return;
        }

        text = text.trim();
        String cleaned = cleanUrl(text);
        boolean doImage = !cleaned.equals(text);
        text = cleaned;

        if (isUrl(text)) {
            if (cachedConfig.getExtractorKey().isEmpty()) {
                logger.error("Extractor API key was not defined.");
                issuer.sendError(Message.ERROR__INTERNAL);
                return;
            }

            getExtractionModel(cachedConfig.getExtractorKey(), text)
                    .thenApplyAsync(v -> {
                        if (v == null) {
                            return null;
                        }
                        return new Pair<>(v, getSummaryModel(cachedConfig.getDeepAiKey(), WebUtil.uploadBytebinContent(cleanText(v.getText()).getBytes(StandardCharsets.UTF_8)).join()).join());
                    })
                    .whenCompleteAsync((val, ex) -> {
                        if (!canCompleteContinue(issuer, val, ex)) {
                            return;
                        }

                        if (val.getT2().getOutput().isEmpty()) {
                            logger.warn("Summary gave no output.");
                            issuer.sendError(Message.ERROR__INTERNAL);
                            return;
                        }

                        if (val.getT2().getOutput().length() <= 700) {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setTitle(val.getT1().getTitle(), val.getT1().getUrl());
                            embed.setColor(new Color(0x0CD7DE));
                            embed.appendDescription("```" + val.getT2().getOutput() + "```");
                            if (doImage && !val.getT1().getImages().isEmpty()) {
                                embed.setImage(val.getT1().getImages().get(0));
                            }
                            embed.setFooter("For " + (event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getAsTag()));

                            event.getChannel().sendMessage(embed.build()).queue();
                        } else {
                            MessageBuilder message = new MessageBuilder();
                            message.append(event.getAuthor());
                            message.append(" \u2014 ");
                            message.append("*" + val.getT1().getTitle() + "*");
                            message.append('\n');
                            message.append("```");
                            if (val.getT2().getOutput().length() > 1500) {
                                message.append(val.getT2().getOutput().substring(0, 1500) + "...");
                            } else {
                                message.append(val.getT2().getOutput());
                            }
                            message.append("```");

                            event.getChannel().sendMessage(message.build()).queue();
                        }
                    });
        } else {
            WebUtil.uploadBytebinContent(text.getBytes(StandardCharsets.UTF_8))
                    .thenComposeAsync(v -> getSummaryModel(cachedConfig.getDeepAiKey(), v))
                    .whenCompleteAsync((val, ex) -> {
                        if (!canCompleteContinue(issuer, val, ex)) {
                            return;
                        }

                        if (val.getOutput().isEmpty()) {
                            logger.warn("Summary gave no output.");
                            issuer.sendError(Message.ERROR__INTERNAL);
                            return;
                        }

                        if (val.getOutput().length() <= 700) {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setTitle("Text Summary");
                            embed.setColor(new Color(0x0CD7DE));
                            embed.appendDescription("```" + val.getOutput() + "```");
                            embed.setFooter("For " + (event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getAsTag()));

                            event.getChannel().sendMessage(embed.build()).queue();
                        } else {
                            MessageBuilder message = new MessageBuilder();
                            message.append(event.getAuthor());
                            message.append('\n');
                            message.append("```");
                            if (val.getOutput().length() > 1500) {
                                message.append(val.getOutput().substring(0, 1500) + "...");
                            } else {
                                message.append(val.getOutput());
                            }
                            message.append("```");

                            event.getChannel().sendMessage(message.build()).queue();
                        }
                    });
        }
    }

    private @NotNull String cleanUrl(@NotNull String text) {
        if (text.charAt(0) == '<' && text.charAt(text.length() - 1) == '>') {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    private boolean isUrl(@NotNull String text) {
        try {
            new URL(text).toURI();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private @NotNull String cleanText(@NotNull String text) {
        System.out.println("Dirty: " + text);

        Map<String, String> replacements = new HashMap<>();
        text = RE_DOT_PATTERN.matcher(text).replaceAll(".");

        int current = 0;
        Matcher matcher = RE_URL_PATTERN.matcher(text);
        while (matcher.find()) {
            replacements.put("<$$" + current + ">", matcher.group(1));
            text = matcher.replaceFirst(Matcher.quoteReplacement("<$$" + current + ">"));
            matcher = RE_URL_PATTERN.matcher(text);
            current++;
        }

        matcher = RE_VERSION_PATTERN.matcher(text);
        while (matcher.find()) {
            replacements.put("<$$" + current + ">", matcher.group(1));
            text = matcher.replaceFirst(Matcher.quoteReplacement("<$$" + current + ">"));
            matcher = RE_VERSION_PATTERN.matcher(text);
            current++;
        }

        text = RE_DOT_PATTERN_2.matcher(text).replaceAll("$1 $2");

        for (Map.Entry<String, String> kvp : replacements.entrySet()) {
            text = text.replaceAll(Pattern.quote(kvp.getKey()), Matcher.quoteReplacement(kvp.getValue()));
        }

        System.out.println("Clean: " + text);

        return text;
    }

    private static final Cache<String, ExtractionModel> extractionCache = Caffeine.newBuilder()
            .expireAfterWrite(1L, TimeUnit.DAYS)
            .expireAfterAccess(12L, TimeUnit.HOURS)
            .build();

    public static @NotNull CompletableFuture<ExtractionModel> getExtractionModel(@NotNull String key, @NotNull String url) {
        return CompletableFuture.supplyAsync(() -> extractionCache.get(url, u -> WebUtil.getUnclosedResponse(String.format(EXTRACT_URL, key, u), "application/json").thenApplyAsync(response -> {
            try (response) {
                JSONDeserializer<ExtractionModel> modelDeserializer = new JSONDeserializer<>();
                ExtractionModel retVal = modelDeserializer.deserialize(response.body().charStream(), ExtractionModel.class);
                return retVal == null || !"COMPLETE".equalsIgnoreCase(retVal.getStatus()) ? null : retVal;
            }
        }).join()));
    }

    private static final Cache<String, SummaryModel> summaryCache = Caffeine.newBuilder()
            .expireAfterWrite(7L, TimeUnit.DAYS)
            .expireAfterAccess(1L, TimeUnit.DAYS)
            .build();

    public static @NotNull CompletableFuture<SummaryModel> getSummaryModel(@NotNull String key, @NotNull String url) {
        System.out.println("Getting summary for " + url);

        return CompletableFuture.supplyAsync(() -> summaryCache.get(url, u -> {
            try {
                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("text", u)
                        .build();

                Request request = WebUtil.getDefaultRequestBuilder(new URL(SUMMARIZE_URL))
                        .header("Accept", "application/json")
                        .header("api-key", key)
                        .post(body)
                        .build();

                System.out.println("Summary request: " + request.toString());

                try (Response response = WebUtil.getResponse(request)) {
                    System.out.println("Summary response: " + response.code());

                    if (!response.isSuccessful()) {
                        throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
                    }

                    JSONDeserializer<SummaryModel> modelDeserializer = new JSONDeserializer<>();
                    SummaryModel retVal = modelDeserializer.deserialize(response.body().charStream(), SummaryModel.class);
                    System.out.println("Summary JSON: " + retVal);
                    return retVal;
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        }));
    }
}
