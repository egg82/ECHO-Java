package me.egg82.echo.commands.internal;

import co.aikar.commands.CommandIssuer;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.lang.Message;
import me.egg82.echo.utils.ResponseUtil;
import me.egg82.echo.utils.WebUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class LearnCommand extends AbstractInternalCommand {
    private final String url;
    private final String delimiter;

    public LearnCommand(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull String url, @NotNull String delimiter) {
        super(issuer, event);
        this.url = url;
        this.delimiter = delimiter;
    }

    public void run() {
        CachedConfig cachedConfig = getCachedConfig(issuer);
        if (cachedConfig == null || !canRun(event, cachedConfig)) {
            return;
        }

        issuer.sendInfo(Message.LEARN__BEGIN);

        WebUtil.getString(url).whenCompleteAsync((val, ex) -> {
            if (!canCompleteContinue(issuer, val, ex)) {
                return;
            }

            String[] splitContent = val.replace("\r", "").split(delimiter);
            if (splitContent.length <= 1) {
                issuer.sendError(Message.ERROR__INTERNAL);
                return;
            }

            ResponseUtil.learnAll(cachedConfig, splitContent);

            issuer.sendInfo(Message.LEARN__END, "{url}", url);
        });
    }
}
