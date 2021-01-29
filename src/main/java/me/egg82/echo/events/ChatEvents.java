package me.egg82.echo.events;

import co.aikar.commands.JDACommandManager;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.paradaux.ai.MarkovMegaHal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.messaging.packets.MessagePacket;
import me.egg82.echo.messaging.packets.MessageUpdatePacket;
import me.egg82.echo.storage.StorageService;
import me.egg82.echo.storage.models.MessageModel;
import me.egg82.echo.utils.PacketUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import ninja.egg82.events.JDAEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatEvents extends EventHolder {
    private final JDA jda;
    private final JDACommandManager manager;

    private final Random rand = new Random();

    private final Pattern RE_SPACE = Pattern.compile("[\\s\\t]+");
    private final Pattern RE_NOT_WORD = Pattern.compile("[^\\w]");

    private final Cache<Long, String> oldMessages = Caffeine.newBuilder().expireAfterWrite(20L, TimeUnit.MINUTES).expireAfterAccess(5L, TimeUnit.MINUTES).build();

    public ChatEvents(@NotNull JDA jda, @NotNull JDACommandManager manager) {
        this.jda = jda;
        this.manager = manager;

        events.add(JDAEvents.subscribe(jda, GuildMessageReceivedEvent.class)
                .filter(e -> !e.getAuthor().isBot())
                .filter(e -> !e.isWebhookMessage())
                .filter(e -> !e.getMessage().getContentStripped().startsWith("!"))
                .handler(this::learn));

        events.add(JDAEvents.subscribe(jda, GuildMessageReceivedEvent.class)
                .filter(e -> !e.getAuthor().isBot())
                .filter(e -> !e.isWebhookMessage())
                .filter(e -> !e.getMessage().getContentStripped().startsWith("!"))
                .handler(this::speak));

        events.add(JDAEvents.subscribe(jda, GuildMessageUpdateEvent.class)
                .filter(e -> !e.getAuthor().isBot())
                .filter(e -> !e.getMessage().getContentStripped().startsWith("!"))
                .handler(this::replace));

        events.add(JDAEvents.subscribe(jda, GuildMessageReceivedEvent.class)
                .filter(e -> containsWord(e.getMessage().getContentStripped(), "alot"))
                .handler(this::reactAlot));
    }

    private void learn(@NotNull GuildMessageReceivedEvent event) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return;
        }

        oldMessages.put(event.getMessageIdLong(), event.getMessage().getContentStripped());

        cachedConfig.getMegaHal().add(event.getMessage().getContentStripped());

        for (StorageService service : cachedConfig.getStorage()) {
            service.getOrCreateMessageModel(event.getMessage().getContentStripped());
        }

        MessagePacket packet = new MessagePacket();
        packet.setMessage(event.getMessage().getContentStripped());
        PacketUtil.queuePacket(packet);
    }

    private void speak(@NotNull GuildMessageReceivedEvent event) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return;
        }

        boolean contains = false;
        for (String phrase : cachedConfig.getReplyPhrases()) {
            if (RE_SPACE.matcher(phrase).find()) {
                if (event.getMessage().getContentStripped().toLowerCase().contains(phrase)) {
                    contains = true;
                    break;
                }
            } else {
                if (containsWord(event.getMessage().getContentStripped(), phrase)) {
                    contains = true;
                    break;
                }
            }
        }

        boolean reversed = false;
        if (!contains) {
            for (String phrase : cachedConfig.getReplyPhrasesReversed()) {
                if (RE_SPACE.matcher(phrase).find()) {
                    if (event.getMessage().getContentStripped().toLowerCase().contains(phrase)) {
                        contains = true;
                        reversed = true;
                        break;
                    }
                } else {
                    if (containsWord(event.getMessage().getContentStripped(), phrase)) {
                        contains = true;
                        reversed = true;
                        break;
                    }
                }
            }
        }

        if (!contains && rand.nextDouble() > cachedConfig.getReplyChance()) {
            return;
        }

        String seed = getSeed(event.getMessage().getContentStripped());
        if (seed == null) {
            return;
        }

        if (cachedConfig.getDebug()) {
            logger.info("Got seed: " + seed);
        }

        if (reversed) {
            event.getChannel().sendMessage(reverse(cachedConfig.getMegaHal().getSentence(reverse(seed)))).queue();
        } else {
            event.getChannel().sendMessage(cachedConfig.getMegaHal().getSentence(seed)).queue();
        }
    }

    private void replace(@NotNull GuildMessageUpdateEvent event) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return;
        }

        String old = oldMessages.getIfPresent(event.getMessageIdLong());
        if (old == null) {
            logger.warn("Could not get old message for new edited message: " + event.getMessageId());
            return;
        }

        oldMessages.put(event.getMessageIdLong(), event.getMessage().getContentStripped());

        MarkovMegaHal megaHal = cachedConfig.getMegaHal();
        //megaHal.remove(old); // TODO: Add MegaHal removal once that becomes a thing in the library
        megaHal.add(event.getMessage().getContentStripped());

        for (StorageService service : cachedConfig.getStorage()) {
            MessageModel model = service.getOrCreateMessageModel(old);
            model.setMessage(event.getMessage().getContentStripped());
            service.storeModel(model);
        }

        MessageUpdatePacket packet = new MessageUpdatePacket();
        packet.setOldMessage(old);
        packet.setNewMessage(event.getMessage().getContentStripped());
        PacketUtil.queuePacket(packet);
    }

    private @Nullable String getSeed(@NotNull String message) {
        List<String> words = new ArrayList<>(Arrays.asList(RE_SPACE.split(message)));
        for (int i = 0; i < words.size(); i++) {
            words.set(i, RE_NOT_WORD.matcher(words.get(i)).replaceAll(""));
        }
        words.removeIf(word -> word.length() < 4);

        return words.isEmpty() ? null : words.get(rand.nextInt(words.size()));
    }

    private void reactAlot(@NotNull GuildMessageReceivedEvent event) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return;
        }

        List<Emote> emotes = event.getGuild().getEmotesByName(cachedConfig.getAlotEmote(), true);
        if (emotes.isEmpty()) {
            logger.warn("Could not find alot emote \"" + cachedConfig.getAlotEmote() + "\" for guild.");
            return;
        }

        event.getMessage().addReaction(emotes.get(0)).queue();
    }

    private boolean containsWord(@NotNull String content, @NotNull String word) {
        String[] split = RE_SPACE.split(content.toLowerCase());
        for (String w : split) {
            w = RE_NOT_WORD.matcher(w).replaceAll("");
            if (word.equalsIgnoreCase(w)) {
                return true;
            }
        }
        return false;
    }

    private @NotNull String reverse(@NotNull String input) {
        StringBuilder builder = new StringBuilder();
        builder.append(input);
        builder.reverse();
        return builder.toString();
    }
}
