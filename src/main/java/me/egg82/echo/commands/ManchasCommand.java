package me.egg82.echo.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.utils.WebUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

@CommandAlias("manchas")
public class ManchasCommand extends AbstractCommand {
    private static final String CAT_URL = "https://api.manchas.cat/";
    private static final String CAT_URL_ID = "https://api.manchas.cat/%d";

    public ManchasCommand() { }

    @Override
    public boolean requiresAdmin() { return false; }

    @Override
    public @Nullable EmbedBuilder getDescription() { return null; }

    @Default
    @Description("{@@description.manchas}")
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
            embed.setImage(val);
            embed.setColor(new Color(0x09E214));
            embed.setFooter("For " + (event.getMember() != null
                                      ? event.getMember().getEffectiveName()
                                      : event.getAuthor().getAsTag()) + " | ID: " + val.substring(val.lastIndexOf('/') + 1));

            event.getChannel().sendMessage(embed.build()).queue();
        });
    }

    public static @NotNull CompletableFuture<String> get(int id) {
        return WebUtil.getRedirectedUrl(id == -1 ? CAT_URL : String.format(CAT_URL_ID, id))
                .thenApplyAsync(HttpUrl::toString);
    }
}
