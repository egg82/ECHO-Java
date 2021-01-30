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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.core.Pair;
import me.egg82.echo.lang.Message;
import me.egg82.echo.web.WebConstants;
import me.egg82.echo.web.WebRequest;
import me.egg82.echo.web.models.ImgurUploadModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandAlias("wa|wolfram")
public class WolframAlphaCommand extends BaseCommand {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String RESULT_URL = "https://api.wolframalpha.com/v1/result?appid=%s&i=%s";
    private static final String IMAGE_URL = "https://api.wolframalpha.com/v1/simple?appid=%s&i=%s";
    private static final String QUERY_LINK = "https://www.wolframalpha.com/input/?i=%s";

    private static final String IMGUR_URL = "https://api.imgur.com/3/upload";

    private static final OkHttpClient client = new OkHttpClient();

    @Default
    @Description("{@@description.wolfram}")
    @Syntax("<query>")
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

        if (cachedConfig.getWolframKey().isEmpty()) {
            logger.error("Wolfram Alpha key was not defined.");
            issuer.sendError(Message.ERROR__INTERNAL);
            return;
        }

        if (cachedConfig.getImgurKey().isEmpty()) {
            logger.error("Imgur key was not defined.");
            issuer.sendError(Message.ERROR__INTERNAL);
            return;
        }

        if (query.contains("@")) { // TODO: find a better way to do this
            issuer.sendError(Message.ERROR__INTERNAL);
            return;
        }

        getResult(cachedConfig.getWolframKey(), query)
                .thenCombineAsync(getImage(cachedConfig.getWolframKey(), query), Pair::new)
                .thenApplyAsync(v -> {
                    ImgurUploadModel model = uploadImage(cachedConfig.getImgurKey(), v.getT2()).join();
                    return new Pair<>(v.getT1(), model.getData().getLink());
                })
                .whenCompleteAsync((val, ex) -> {
                    if (ex != null) {
                        if (ConfigUtil.getDebugOrFalse()) {
                            logger.error(ex.getMessage(), ex);
                        } else {
                            logger.error(ex.getMessage());
                        }
                        issuer.sendError(Message.ERROR__INTERNAL);
                        return;
                    }

                    if (val == null || val.getT1() == null || val.getT2() == null) {
                        issuer.sendError(Message.ERROR__INTERNAL);
                        return;
                    }

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("Wolfram Alpha query: " + query, String.format(QUERY_LINK, WebRequest.urlEncode(query.replace("\\s+", "+"))));
                    embed.setColor(Color.GREEN);
                    embed.appendDescription("Answer");
                    embed.appendDescription(String.format("```%s```", val.getT1()));
                    embed.setImage(val.getT2());
                    embed.setFooter("For " + (event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getAsTag()));

                    event.getChannel().sendMessage(embed.build()).queue();
                });
    }

    public static @NotNull CompletableFuture<String> getResult(@NotNull String key, @NotNull String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return WebRequest.builder(new URL(String.format(RESULT_URL, key, WebRequest.urlEncode(query.replace("\\s+", "+")))))
                        .timeout(WebConstants.TIMEOUT)
                        .userAgent(WebConstants.USER_AGENT)
                        .build().getString();
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }

    public static @NotNull CompletableFuture<byte[]> getImage(@NotNull String key, @NotNull String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return WebRequest.builder(new URL(String.format(IMAGE_URL, key, WebRequest.urlEncode(query.replace("\\s+", "+")))))
                        .timeout(WebConstants.TIMEOUT)
                        .userAgent(WebConstants.USER_AGENT)
                        .build().getBytes();
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }

    public static @NotNull CompletableFuture<ImgurUploadModel> uploadImage(@NotNull String key, @NotNull byte[] data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("title", "Wolfram Result")
                        .addFormDataPart("image", "wolfram.gif", RequestBody.create(data, MediaType.get("image/gif")))
                        .build();

                Request request = new Request.Builder()
                        .header("Accept", "application/json")
                        .header("Authorization", "Bearer " + key)
                        .url(new URL(IMGUR_URL))
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
                    }

                    JSONDeserializer<ImgurUploadModel> modelDeserializer = new JSONDeserializer<>();
                    ImgurUploadModel retVal = modelDeserializer.deserialize(response.body().string(), ImgurUploadModel.class);
                    return retVal == null || !retVal.isSuccess() ? null : retVal;
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }
}
