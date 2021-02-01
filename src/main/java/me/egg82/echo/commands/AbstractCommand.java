package me.egg82.echo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.core.Pair;
import me.egg82.echo.lang.Message;
import me.egg82.echo.services.lookup.PlayerInfo;
import me.egg82.echo.services.lookup.PlayerLookup;
import me.egg82.echo.utils.JDAUtil;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCommand extends BaseCommand {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public abstract boolean requiresAdmin();

    public boolean isDisabled(@NotNull CachedConfig cachedConfig) { return cachedConfig.getDisabledCommands().contains(getName()); }

    protected final @Nullable CachedConfig getCachedConfig(@NotNull CommandIssuer issuer) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            issuer.sendError(Message.ERROR__INTERNAL);
        }
        return cachedConfig;
    }

    protected final boolean canRun(@NotNull MessageReceivedEvent event, @NotNull CachedConfig cachedConfig) {
        if (event.getAuthor().isBot() || isDisabled(cachedConfig)) {
            return false;
        }

        if (event.getMember() != null && !JDAUtil.isAllowed(cachedConfig, event.getMember()) || (requiresAdmin() && !JDAUtil.isAdmin(cachedConfig, event.getMember()))) {
            Emote emote = JDAUtil.getEmote(cachedConfig.getDisallowedEmote(), event.getGuild());
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

    protected final boolean queryMentionsUsers(@NotNull MessageReceivedEvent event, @NotNull CachedConfig cachedConfig, @NotNull String query) {
        if (query.contains("@")) { // TODO: find a better way to do this
            Emote emote = JDAUtil.getEmote(cachedConfig.getDisallowedEmote(), event.getGuild());
            if (emote == null) {
                logger.warn("Could not find disallowed emote \"" + cachedConfig.getDisallowedEmote() + "\" for guild \"" + event.getGuild().getName() + "\".");
                return true;
            }
            event.getMessage().addReaction(emote).queue();
            return true;
        }
        return false;
    }

    protected final @NotNull CompletableFuture<UUID> fetchUuid(@NotNull String name) { return PlayerLookup.get(name).thenApply(PlayerInfo::getUUID); }

    protected final @NotNull CompletableFuture<Member> getMember(@NotNull String user, @NotNull Guild guild) {
        return CompletableFuture.supplyAsync(() -> {
            List<Member> members = guild.findMembers(m -> m.getId().equals(user)).get();
            Member retVal = !members.isEmpty() ? members.get(0) : null;
            if (retVal == null) {
                members = guild.findMembers(m -> m.getUser().getAsTag().equalsIgnoreCase(user)).get();
                retVal = !members.isEmpty() ? members.get(0) : null;
                if (retVal == null) {
                    members = guild.findMembers(m -> m.getEffectiveName().equalsIgnoreCase(user)).get();
                    retVal = !members.isEmpty() ? members.get(0) : null;
                }
            }

            return retVal;
        });
    }
}
