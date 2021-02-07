package me.egg82.echo.utils;

import co.aikar.commands.JDACommandManager;
import io.paradaux.ai.MarkovMegaHal;
import java.util.Collection;
import java.util.Set;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.lang.Message;
import me.egg82.echo.messaging.packets.MessagePacket;
import me.egg82.echo.services.CollectionProvider;
import me.egg82.echo.storage.StorageService;
import me.egg82.echo.storage.models.MessageModel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseUtil {
    private static final Logger logger = LoggerFactory.getLogger(ResponseUtil.class);

    private ResponseUtil() { }

    public static void loadMegaHal(@NotNull JDACommandManager manager) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return;
        }

        MarkovMegaHal megaHal = cachedConfig.getMegaHal();
        StorageService master = cachedConfig.getStorage().get(0);

        if (cachedConfig.getDebug()) {
            BotLogUtil.sendInfo(logger, manager, Message.IMPORT__BEGIN);
            BotLogUtil.sendInfo(logger, manager, Message.IMPORT__MESSAGES, "{id}", "0");
        }

        int start = 1;
        int max = 1000;
        Set<MessageModel> models;
        do {
            models = master.getAllMessages(start, max);
            for (MessageModel model : models) {
                megaHal.add(model.getMessage());
            }
            if (cachedConfig.getDebug()) {
                BotLogUtil.sendInfo(logger, manager, Message.IMPORT__MESSAGES, "{id}", String.valueOf(start + models.size()));
            }
            start += models.size();
        } while (models.size() == max);

        logger.info("Loaded " + start + " lines into MegaHal.");

        if (cachedConfig.getDebug()) {
            BotLogUtil.sendInfo(logger, manager, Message.IMPORT__END);
        }
    }

    public static boolean canLearn(@NotNull User user) { return Boolean.TRUE.equals(CollectionProvider.getCanLearnCache().get(user.getIdLong())); }

    public static boolean canLearn(@NotNull Member member) { return Boolean.TRUE.equals(CollectionProvider.getCanLearnCache().get(member.getIdLong())); }

    public static void learn(@NotNull CachedConfig cachedConfig, @NotNull String sentence) {
        boolean learned = false;
        for (StorageService service : cachedConfig.getStorage()) {
            MessageModel m = service.getMessageModel(sentence);
            if (m != null) {
                learned = true;
            } else {
                m = new MessageModel();
                m.setMessage(sentence);
                service.storeModel(m);
            }
        }

        if (!learned) {
            cachedConfig.getMegaHal().add(sentence);

            MessagePacket packet = new MessagePacket();
            packet.setMessage(sentence);
            PacketUtil.queuePacket(packet);
        }
    }

    public static void learnAll(@NotNull CachedConfig cachedConfig, @NotNull Collection<String> sentences) { learnAll(cachedConfig, sentences.toArray(new String[0])); }

    public static void learnAll(@NotNull CachedConfig cachedConfig, @NotNull String[] sentences) {
        MarkovMegaHal megaHal = cachedConfig.getMegaHal();

        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) {
                continue;
            }

            boolean learned = false;
            for (StorageService service : cachedConfig.getStorage()) {
                MessageModel m = service.getMessageModel(sentence);
                if (m != null) {
                    learned = true;
                } else {
                    m = new MessageModel();
                    m.setMessage(sentence);
                    service.storeModel(m);
                }
            }

            if (!learned) {
                megaHal.add(sentence);

                MessagePacket packet = new MessagePacket();
                packet.setMessage(sentence);
                PacketUtil.queuePacket(packet);
            }
        }
    }
}
