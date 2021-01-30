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
import me.egg82.echo.utils.WebUtil;
import me.egg82.echo.web.models.RoryModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandAlias("rory")
public class RoryCommand extends BaseCommand {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String CAT_URL = "https://rory.cat/purr/";
    private static final String CAT_URL_ID = "https://rory.cat/purr/%d";

    @Default
    @Description("{@@description.rory}")
    @Syntax("[id]")
    public void submit(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @Default("-1") int id) {
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

        get(id).whenCompleteAsync((val, ex) -> {
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
            embed.setImage(val.getUrl());
            embed.setColor(new Color(0x09E214));
            embed.setFooter("For " + (event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getAsTag()) + " | ID: " + val.getId());

            event.getChannel().sendMessage(embed.build()).queue();
        });
    }

    public static @NotNull CompletableFuture<RoryModel> get(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Request request = WebUtil.getDefaultRequestBuilder(new URL(id == -1 ? CAT_URL : String.format(CAT_URL_ID, id)))
                        .header("Accept", "application/json")
                        .build();

                try (Response response = WebUtil.getResponse(request)) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
                    }

                    JSONDeserializer<RoryModel> modelDeserializer = new JSONDeserializer<>();
                    RoryModel retVal = modelDeserializer.deserialize(response.body().string(), RoryModel.class);
                    return retVal == null || retVal.getId() == -1 ? null : retVal;
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }
}
