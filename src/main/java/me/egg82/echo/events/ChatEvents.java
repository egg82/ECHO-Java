package me.egg82.echo.events;

import co.aikar.commands.JDACommandManager;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.messaging.packets.MessagePacket;
import me.egg82.echo.storage.StorageService;
import me.egg82.echo.utils.PacketUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ninja.egg82.events.JDAEvents;
import org.jetbrains.annotations.NotNull;

public class ChatEvents extends EventHolder {
    private JDA jda;
    private JDACommandManager manager;

    public ChatEvents(@NotNull JDA jda, @NotNull JDACommandManager manager) {
        this.jda = jda;
        this.manager = manager;

        JDAEvents.subscribe(jda, MessageReceivedEvent.class)
                .filter(e -> !e.getAuthor().isBot())
                .filter(e -> !e.isWebhookMessage())
                .handler(this::learn);
    }

    private void learn(@NotNull MessageReceivedEvent event) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return;
        }

        cachedConfig.getMegaHal().add(event.getMessage().getContentStripped());

        for (StorageService service : cachedConfig.getStorage()) {
            service.getOrCreateMessageModel(event.getMessage().getContentStripped());
        }

        MessagePacket packet = new MessagePacket();
        packet.setMessage(event.getMessage().getContentRaw());
        PacketUtil.queuePacket(packet);
    }
}
