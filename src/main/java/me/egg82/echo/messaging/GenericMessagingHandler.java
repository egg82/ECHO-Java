package me.egg82.echo.messaging;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.paradaux.ai.MarkovMegaHal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.messaging.packets.*;
import me.egg82.echo.services.CollectionProvider;
import me.egg82.echo.storage.StorageService;
import me.egg82.echo.storage.models.LearnModel;
import me.egg82.echo.storage.models.MessageModel;
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

    private void handleMessage(@NotNull MessagePacket packet) {
        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Handling message packet: " + packet.getMessage());
        }

        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return;
        }

        cachedConfig.getMegaHal().add(packet.getMessage());

        for (StorageService service : cachedConfig.getStorage()) {
            service.getOrCreateMessageModel(packet.getMessage());
        }
    }

    private void handleMessageUpdate(@NotNull MessageUpdatePacket packet) {
        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Handling message update packet: " + packet.getNewMessage());
        }

        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return;
        }

        MarkovMegaHal megaHal = cachedConfig.getMegaHal();
        //megaHal.remove(packet.getOldMessage()); // TODO: Add MegaHal removal once that becomes a thing in the library
        megaHal.add(packet.getNewMessage());

        for (StorageService service : cachedConfig.getStorage()) {
            MessageModel model = service.getOrCreateMessageModel(packet.getOldMessage());
            model.setMessage(packet.getNewMessage());
            service.storeModel(model);
        }
    }

    private void handleLearn(@NotNull LearnPacket packet) {
        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Handling learn packet: " + packet.getUser());
        }

        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return;
        }

        CollectionProvider.getCanLearnCache().put(packet.getUser(), packet.isLearning());

        for (StorageService service : cachedConfig.getStorage()) {
            LearnModel model = service.getOrCreateLearnModel(packet.getUser(), packet.isLearning());
            model.setLearning(packet.isLearning());
            service.storeModel(model);
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
        if (packet instanceof MessagePacket) {
            handleMessage((MessagePacket) packet);
        } else if (packet instanceof MessageUpdatePacket) {
            handleMessageUpdate((MessageUpdatePacket) packet);
        } else if (packet instanceof LearnPacket) {
            handleLearn((LearnPacket) packet);
        } else if (packet instanceof MultiPacket) {
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
