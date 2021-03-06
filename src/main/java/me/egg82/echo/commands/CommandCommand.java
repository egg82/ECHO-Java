package me.egg82.echo.commands;

import co.aikar.commands.*;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import co.aikar.locales.MessageKey;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.lang.Message;
import me.egg82.echo.services.CollectionProvider;
import me.egg82.echo.utils.JDAUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@CommandAlias("command|commands|help")
public class CommandCommand extends AbstractCommand {
    public CommandCommand() { }

    @Override
    public boolean requiresAdmin() { return false; }

    @Override
    public @Nullable EmbedBuilder getDescription() { return null; }

    @Default
    @Description("{@@description.command}")
    @Syntax("[search]")
    public void submit(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, String[] args) {
        CachedConfig cachedConfig = getCachedConfig(issuer);
        if (cachedConfig == null || !canRun(event, cachedConfig)) {
            return;
        }

        boolean isAdmin = event.getMember() != null && JDAUtil.isAdmin(cachedConfig, event.getMember());
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(0x0CD7DE));

        if (args != null && args.length > 0) {
            String query = ACFUtil.join(args, " ");
            if (queryMentionsUsers(event, cachedConfig, query)) {
                return;
            }
            embed.setTitle("Command Search: " + query);

            if (issuer.getManager().getRootCommand(query) == null) {
                issuer.sendError(Message.ERROR__COMMAND_NOT_EXIST);
                return;
            }

            CommandHelp help = issuer.getManager().generateCommandHelp(issuer, query);
            List<HelpEntry> entries = help.getHelpEntries().stream().filter(HelpEntry::shouldShow).collect(Collectors.toList());
            Iterator<HelpEntry> results = entries.stream().sorted(Comparator.comparingInt(e -> e.getSearchScore() * -1)).iterator();
            if (!results.hasNext()) {
                entries = help.getHelpEntries();
                results = entries.iterator();
            }

            int min = (help.getPage() - 1) * help.getPerPage();
            int max = min + help.getPerPage();
            int i = 0;
            if (min >= entries.size()) {
                issuer.sendMessage(MessageType.HELP, MessageKeys.HELP_NO_RESULTS);
                return;
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
                    AbstractCommand command = CollectionProvider.getCommand((JDACommandManager) help.getManager(), entry.getCommand(), false);
                    if (command == null || (!isAdmin && command.requiresAdmin()) || command.isDisabled(cachedConfig)) {
                        continue;
                    }
                    String description = getDescription(issuer, help.getManager(), entry.getDescription());
                    embed.addField(
                            entry.getCommand() + " " + entry.getParameterSyntax(issuer),
                            "```" + (description == null ? "No description available" : description) + "```",
                            false
                    );
                    EmbedBuilder descriptionBuilder = command.getDescription();
                    if (descriptionBuilder != null) {
                        for (MessageEmbed.Field field : descriptionBuilder.getFields()) {
                            embed.addField(field);
                        }
                    }
                }
            }
        } else {
            embed.setTitle("Commands");
            for (AbstractCommand command : CollectionProvider.getCommands((JDACommandManager) issuer.getManager())) {
                if ((!command.requiresAdmin() || isAdmin) && !command.isDisabled(cachedConfig)) {
                    CommandHelp help = issuer.getManager().generateCommandHelp(issuer, command.getName());

                    if (!help.getHelpEntries().isEmpty()) {
                        for (HelpEntry entry : help.getHelpEntries()) {
                            String description = getDescription(issuer, help.getManager(), entry.getDescription());
                            embed.addField(
                                    entry.getCommand() + " " + entry.getParameterSyntax(issuer),
                                    "```" + (description == null ? "No description available" : description) + "```",
                                    false
                            );
                        }
                    }
                }
            }
        }
        embed.setFooter("For " + (event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getAsTag()));

        event.getChannel().sendMessage(embed.build()).queue();
    }

    private static final Pattern RE_DESC = Pattern.compile("^\\{@@(.+)\\}$");

    private @Nullable String getDescription(@NotNull CommandIssuer issuer, @NotNull CommandManager manager, @NotNull String description) {
        Matcher matcher = RE_DESC.matcher(description);
        if (matcher.find()) {
            return manager.getLocales().getOptionalMessage(issuer, MessageKey.of(matcher.group(1)));
        }
        return description;
    }
}
