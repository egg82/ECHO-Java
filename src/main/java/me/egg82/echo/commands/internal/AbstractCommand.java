package me.egg82.echo.commands.internal;

import co.aikar.commands.CommandIssuer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.egg82.echo.services.lookup.PlayerInfo;
import me.egg82.echo.services.lookup.PlayerLookup;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCommand implements Runnable {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final CommandIssuer issuer;

    protected AbstractCommand(@NotNull CommandIssuer issuer) {
        this.issuer = issuer;
    }

    protected @NotNull CompletableFuture<UUID> fetchUuid(@NotNull String name) { return PlayerLookup.get(name).thenApply(PlayerInfo::getUUID); }

    protected @NotNull CompletableFuture<Member> getMember(@NotNull String user, @NotNull Guild guild) {
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
