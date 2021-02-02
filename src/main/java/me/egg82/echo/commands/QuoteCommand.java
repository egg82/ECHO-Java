package me.egg82.echo.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import flexjson.JSONDeserializer;
import java.util.concurrent.CompletableFuture;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.utils.WebUtil;
import me.egg82.echo.web.models.QuoteModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("quote")
public class QuoteCommand extends AbstractCommand {
    private static final String API_URL = "http://api.quotable.io/random";

    public QuoteCommand() { }

    public boolean requiresAdmin() { return false; }

    public @Nullable EmbedBuilder getDescription() { return null; }

    @Default
    @Description("{@@description.quote}")
    @Syntax("")
    public void submit(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event) {
        CachedConfig cachedConfig = getCachedConfig(issuer);
        if (cachedConfig == null || !canRun(event, cachedConfig)) {
            return;
        }

        get().whenCompleteAsync((val, ex) -> {
            if (!canCompleteContinue(issuer, val, ex)) {
                return;
            }

            MessageBuilder message = new MessageBuilder();
            message.append(event.getAuthor());
            message.append(" \"" + val.getContent() + "\" \u2014 *" + val.getAuthor() + "*");

            event.getChannel().sendMessage(message.build()).queue();
        });
    }

    public static @NotNull CompletableFuture<QuoteModel> get() {
        return WebUtil.getUnclosedResponse(API_URL, "application/json").thenApplyAsync(response -> {
            try (response) {
                JSONDeserializer<QuoteModel> modelDeserializer = new JSONDeserializer<>();
                return modelDeserializer.deserialize(response.body().charStream(), QuoteModel.class);
            }
        });
    }
}
