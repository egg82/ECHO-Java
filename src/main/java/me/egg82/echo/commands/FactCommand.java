package me.egg82.echo.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import flexjson.JSONDeserializer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.lang.Message;
import me.egg82.echo.utils.WebUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("fact|facts|factoid|factoids|useless")
public class FactCommand extends AbstractCommand {
    private static final List<String> API_URLS = Arrays.asList(
            "https://uselessfacts.jsph.pl/random.json?language=en",
            "https://useless-facts.sameerkumar.website/api"
    );

    private static final Random random = new Random();

    public FactCommand() { }

    public boolean requiresAdmin() { return false; }

    public @Nullable EmbedBuilder getDescription() { return null; }

    @Default
    @Description("{@@description.fact}")
    @Syntax("")
    public void submit(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event) {
        CachedConfig cachedConfig = getCachedConfig(issuer);
        if (cachedConfig == null || !canRun(event, cachedConfig)) {
            return;
        }

        if (cachedConfig.getGoogleKey().isEmpty()) {
            logger.error("Google key was not defined.");
            issuer.sendError(Message.ERROR__INTERNAL);
            return;
        }

        get().whenCompleteAsync((val, ex) -> {
            if (!canCompleteContinue(issuer, val, ex)) {
                return;
            }

            MessageBuilder message = new MessageBuilder();
            message.append(event.getAuthor());
            message.append(" Did you know that.. ?\n");
            message.append(val);

            event.getChannel().sendMessage(message.build()).queue();
        });
    }

    public static @NotNull CompletableFuture<String> get() {
        return WebUtil.getUnclosedResponse(API_URLS.get(random.nextInt(API_URLS.size()))).thenApplyAsync(response -> {
            try (response) {
                JSONDeserializer<Map<String, Object>> modelDeserializer = new JSONDeserializer<>();
                Map<String, Object> retVal = modelDeserializer.deserialize(response.body().charStream());
                if (retVal == null || retVal.isEmpty()) {
                    return null;
                }
                if (retVal.containsKey("text")) {
                    return (String) retVal.get("text");
                } else if (retVal.containsKey("data")) {
                    return (String) retVal.get("data");
                }
                return null;
            }
        });
    }
}
