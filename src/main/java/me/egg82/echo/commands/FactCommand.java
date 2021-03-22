package me.egg82.echo.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import flexjson.JSONDeserializer;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.core.NullablePair;
import me.egg82.echo.utils.WebUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@CommandAlias("fact|facts|factoid|factoids|useless")
public class FactCommand extends AbstractCommand {
    private static final List<NullablePair<String, String>> API_URLS = Arrays.asList(
            new NullablePair<>("https://uselessfacts.jsph.pl/random.json?language=en", "text"),
            new NullablePair<>("https://useless-facts.sameerkumar.website/api", "data"),
            new NullablePair<>("https://catfact.ninja/fact", "fact"),
            new NullablePair<>("http://numbersapi.com/random/trivia", null),
            new NullablePair<>("http://numbersapi.com/random/year", null),
            new NullablePair<>("http://numbersapi.com/random/date", null),
            new NullablePair<>("http://numbersapi.com/random/math", null)
    );

    private static final Random random = new Random();

    public FactCommand() { }

    @Override
    public boolean requiresAdmin() { return false; }

    @Override
    public @Nullable EmbedBuilder getDescription() { return null; }

    @Default
    @Description("{@@description.fact}")
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
            message.append(" Did you know that.. ?\n");
            message.append(val);

            event.getChannel().sendMessage(message.build()).queue();
        });
    }

    public static @NotNull CompletableFuture<String> get() {
        NullablePair<String, String> data = API_URLS.get(random.nextInt(API_URLS.size()));
        String url = data.getT1();
        while (url.contains("%d")) {
            url = String.format(url, random.nextInt(100));
        }

        return WebUtil.getUnclosedResponse(url, "application/json").thenApplyAsync(response -> {
            try (response) {
                if (data.getT2() == null) {
                    return response.body().string();
                }

                JSONDeserializer<Map<String, Object>> modelDeserializer = new JSONDeserializer<>();
                Map<String, Object> retVal = modelDeserializer.deserialize(response.body().charStream());
                return retVal == null || retVal.isEmpty() ? null : (String) retVal.get(data.getT2());
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }
}
