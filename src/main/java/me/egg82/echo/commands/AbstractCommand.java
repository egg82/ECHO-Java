package me.egg82.echo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.core.Pair;
import me.egg82.echo.lang.Message;
import me.egg82.echo.utils.JDAUtil;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCommand extends BaseCommand {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final @Nullable CachedConfig getCachedConfig(@NotNull CommandIssuer issuer) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            issuer.sendError(Message.ERROR__INTERNAL);
        }
        return cachedConfig;
    }

    protected final boolean canRun(@NotNull MessageReceivedEvent event, @NotNull CachedConfig cachedConfig) {
        if (event.getAuthor().isBot() || cachedConfig.getDisabledCommands().contains(getName())) {
            return false;
        }

        if (event.getMember() != null && !JDAUtil.isAllowed(event.getMember())) {
            Emote emote = JDAUtil.getEmote(cachedConfig.getDisallowedEmote(), event.getJDA(), event.getGuild());
            if (emote == null) {
                logger.warn("Could not find disallowed emote \"" + cachedConfig.getDisallowedEmote() + "\" for guild \"" + event.getGuild().getName() + "\".");
                return false;
            }
            event.getMessage().addReaction(emote).queue();
            return false;
        }

        return true;
    }

    protected final boolean canCompleteContinue(@NotNull CommandIssuer issuer, Object val, Throwable ex) {
        if (ex != null) {
            if (ConfigUtil.getDebugOrFalse()) {
                logger.error(ex.getMessage(), ex);
            } else {
                logger.error(ex.getMessage());
            }
            issuer.sendError(Message.ERROR__INTERNAL);
            return false;
        }

        if (val == null) {
            issuer.sendError(Message.ERROR__INTERNAL);
            return false;
        }

        if (val instanceof Pair && (((Pair<?, ?>) val).getT1() == null || ((Pair<?, ?>) val).getT2() == null)) {
            issuer.sendError(Message.ERROR__INTERNAL);
            return false;
        }

        return true;
    }

    protected final boolean queryMentionsUsers(@NotNull CommandIssuer issuer, @NotNull String query) {
        if (query.contains("@")) { // TODO: find a better way to do this
            issuer.sendError(Message.ERROR__INTERNAL);
            return true;
        }
        return false;
    }
}
