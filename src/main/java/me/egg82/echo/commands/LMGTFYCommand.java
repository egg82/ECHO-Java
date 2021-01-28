package me.egg82.echo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.awt.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

@CommandAlias("lmgtfy")
public class LMGTFYCommand extends BaseCommand {
    private static final String SEARCH_URL = "https://lmgtfy.com/?q=%s&iie=1";

    public LMGTFYCommand() { }

    @Default
    @Description("{@@description.lmgtfy}")
    @Syntax("<search>")
    public void submit(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull String query) {
        if (event.getAuthor().isBot()) {
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Click for answer!", String.format(SEARCH_URL, query.replace("\\s+", "+")));
        embed.setColor(new Color(0x17E77E));
        embed.setFooter("For " + event.getAuthor().getAsTag());

        event.getChannel().sendMessage(embed.build()).queue();
    }
}
