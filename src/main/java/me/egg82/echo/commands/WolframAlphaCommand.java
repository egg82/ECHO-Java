package me.egg82.echo.commands;

import co.aikar.commands.BaseCommand;
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
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.core.Pair;
import me.egg82.echo.lang.Message;
import me.egg82.echo.utils.EmoteUtil;
import me.egg82.echo.utils.RoleUtil;
import me.egg82.echo.utils.WebUtil;
import me.egg82.echo.web.models.ImgurUploadModel;
import me.egg82.echo.web.transformers.InstantTransformer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandAlias("wolfram|wa")
public class WolframAlphaCommand extends BaseCommand {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String RESULT_URL = "https://api.wolframalpha.com/v1/result?appid=%s&i=%s";
    private static final String IMAGE_URL = "https://api.wolframalpha.com/v1/simple?appid=%s&i=%s";
    private static final String QUERY_LINK = "https://www.wolframalpha.com/input/?i=%s";

    private static final String IMGUR_URL = "https://api.imgur.com/3/image";

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

        if (event.getMember() != null && !RoleUtil.isAllowed(event.getMember())) {
            Emote emote = EmoteUtil.getEmote(cachedConfig.getDisallowedEmote(), event.getGuild());
            if (emote == null) {
                logger.warn("Could not find disallowed emote \"" + cachedConfig.getAlotEmote() + "\" for guild \"" + event.getGuild().getName() + "\".");
                return;
            }
            event.getMessage().addReaction(emote).queue();
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
                    if (v.getT2() == null) {
                        return new Pair<>(v.getT1(), "");
                    }
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

                    if (val == null || val.getT1() == null) {
                        issuer.sendError(Message.ERROR__INTERNAL);
                        return;
                    }

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("Wolfram Alpha query: " + query, String.format(QUERY_LINK, WebUtil.urlEncode(query)));
                    embed.setColor(Color.GREEN);
                    embed.appendDescription("Answer");
                    embed.appendDescription(String.format("```%s```", val.getT1()));
                    embed.setImage(val.getT2());
                    embed.setFooter("For " + (event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getAsTag()));

                    event.getChannel().sendMessage(embed.build()).queue();
                });
    }

    private static final Cache<String, String> resultCache = Caffeine.newBuilder().build();

    public static @NotNull CompletableFuture<String> getResult(@NotNull String key, @NotNull String query) {
        return CompletableFuture.supplyAsync(() -> resultCache.get(query, q -> {
            try {
                Request request = WebUtil.getDefaultRequestBuilder(new URL(String.format(RESULT_URL, key, WebUtil.urlEncode(q))))
                        .build();

                try (Response response = WebUtil.getResponse(request)) {
                    if (!response.isSuccessful()) {
                        if (response.code() == 501) {
                            return "No short answer available";
                        }
                        throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
                    }

                    return response.body().string();
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        }));
    }

    private static final Cache<String, byte[]> imageCache = Caffeine.newBuilder().build();

    public static @NotNull CompletableFuture<byte[]> getImage(@NotNull String key, @NotNull String query) {
        return CompletableFuture.supplyAsync(() -> imageCache.get(query, q -> {
            try {
                Request request = WebUtil.getDefaultRequestBuilder(new URL(String.format(IMAGE_URL, key, WebUtil.urlEncode(q))))
                        .build();

                try (Response response = WebUtil.getResponse(request)) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
                    }

                    return response.body().bytes();
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        }));
    }

    private static final Cache<byte[], ImgurUploadModel> imgurCache = Caffeine.newBuilder().build();

    public static @NotNull CompletableFuture<ImgurUploadModel> uploadImage(@NotNull String key, byte @NotNull [] data) {
        return CompletableFuture.supplyAsync(() -> imgurCache.get(data, d -> {
            try {
                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("title", "Wolfram Result")
                        .addFormDataPart("image", "wolfram.gif", RequestBody.create(d, MediaType.get("image/gif")))
                        .build();

                Request request = WebUtil.getDefaultRequestBuilder(new URL(IMGUR_URL))
                        .header("Accept", "application/json")
                        .header("Authorization", "Client-ID " + key)
                        .post(body)
                        .build();

                try (Response response = WebUtil.getResponse(request)) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
                    }

                    JSONDeserializer<ImgurUploadModel> modelDeserializer = new JSONDeserializer<>();
                    modelDeserializer.use(Instant.class, new InstantTransformer());
                    ImgurUploadModel retVal = modelDeserializer.deserialize(response.body().charStream(), ImgurUploadModel.class);
                    return retVal == null || !retVal.isSuccess() ? null : retVal;
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        }));
    }
}
