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
import me.egg82.echo.utils.WebUtil;
import me.egg82.echo.web.models.RoryModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("rory")
public class RoryCommand extends AbstractCommand {
    private static final String CAT_URL = "https://rory.cat/purr/";
    private static final String CAT_URL_ID = "https://rory.cat/purr/%d";

    public RoryCommand() { }

    public boolean requiresAdmin() { return false; }

    public @Nullable EmbedBuilder getDescription() { return null; }

    @Default
    @Description("{@@description.rory}")
    @Syntax("[id]")
    public void submit(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @Default("-1") int id) {
        CachedConfig cachedConfig = getCachedConfig(issuer);
        if (cachedConfig == null || !canRun(event, cachedConfig)) {
            return;
        }

        get(id).whenCompleteAsync((val, ex) -> {
            if (!canCompleteContinue(issuer, val, ex)) {
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
        return WebUtil.getUnclosedResponse(id == -1 ? CAT_URL : String.format(CAT_URL_ID, id)).thenApplyAsync(response -> {
            try (response) {
                JSONDeserializer<RoryModel> modelDeserializer = new JSONDeserializer<>();
                RoryModel retVal = modelDeserializer.deserialize(response.body().charStream(), RoryModel.class);
                return retVal == null || retVal.getId() == -1 ? null : retVal;
            }
        });
    }
}
