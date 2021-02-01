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
import me.egg82.echo.web.models.EightBallModel;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

@CommandAlias("8ball")
public class EightBallCommand extends AbstractCommand {
    private static final String API_URL = "https://8ball.delegator.com/magic/JSON/%s";

    public EightBallCommand() { }

    public boolean requiresAdmin() { return false; }

    @Default
    @Description("{@@description.eight-ball}")
    @Syntax("<phrase>")
    public void submit(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull String phrase) {
        CachedConfig cachedConfig = getCachedConfig(issuer);
        if (cachedConfig == null || !canRun(event, cachedConfig)) {
            return;
        }

        getModel(phrase).whenCompleteAsync((val, ex) -> {
            if (!canCompleteContinue(issuer, val, ex)) {
                return;
            }

            MessageBuilder message = new MessageBuilder();
            message.append(event.getAuthor());
            message.append(" ");
            message.append(val.getMagic().getAnswer());

            event.getChannel().sendMessage(message.build()).queue();
        });
    }

    public static @NotNull CompletableFuture<EightBallModel> getModel(@NotNull String phrase) {
        return WebUtil.getUnclosedResponse(String.format(API_URL, phrase)).thenApplyAsync(response -> {
            try (response) {
                JSONDeserializer<EightBallModel> modelDeserializer = new JSONDeserializer<>();
                return modelDeserializer.deserialize(response.body().charStream(), EightBallModel.class);
            }
        });
    }
}
