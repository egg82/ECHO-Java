package me.egg82.echo.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import flexjson.JSONDeserializer;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import java.util.concurrent.CompletableFuture;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.utils.WebUtil;
import me.egg82.echo.web.models.XKCDInfoModel;
import me.egg82.echo.web.models.XKCDSearchModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

@CommandAlias("xkcd")
public class XKCDCommand extends AbstractCommand {
    private static final String SEARCH_URL = "https://relevantxkcd.appspot.com/process?action=xkcd&query=%s";
    private static final String INFO_URL = "https://xkcd.com/%d/info.0.json";

    public XKCDCommand() { }

    @Default
    @Description("{@@description.xkcd}")
    @Syntax("<search>")
    public void submit(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull String query) {
        CachedConfig cachedConfig = getCachedConfig(issuer);
        if (cachedConfig == null || !canRun(event, cachedConfig)) {
            return;
        }

        getModel(query).whenCompleteAsync((val, ex) -> {
            if (!canCompleteContinue(issuer, val, ex)) {
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
        return search(query).thenComposeAsync(v -> WebUtil.getUnclosedResponse(String.format(INFO_URL, v.getComics().get(v.getSelection()).leftInt())).thenApplyAsync(response -> {
            try (response) {
                JSONDeserializer<XKCDInfoModel> modelDeserializer = new JSONDeserializer<>();
                XKCDInfoModel retVal = modelDeserializer.deserialize(response.body().charStream(), XKCDInfoModel.class);
                return retVal == null || retVal.getNum() == -1 ? null : retVal;
            }
        }));
    }

    private static @NotNull CompletableFuture<XKCDSearchModel> search(@NotNull String query) {
        return WebUtil.getString(String.format(SEARCH_URL, WebUtil.urlEncode(query))).thenApplyAsync(val -> {
            String[] splitContent = val.replace("\r", "").replace("\n", " ").split("\\s+");
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
        });
    }
}
