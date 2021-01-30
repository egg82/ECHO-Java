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
import me.egg82.echo.lang.Message;
import me.egg82.echo.utils.EmoteUtil;
import me.egg82.echo.utils.RoleUtil;
import me.egg82.echo.utils.WebUtil;
import me.egg82.echo.web.models.GoogleSearchModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandAlias("google")
public class GoogleSearchCommand extends BaseCommand {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SEARCH_URL = "https://www.googleapis.com/customsearch/v1?key=%s&q=%s&safe=ACTIVE";
    private static final int ITEM_LIMIT = 4;

    public GoogleSearchCommand() { }

    @Default
    @Description("{@@description.google}")
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

        if (cachedConfig.getGoogleKey().isEmpty()) {
            logger.error("Google key was not defined.");
            issuer.sendError(Message.ERROR__INTERNAL);
            return;
        }

        if (query.contains("@")) { // TODO: find a better way to do this
            issuer.sendError(Message.ERROR__INTERNAL);
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

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Google search: " + query);
            embed.setColor(new Color(0x0CD7DE));

            int items = 0;
            for (GoogleSearchModel.GoogleSearchItemModel item : val.getItems()) {
                embed.addField(item.getTitle() == null ? "*Title not available*" : item.getTitle(), item.getLink(), false);
                items++;
                if (items >= ITEM_LIMIT) {
                    break;
                }
            }

            embed.setFooter("For " + (event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getAsTag()));

            event.getChannel().sendMessage(embed.build()).queue();
        });
    }

    public static @NotNull CompletableFuture<GoogleSearchModel> getModel(@NotNull String query) {
        return CompletableFuture.supplyAsync(() -> {
            CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
            if (cachedConfig == null) {
                throw new CompletionException(new IllegalStateException("Could not get cached config."));
            }
            if (cachedConfig.getGoogleKey().isEmpty()) {
                throw new CompletionException(new IllegalStateException("Google API key is empty."));
            }

            try {
                Request request = WebUtil.getDefaultRequestBuilder(new URL(String.format(SEARCH_URL, cachedConfig.getGoogleKey(), WebUtil.urlEncode(query.replace("\\s+", "+")))))
                        .header("Accept", "application/json")
                        .build();

                try (Response response = WebUtil.getResponse(request)) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
                    }

                    JSONDeserializer<GoogleSearchModel> modelDeserializer = new JSONDeserializer<>();
                    GoogleSearchModel retVal = modelDeserializer.deserialize(response.body().string(), GoogleSearchModel.class);
                    return retVal == null || retVal.getItems().isEmpty() ? null : retVal;
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }
}
