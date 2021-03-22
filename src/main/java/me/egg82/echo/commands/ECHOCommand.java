package me.egg82.echo.commands;

import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.JDACommandManager;
import co.aikar.commands.annotation.*;
import me.egg82.echo.commands.internal.*;
import me.egg82.echo.utils.FileUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("echo")
public class ECHOCommand extends AbstractCommand {
    private final JDA jda;
    private final JDACommandManager manager;

    public ECHOCommand(@NotNull JDA jda, @NotNull JDACommandManager manager) {
        this.jda = jda;
        this.manager = manager;
    }

    @Override
    public boolean requiresAdmin() { return true; }

    @Override
    public @Nullable EmbedBuilder getDescription() { return null; }

    @Subcommand("reload")
    @Description("{@@description.reload}")
    public void onReload(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event) {
        new ReloadCommand(issuer, event, FileUtil.getCwd(), manager, jda).run();
    }

    @Subcommand("learn")
    @Description("{@@description.learn}")
    @Syntax("<url> [delimiter]")
    public void onLearn(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull String url, @Default("\n") String delimiter) {
        new LearnCommand(issuer, event, url, delimiter).run();
    }

    @Subcommand("donotlearn|dnl")
    @Description("{@@description.no_learn}")
    @Syntax("<user> [learn]")
    public void onDnl(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull String user, @Default("false") boolean learning) {
        new DoNotLearnCommand(issuer, event, user, event.getGuild(), learning).run();
    }

    @CatchUnknown
    @Default
    public void onDefault(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, String[] args) {
        new CommandsCommand(issuer, event, manager, args).run();
    }

    @HelpCommand
    @Description("{@@description.help}")
    @Syntax("[command]")
    public void onHelp(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull CommandHelp help) {
        new InternalUsageCommand(issuer, event, help).run();
    }
}
