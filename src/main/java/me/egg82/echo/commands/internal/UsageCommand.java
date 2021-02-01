package me.egg82.echo.commands.internal;

import co.aikar.commands.*;
import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import me.egg82.echo.config.CachedConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class UsageCommand extends AbstractInternalCommand {
    private final CommandHelp help;

    public UsageCommand(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull CommandHelp help) {
        super(issuer, event);
        this.help = help;
    }

    public void run() {
        CachedConfig cachedConfig = getCachedConfig(issuer);
        if (cachedConfig == null || !canRun(event, cachedConfig)) {
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(0x0CD7DE));

        Iterator<HelpEntry> results;
        if (!help.getSelectedEntry().isEmpty()) {
            HelpEntry first = ACFUtil.getFirstElement(help.getSelectedEntry());
            embed.setTitle(first.getCommandPrefix() + first.getCommand());
            results = help.getSelectedEntry().iterator();
            while (results.hasNext()) {
                HelpEntry entry = results.next();
                embed.addField(entry.getCommand() + " " + entry.getParameterSyntax(issuer), "```" + entry.getDescription() + "```", false);
            }
        } else {
            if (help.getSearch() == null) {
                embed.setTitle("Commands");
            } else {
                String query = ACFUtil.join(help.getSearch(), " ");
                if (queryMentionsUsers(event, cachedConfig, query)) {
                    return;
                }
                embed.setTitle("Command Search: " + query);
            }

            List<HelpEntry> entries = help.getHelpEntries().stream().filter(HelpEntry::shouldShow).collect(Collectors.toList());
            results = entries.stream().sorted(Comparator.comparingInt(e -> e.getSearchScore() * -1)).iterator();
            if (!results.hasNext()) {
                entries = help.getHelpEntries();
                results = entries.iterator();
            }

            int min = (help.getPage() - 1) * help.getPerPage();
            int max = min + help.getPerPage();
            int i = 0;
            if (min >= entries.size()) {
                issuer.sendMessage(MessageType.HELP, MessageKeys.HELP_NO_RESULTS);
            } else {
                entries = new ArrayList<>();
                while (results.hasNext()) {
                    HelpEntry e = results.next();
                    if (i >= max) {
                        break;
                    }
                    if (i++ >= min) {
                        entries.add(e);
                    }
                }

                results = entries.iterator();
                while (results.hasNext()) {
                    HelpEntry entry = results.next();
                    embed.addField(entry.getCommand() + " " + entry.getParameterSyntax(issuer), entry.getDescription(), false);
                }
            }
        }
        embed.setFooter("For " + (event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getAsTag()));

        event.getChannel().sendMessage(embed.build()).queue();
    }
}
