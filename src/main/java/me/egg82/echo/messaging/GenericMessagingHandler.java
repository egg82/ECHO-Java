package me.egg82.echo.messaging;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.messaging.packets.MultiPacket;
import me.egg82.echo.messaging.packets.Packet;
import me.egg82.echo.utils.PacketUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericMessagingHandler implements MessagingHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final LoadingCache<UUID, Boolean> messageCache = Caffeine.newBuilder().expireAfterWrite(2L, TimeUnit.MINUTES).expireAfterAccess(30L, TimeUnit.SECONDS).build(k -> Boolean.FALSE);
    private final Object messageCacheLock = new Object();

    public GenericMessagingHandler() { }

    public void handlePacket(@NotNull UUID messageId, @NotNull String fromService, @NotNull Packet packet) {
        if (isDuplicate(messageId)) {
            return;
        }

        try {
            handleGenericPacket(packet);
        } finally {
            PacketUtil.repeatPacket(messageId, packet, fromService);
        }
    }

    private void handleMulti(@NotNull MultiPacket packet) {
        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Handling multi-packet.");
        }

        for (Packet p : packet.getPackets()) {
            handleGenericPacket(p);
        }
    }

    private void handleGenericPacket(@NotNull Packet packet) {
        if (packet instanceof MultiPacket) {
            handleMulti((MultiPacket) packet);
        }
    }

    public void cancel() { }

    private boolean isDuplicate(@NotNull UUID messageId) {
        if (Boolean.TRUE.equals(messageCache.getIfPresent(messageId))) {
            return true;
        }
        // Double-checked locking
        synchronized (messageCacheLock) {
            if (Boolean.TRUE.equals(messageCache.getIfPresent(messageId))) {
                return true;
            }
            messageCache.put(messageId, Boolean.TRUE);
        }
        return false;
    }
}
