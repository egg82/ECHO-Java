package me.egg82.echo.commands.internal;

import co.aikar.commands.CommandIssuer;
import io.paradaux.ai.MarkovMegaHal;
import java.io.IOException;
import java.net.URL;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.lang.Message;
import me.egg82.echo.messaging.packets.MessagePacket;
import me.egg82.echo.storage.StorageService;
import me.egg82.echo.utils.PacketUtil;
import me.egg82.echo.web.WebConstants;
import me.egg82.echo.web.WebRequest;
import org.jetbrains.annotations.NotNull;

public class LearnCommand extends AbstractCommand {
    private final String url;
    private final String delimiter;

    public LearnCommand(@NotNull CommandIssuer issuer, @NotNull String url, @NotNull String delimiter) {
        super(issuer);
        this.url = url;
        this.delimiter = delimiter;
    }

    public void run() {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return;
        }

        issuer.sendInfo(Message.LEARN__BEGIN);

        String[] splitContent;
        try {
            splitContent = WebRequest.builder(new URL(url))
                    .timeout(WebConstants.TIMEOUT)
                    .userAgent(WebConstants.USER_AGENT)
                    .header("Content-Type", "text/plain")
                    .build().getString().replace("\r", "").split(delimiter);
        } catch (IOException ex) {
            if (cachedConfig.getDebug()) {
                logger.error(ex.getMessage(), ex);
            } else {
                logger.error(ex.getMessage());
            }
            return;
        }

        if (splitContent.length <= 1) {
            issuer.sendError(Message.ERROR__INTERNAL);
            return;
        }

        MarkovMegaHal megaHal = cachedConfig.getMegaHal();

        for (String sentence : splitContent) {
            megaHal.add(sentence);

            for (StorageService service : cachedConfig.getStorage()) {
                service.getOrCreateMessageModel(sentence);
            }

            MessagePacket packet = new MessagePacket();
            packet.setMessage(sentence);
            PacketUtil.queuePacket(packet);
        }

        issuer.sendInfo(Message.LEARN__END);
    }
}
