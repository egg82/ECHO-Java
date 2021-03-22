package me.egg82.echo.commands.internal;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.JDACommandManager;
import io.paradaux.ai.MarkovMegaHal;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigurationFileUtil;
import me.egg82.echo.lang.Message;
import me.egg82.echo.messaging.GenericMessagingHandler;
import me.egg82.echo.messaging.MessagingService;
import me.egg82.echo.storage.StorageService;
import me.egg82.echo.utils.ResponseUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ReloadCommand extends AbstractInternalCommand {
    private final File dataFolder;
    private final JDACommandManager manager;
    private final JDA jda;

    public ReloadCommand(
            @NotNull CommandIssuer issuer,
            @NotNull MessageReceivedEvent event,
            @NotNull File dataFolder,
            @NotNull JDACommandManager manager,
            @NotNull JDA jda
    ) {
        super(issuer, event);
        this.dataFolder = dataFolder;
        this.manager = manager;
        this.jda = jda;
    }

    @Override
    public void run() {
        CachedConfig cachedConfig = getCachedConfig(issuer);
        if (cachedConfig == null || !canRun(event, cachedConfig)) {
            return;
        }

        issuer.sendInfo(Message.RELOAD__BEGIN);

        for (MessagingService service : cachedConfig.getMessaging()) {
            service.close();
        }
        for (StorageService service : cachedConfig.getStorage()) {
            service.close();
        }

        ConfigurationFileUtil.reloadConfig(dataFolder, manager, new GenericMessagingHandler(), new MarkovMegaHal());
        ResponseUtil.loadMegaHal(manager);

        issuer.sendInfo(Message.RELOAD__END);
    }
}
