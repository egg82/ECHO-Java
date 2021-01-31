package me.egg82.echo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import flexjson.JSONDeserializer;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
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
import me.egg82.echo.web.models.XKCDInfoModel;
import me.egg82.echo.web.models.XKCDSearchModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandAlias("xkcd")
public class XKCDCommand extends BaseCommand {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SEARCH_URL = "https://relevantxkcd.appspot.com/process?action=xkcd&query=%s";
    private static final String INFO_URL = "https://xkcd.com/%d/info.0.json";

    public XKCDCommand() { }

    @Default
    @Description("{@@description.xkcd}")
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
            embed.setTitle("XKCD: " + val.getSafeTitle(), val.getLink().isEmpty() ? String.format("https://xkcd.com/%d/", val.getNum()) : val.getLink());
            embed.setImage(val.getImg());
            embed.setFooter("For " + (event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getAsTag()) + " | " + val.getAlt());

            event.getChannel().sendMessage(embed.build()).queue();
        });
    }

    public static @NotNull CompletableFuture<XKCDInfoModel> getModel(@NotNull String query) {
        return search(query).thenApply(v -> {
            if (v == null) {
                return null;
            }

            try {
                Request request = WebUtil.getDefaultRequestBuilder(new URL(String.format(INFO_URL, v.getComics().get(v.getSelection()).leftInt())))
                        .header("Accept", "application/json")
                        .build();

                try (Response response = WebUtil.getResponse(request)) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
                    }

                    JSONDeserializer<XKCDInfoModel> modelDeserializer = new JSONDeserializer<>();
                    XKCDInfoModel retVal = modelDeserializer.deserialize(response.body().charStream(), XKCDInfoModel.class);
                    return retVal == null || retVal.getNum() == -1 ? null : retVal;
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }

    private static @NotNull CompletableFuture<XKCDSearchModel> search(@NotNull String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Request request = WebUtil.getDefaultRequestBuilder(new URL(String.format(SEARCH_URL, WebUtil.urlEncode(query.replace("\\s+", "+")))))
                        .header("Accept", "text/plain")
                        .build();

                try (Response response = WebUtil.getResponse(request)) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
                    }

                    String[] splitContent = response.body().string().replace("\r", "").replace("\n", " ").split("\\s+");
                    if (splitContent.length < 2) {
                        return null;
                    }

                    XKCDSearchModel retVal = new XKCDSearchModel();
                    retVal.setWeight(Float.parseFloat(splitContent[0]));
                    retVal.setSelection(Integer.parseInt(splitContent[1]));
                    for (int i = 2; i < splitContent.length; i += 2) {
                        retVal.getComics().add(new IntObjectImmutablePair<>(Integer.parseInt(splitContent[i]), splitContent[i + 1]));
                    }

                    return retVal;
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }
}
