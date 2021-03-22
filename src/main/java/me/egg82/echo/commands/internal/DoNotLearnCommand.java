package me.egg82.echo.commands.internal;

import co.aikar.commands.CommandIssuer;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.lang.Message;
import me.egg82.echo.messaging.packets.LearnPacket;
import me.egg82.echo.services.CollectionProvider;
import me.egg82.echo.storage.StorageService;
import me.egg82.echo.storage.models.LearnModel;
import me.egg82.echo.utils.PacketUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class DoNotLearnCommand extends AbstractInternalCommand {
    private final String user;
    private final Guild guild;
    private final boolean learning;

    public DoNotLearnCommand(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull String user, @NotNull Guild guild, boolean learning) {
        super(issuer, event);
        this.user = user;
        this.guild = guild;
        this.learning = learning;
    }

    @Override
    public void run() {
        CachedConfig cachedConfig = getCachedConfig(issuer);
        if (cachedConfig == null || !canRun(event, cachedConfig)) {
            return;
        }

        issuer.sendInfo(Message.NO_LEARN__BEGIN);

        getMember(user, guild).whenCompleteAsync((val, ex) -> {
            if (!canCompleteContinue(issuer, val, ex)) {
                return;
            }

            for (StorageService service : cachedConfig.getStorage()) {
                LearnModel model = service.getOrCreateLearnModel(val.getIdLong(), learning);
                model.setLearning(learning);
                service.storeModel(model);
            }

            LearnPacket packet = new LearnPacket();
            packet.setUser(val.getIdLong());
            packet.setLearning(learning);
            PacketUtil.queuePacket(packet);

            CollectionProvider.getCanLearnCache().put(val.getIdLong(), learning);

            if (learning) {
                issuer.sendInfo(Message.NO_LEARN__LEARNING, "{user}", val.getUser().getAsTag());
            } else {
                issuer.sendInfo(Message.NO_LEARN__NOT_LEARNING, "{user}", val.getUser().getAsTag());
            }
        });
    }
}
