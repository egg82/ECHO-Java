package me.egg82.echo.commands.internal;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.JDACommandManager;
import io.paradaux.ai.MarkovMegaHal;
import java.io.File;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.config.ConfigurationFileUtil;
import me.egg82.echo.lang.Message;
import me.egg82.echo.messaging.GenericMessagingHandler;
import me.egg82.echo.messaging.MessagingHandler;
import me.egg82.echo.messaging.MessagingService;
import me.egg82.echo.storage.StorageService;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends AbstractCommand {
    private final File dataFolder;
    private final JDACommandManager manager;
    private final JDA jda;

    public ReloadCommand(@NotNull CommandIssuer issuer, @NotNull File dataFolder, @NotNull JDACommandManager manager, @NotNull JDA jda) {
        super(issuer);
        this.dataFolder = dataFolder;
        this.manager = manager;
        this.jda = jda;
    }

    public void run() {
        issuer.sendInfo(Message.RELOAD__BEGIN);

        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return;
        }

        for (MessagingService service : cachedConfig.getMessaging()) {
            service.close();
        }
        for (StorageService service : cachedConfig.getStorage()) {
            service.close();
        }

        ConfigurationFileUtil.reloadConfig(dataFolder, manager, new GenericMessagingHandler(), new MarkovMegaHal());

        issuer.sendInfo(Message.RELOAD__END);
    }
}
