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
import me.egg82.echo.web.WebConstants;
import me.egg82.echo.web.WebRequest;
import me.egg82.echo.web.models.GoogleSearchModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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

        if (query.contains("@")) { // TODO: find a better way to do this
            issuer.sendMessage("An error occurred, sorry :(");
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

            embed.setFooter("For " + event.getAuthor().getAsTag());

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
                String content = WebRequest.builder(new URL(String.format(SEARCH_URL, cachedConfig.getGoogleKey(), WebRequest.urlEncode(query))))
                        .timeout(WebConstants.TIMEOUT)
                        .userAgent(WebConstants.USER_AGENT)
                        .header("Accept", "application/json")
                        .build().getString();

                JSONDeserializer<GoogleSearchModel> modelDeserializer = new JSONDeserializer<>();
                GoogleSearchModel retVal = modelDeserializer.deserialize(content, GoogleSearchModel.class);
                return retVal == null || retVal.getItems().isEmpty() ? null : retVal;
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }
}
