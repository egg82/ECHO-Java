package me.egg82.echo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.JDACommandManager;
import co.aikar.commands.annotation.*;
import me.egg82.echo.commands.internal.ReloadCommand;
import me.egg82.echo.utils.FileUtil;
import net.dv8tion.jda.api.JDA;
import org.checkerframework.checker.nullness.qual.NonNull;
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
    public void onReload(@NonNull CommandIssuer issuer) {
        new ReloadCommand(issuer, FileUtil.getCwd(), manager, jda).run();
    }

    @CatchUnknown
    @Default
    @CommandCompletion("@subcommand")
    public void onDefault(@NonNull CommandIssuer issuer, String[] args) {
        manager.getRootCommand("echo help").execute(issuer, null, args);
    }

    @HelpCommand
    @Syntax("[command]")
    public void onHelp(@NonNull CommandIssuer issuer, @NonNull CommandHelp help) { help.showHelp(); }
}
