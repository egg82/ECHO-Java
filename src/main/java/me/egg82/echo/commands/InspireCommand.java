package me.egg82.echo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandAlias("inspire|inspireme")
public class InspireCommand extends BaseCommand {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String API_URL = "https://inspirobot.me/api?generate=true";

    public InspireCommand() { }

    @Default
    @Description("{@@description.inspire}")
    public void submit(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event) {
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

        get().whenCompleteAsync((val, ex) -> {
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
            embed.setTitle("Inspiration");
            embed.setColor(new Color(0x09E214));
            embed.setImage(val);
            embed.setFooter("For " + (event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getAsTag()));

            event.getChannel().sendMessage(embed.build()).queue();
        });
    }

    public static @NotNull CompletableFuture<String> get() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return WebRequest.builder(new URL(API_URL))
                        .timeout(WebConstants.TIMEOUT)
                        .userAgent(WebConstants.USER_AGENT)
                        .header("Accept", "text/plain")
                        .build().getString();
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }
}
