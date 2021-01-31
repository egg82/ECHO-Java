package me.egg82.echo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.JDACommandManager;
import co.aikar.commands.annotation.*;
import me.egg82.echo.commands.internal.DoNotLearnCommand;
import me.egg82.echo.commands.internal.LearnCommand;
import me.egg82.echo.commands.internal.ReloadCommand;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.lang.Message;
import me.egg82.echo.utils.FileUtil;
import me.egg82.echo.utils.JDAUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandAlias("echo")
public class ECHOCommand extends BaseCommand {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JDA jda;
    private final JDACommandManager manager;

    public ECHOCommand(@NotNull JDA jda, @NotNull JDACommandManager manager) {
        this.jda = jda;
        this.manager = manager;
    }

    @Subcommand("reload")
    @Description("{@@description.reload}")
    public void onReload(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event) {
        if (event.getMember() != null && !JDAUtil.isAdmin(event.getMember())) {
            CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
            if (cachedConfig == null) {
                logger.error("Could not get cached config.");
                issuer.sendError(Message.ERROR__INTERNAL);
                return;
            }

            Emote emote = JDAUtil.getEmote(cachedConfig.getDisallowedEmote(), jda, event.getGuild());
            if (emote == null) {
                logger.warn("Could not find disallowed emote \"" + cachedConfig.getDisallowedEmote() + "\" for guild \"" + event.getGuild().getName() + "\".");
                return;
            }
            event.getMessage().addReaction(emote).queue();
            return;
        }

        new ReloadCommand(issuer, FileUtil.getCwd(), manager, jda).run();
    }

    @Subcommand("learn")
    @Description("{@@description.learn}")
    @Syntax("<url> [delimiter]")
    public void onLearn(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull String url, @Default("\n") String delimiter) {
        if (event.getMember() != null && !JDAUtil.isAdmin(event.getMember())) {
            CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
            if (cachedConfig == null) {
                logger.error("Could not get cached config.");
                issuer.sendError(Message.ERROR__INTERNAL);
                return;
            }

            Emote emote = JDAUtil.getEmote(cachedConfig.getDisallowedEmote(), jda, event.getGuild());
            if (emote == null) {
                logger.warn("Could not find disallowed emote \"" + cachedConfig.getDisallowedEmote() + "\" for guild \"" + event.getGuild().getName() + "\".");
                return;
            }
            event.getMessage().addReaction(emote).queue();
            return;
        }

        new LearnCommand(issuer, url, delimiter).run();
    }

    @Subcommand("donotlearn|dnl")
    @Description("{@@description.no-learn}")
    @Syntax("<user> [learn]")
    public void onDnl(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull String user, @Default("false") boolean learning) {
        if (event.getMember() != null && !JDAUtil.isAdmin(event.getMember())) {
            CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
            if (cachedConfig == null) {
                logger.error("Could not get cached config.");
                issuer.sendError(Message.ERROR__INTERNAL);
                return;
            }

            Emote emote = JDAUtil.getEmote(cachedConfig.getDisallowedEmote(), jda, event.getGuild());
            if (emote == null) {
                logger.warn("Could not find disallowed emote \"" + cachedConfig.getDisallowedEmote() + "\" for guild \"" + event.getGuild().getName() + "\".");
                return;
            }
            event.getMessage().addReaction(emote).queue();
            return;
        }

        new DoNotLearnCommand(issuer, user, event.getGuild(), learning).run();
    }

    @CatchUnknown
    @Default
    @CommandCompletion("@subcommand")
    public void onDefault(@NotNull CommandIssuer issuer, String[] args) {
        manager.getRootCommand("echo help").execute(issuer, args[0], args);
    }

    @HelpCommand
    @Syntax("[command]")
    public void onHelp(@NotNull CommandIssuer issuer, @NotNull CommandHelp help) { help.showHelp(); }
}
