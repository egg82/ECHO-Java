package me.egg82.echo.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import flexjson.JSONDeserializer;
import java.awt.*;
import java.util.concurrent.CompletableFuture;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.lang.Message;
import me.egg82.echo.utils.WebUtil;
import me.egg82.echo.web.models.GoogleSearchModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

@CommandAlias("google")
public class GoogleSearchCommand extends AbstractCommand {
    private static final String SEARCH_URL = "https://www.googleapis.com/customsearch/v1?key=%s&q=%s&safe=ACTIVE";
    private static final int ITEM_LIMIT = 4;

    public GoogleSearchCommand() { }

    @Default
    @Description("{@@description.google}")
    @Syntax("<search>")
    public void submit(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull String query) {
        CachedConfig cachedConfig = getCachedConfig(issuer);
        if (cachedConfig == null || !canRun(event, cachedConfig) || queryMentionsUsers(issuer, query)) {
            return;
        }

        if (cachedConfig.getGoogleKey().isEmpty()) {
            logger.error("Google key was not defined.");
            issuer.sendError(Message.ERROR__INTERNAL);
            return;
        }

        getModel(cachedConfig.getGoogleKey(), query).whenCompleteAsync((val, ex) -> {
            if (!canCompleteContinue(issuer, val, ex)) {
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

    public static @NotNull CompletableFuture<GoogleSearchModel> getModel(@NotNull String key, @NotNull String query) {
        return WebUtil.getReader(String.format(SEARCH_URL, key, WebUtil.urlEncode(query.replace("\\s+", "+")))).thenApplyAsync(stream -> {
            JSONDeserializer<GoogleSearchModel> modelDeserializer = new JSONDeserializer<>();
            GoogleSearchModel retVal = modelDeserializer.deserialize(stream, GoogleSearchModel.class);
            return retVal == null || retVal.getItems().isEmpty() ? null : retVal;
        });
    }
}
