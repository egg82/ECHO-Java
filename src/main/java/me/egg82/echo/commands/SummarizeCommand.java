package me.egg82.echo.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import flexjson.JSONDeserializer;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.core.Pair;
import me.egg82.echo.lang.Message;
import me.egg82.echo.utils.DatabaseUtil;
import me.egg82.echo.utils.ExceptionUtil;
import me.egg82.echo.utils.TimeUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandAlias("summarize|summary")
public class SummarizeCommand extends AbstractCommand {
    private static final Logger logger = LoggerFactory.getLogger(SummarizeCommand.class);

    private static final String EXTRACT_URL = "https://extractorapi.com/api/v1/extractor/?apikey=%s&url=%s";
    private static final String SUMMARIZE_URL = "https://api.deepai.org/api/summarization";

    private static final Pattern RE_DOT_PATTERN = Pattern.compile("\\.\\s*");
    private static final Pattern RE_URL_PATTERN = Pattern.compile("(https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*))");
    private static final Pattern RE_VERSION_PATTERN = Pattern.compile("\\b(\\d+\\.\\d+(?:[\\.\\d]*))\\b");
    private static final Pattern RE_DOT_PATTERN_2 = Pattern.compile("([\\.?!])+\\s*([^\\)])");

    private static final StanfordCoreNLP sentenceNlp = new StanfordCoreNLP(PropertiesUtils.asProperties(
            "annotators", "tokenize,ssplit"
    ));

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
                            CoreDocument doc = new CoreDocument(val.getT2().getOutput());
                            sentenceNlp.annotate(doc);

                            List<String> messages = new ArrayList<>();
                            StringBuilder current = new StringBuilder();
                            for (CoreSentence sentence : doc.sentences()) {
                                String s = sentence.text();
                                if (current.length() > 0 && current.length() + s.length() > 1500) {
                                    current.deleteCharAt(current.length() - 1);
                                    messages.add(current.toString());
                                    current = new StringBuilder();
                                }
                                current.append(s);
                                current.append(' ');
                            }
                            if (current.length() > 0) {
                                current.deleteCharAt(current.length() - 1);
                                messages.add(current.toString());
                            }

                            for (int i = 0; i < messages.size(); i++) {
                                MessageBuilder message = new MessageBuilder();
                                if (i == 0) {
                                    message.append(event.getAuthor());
                                    message.append(" \u2014 ");
                                }
                                message.append("*" + val.getT1().getTitle() + "*");
                                message.append('\n');
                                message.append("```");
                                message.append(messages.get(i));
                                message.append("```");

                                event.getChannel().sendMessage(message.build()).queue();
                            }
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
                            CoreDocument doc = new CoreDocument(val.getOutput());
                            sentenceNlp.annotate(doc);

                            List<String> messages = new ArrayList<>();
                            StringBuilder current = new StringBuilder();
                            for (CoreSentence sentence : doc.sentences()) {
                                String s = sentence.text();
                                if (current.length() > 0 && current.length() + s.length() > 1500) {
                                    current.deleteCharAt(current.length() - 1);
                                    messages.add(current.toString());
                                    current = new StringBuilder();
                                }
                                current.append(s);
                                current.append(' ');
                            }
                            if (current.length() > 0) {
                                current.deleteCharAt(current.length() - 1);
                                messages.add(current.toString());
                            }

                            for (int i = 0; i < messages.size(); i++) {
                                MessageBuilder message = new MessageBuilder();
                                if (i == 0) {
                                    message.append(event.getAuthor());
                                    message.append('\n');
                                }
                                message.append("```");
                                message.append(messages.get(i));
                                message.append("```");

                                event.getChannel().sendMessage(message.build()).queue();
                            }
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
            .expireAfterAccess(4L, TimeUnit.HOURS)
            .build();

    private static final long EXTRACTOR_CACHE_TIME = new TimeUtil.Time(7L, TimeUnit.DAYS).getMillis();

    public static @NotNull CompletableFuture<ExtractionModel> getExtractionModel(@NotNull String key, @NotNull String url) {
        return CompletableFuture.supplyAsync(() -> extractionCache.get(DatabaseUtil.sha512(url), k -> {
            ExtractionModel retVal = DatabaseUtil.getModel(k, "extractor", ExtractionModel.class, EXTRACTOR_CACHE_TIME);
            if (retVal != null && "COMPLETE".equalsIgnoreCase(retVal.getStatus())) {
                return retVal;
            }

            return WebUtil.getUnclosedResponse(String.format(EXTRACT_URL, key, url), "application/json").thenApplyAsync(response -> {
                try (response) {
                    JSONDeserializer<ExtractionModel> modelDeserializer = new JSONDeserializer<>();
                    ExtractionModel r = modelDeserializer.deserialize(response.body().charStream(), ExtractionModel.class);
                    if (r != null && "COMPLETE".equalsIgnoreCase(r.getStatus())) {
                        DatabaseUtil.storeModel(k, "extractor", r);
                    }
                    return r == null || !"COMPLETE".equalsIgnoreCase(r.getStatus()) ? null : r;
                }
            }).join();
        })).exceptionally(ex -> ExceptionUtil.handleException(ex, logger));
    }

    private static final Cache<String, SummaryModel> summaryCache = Caffeine.newBuilder()
            .expireAfterWrite(1L, TimeUnit.DAYS)
            .expireAfterAccess(4L, TimeUnit.HOURS)
            .build();

    public static @NotNull CompletableFuture<SummaryModel> getSummaryModel(@NotNull String key, @NotNull String url) {
        System.out.println("Getting summary for " + url);

        return CompletableFuture.supplyAsync(() -> summaryCache.get(DatabaseUtil.sha512(url), k -> {
            try {
                SummaryModel retVal = DatabaseUtil.getModel(k, "summarizer", SummaryModel.class);
                if (retVal != null) {
                    return retVal;
                }

                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("text", url)
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
                    retVal = modelDeserializer.deserialize(response.body().charStream(), SummaryModel.class);
                    System.out.println("Summary JSON: " + retVal);
                    if (retVal != null) {
                        DatabaseUtil.storeModel(k, "summarizer", retVal);
                    }
                    return retVal;
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        })).exceptionally(ex -> ExceptionUtil.handleException(ex, logger));
    }
}
