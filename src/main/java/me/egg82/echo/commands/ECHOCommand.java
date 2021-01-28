package me.egg82.echo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.egg82.echo.commands.internal.ReloadCommand;
import me.egg82.echo.utils.FileUtil;
import net.dv8tion.jda.api.JDA;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

@CommandAlias("echo")
public class ECHOCommand extends BaseCommand {
    private final JDA jda;
    private final CommandManager manager;

    public ECHOCommand(@NotNull JDA jda, @NotNull CommandManager manager) {
        this.jda = jda;
        this.manager = manager;
    }

    @Subcommand("reload")
    @CommandPermission("echo.admin")
    @Description("{@@description.reload}")
    public void onReload(@NonNull CommandIssuer issuer) {
        new ReloadCommand(issuer, FileUtil.getCwd(), manager, jda).run();
    }

    /*@CatchUnknown
    @Default
    @CommandCompletion("@subcommand")
    public void onDefault(@NonNull CommandSender sender, String[] args) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(sender, "echo help");
    }

    @HelpCommand
    @Syntax("[command]")
    public void onHelp(@NonNull CommandSender sender, @NonNull CommandHelp help) { help.showHelp(); }*/
}
