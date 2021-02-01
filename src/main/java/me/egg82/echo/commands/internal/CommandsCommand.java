package me.egg82.echo.commands.internal;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.JDACommandManager;
import java.awt.*;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.utils.JDAUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class CommandsCommand extends AbstractInternalCommand {
    private final JDACommandManager manager;
    private final String[] args;

    public CommandsCommand(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull JDACommandManager manager, String @NotNull [] args) {
        super(issuer, event);
        this.manager = manager;
        this.args = args;
    }

    public void run() {
        CachedConfig cachedConfig = getCachedConfig(issuer);
        if (cachedConfig == null || !canRun(event, cachedConfig)) {
            return;
        }

        boolean isAdmin = event.getMember() != null && JDAUtil.isAdmin(cachedConfig, event.getMember());
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(0x0CD7DE));

        if (args.length > 0) {

        } else {
            embed.setTitle("Commands");

            /*for (RootCommand rootCommand : manager.getRegisteredRootCommands()) {
                if (rootCommand instanceof AbstractCommand) {
                    AbstractCommand command = (AbstractCommand) rootCommand;
                    if (!command.isDisabled(cachedConfig) && (!command.requiresAdmin() || isAdmin)) {
                        embed.addField(command.getName() + command.showSyntax(), command.getCommandHelp(), false);
                    }
                } else {

                }
            }*/
        }

        event.getChannel().sendMessage(embed.build()).queue();
    }
}
