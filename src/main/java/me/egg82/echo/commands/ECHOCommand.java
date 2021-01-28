package me.egg82.echo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.JDACommandManager;
import co.aikar.commands.annotation.*;
import me.egg82.echo.commands.internal.LearnCommand;
import me.egg82.echo.commands.internal.ReloadCommand;
import me.egg82.echo.utils.FileUtil;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;

@CommandAlias("echo")
public class ECHOCommand extends BaseCommand {
    private final JDA jda;
    private final JDACommandManager manager;

    public ECHOCommand(@NotNull JDA jda, @NotNull JDACommandManager manager) {
        this.jda = jda;
        this.manager = manager;
    }

    @Subcommand("reload")
    @CommandPermission("echo.admin")
    @Description("{@@description.reload}")
    public void onReload(@NotNull CommandIssuer issuer) {
        new ReloadCommand(issuer, FileUtil.getCwd(), manager, jda).run();
    }

    @Subcommand("learn")
    @CommandPermission("echo.admin")
    @Description("{@@description.learn}")
    @Syntax("<url> [delimiter]")
    public void onReload(@NotNull CommandIssuer issuer, @NotNull String url, @Default("\n") String delimiter) {
        new LearnCommand(issuer, url, delimiter).run();
    }

    @CatchUnknown
    @Default
    @CommandCompletion("@subcommand")
    public void onDefault(@NotNull CommandIssuer issuer, String[] args) {
        manager.getRootCommand("echo help").execute(issuer, null, args);
    }

    @HelpCommand
    @Syntax("[command]")
    public void onHelp(@NotNull CommandIssuer issuer, @NotNull CommandHelp help) { help.showHelp(); }
}
