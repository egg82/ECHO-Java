package me.egg82.echo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.web.WebConstants;
import me.egg82.echo.web.WebRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandAlias("manchas")
public class ManchasCommand extends BaseCommand {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String CAT_URL = "https://api.manchas.cat/";
    private static final String CAT_URL_ID = "https://api.manchas.cat/%d";

    public ManchasCommand() { }

    @Default
    @Description("{@@description.manchas}")
    @Syntax("[id]")
    public void submit(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @Default("-1") int id) {
        if (event.getAuthor().isBot()) {
            return;
        }

        get(id).whenCompleteAsync((val, ex) -> {
            if (ex != null) {
                if (ConfigUtil.getDebugOrFalse()) {
                    logger.error(ex.getMessage(), ex);
                } else {
                    logger.error(ex.getMessage());
                }
                issuer.sendMessage("An error occurred, sorry :(");
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed.setImage(val);
            embed.setColor(new Color(0x09E214));
            embed.setFooter("For " + event.getAuthor().getAsTag() + " | ID: " + val.substring(val.lastIndexOf('/') + 1));

            event.getChannel().sendMessage(embed.build()).queue();
        });
    }

    public static @NotNull CompletableFuture<String> get(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return WebRequest.builder(new URL(id == -1 ? CAT_URL : String.format(CAT_URL_ID, id)))
                        .timeout(WebConstants.TIMEOUT)
                        .userAgent(WebConstants.USER_AGENT)
                        .build().getConnection().getURL().toExternalForm();
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }
}