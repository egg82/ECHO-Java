package me.egg82.echo.commands.internal;

import co.aikar.commands.*;
import co.aikar.locales.MessageKey;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.lang.Message;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                String description = getDescription(help.getManager(), entry.getDescription());
                embed.addField(entry.getCommand() + " " + entry.getParameterSyntax(issuer), "```" + (description == null ? "No description available" : description) + "```", false);
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
                    String description = getDescription(help.getManager(), entry.getDescription());
                    embed.addField(entry.getCommand() + " " + entry.getParameterSyntax(issuer), "```" + (description == null ? "No description available" : description) + "```", false);
                }
            }
        }
        embed.setFooter("For " + (event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getAsTag()));

        if (embed.getFields().isEmpty()) {
            issuer.sendError(Message.ERROR__COMMAND_NOT_EXIST);
        } else {
            event.getChannel().sendMessage(embed.build()).queue();
        }
    }

    private static final Pattern RE_DESC = Pattern.compile("^\\{@@(.+)\\}$");

    private @Nullable String getDescription(@NotNull CommandManager manager, @NotNull String description) {
        Matcher matcher = RE_DESC.matcher(description);
        if (matcher.find()) {
            return manager.getLocales().getOptionalMessage(issuer, MessageKey.of(matcher.group(1)));
        }
        return description;
    }
}
