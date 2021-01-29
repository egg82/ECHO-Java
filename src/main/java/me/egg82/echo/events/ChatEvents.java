package me.egg82.echo.events;

import co.aikar.commands.JDACommandManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.messaging.packets.MessagePacket;
import me.egg82.echo.storage.StorageService;
import me.egg82.echo.utils.PacketUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import ninja.egg82.events.JDAEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatEvents extends EventHolder {
    private final JDA jda;
    private final JDACommandManager manager;

    private final Random rand = new Random();

    private final Pattern RE_SPACE = Pattern.compile("[\\s\\t]+");
    private final Pattern RE_NOT_WORD = Pattern.compile("[^\\w]");

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
                .filter(e -> rand.nextDouble() <= 0.15)
                .handler(this::speak));

        events.add(JDAEvents.subscribe(jda, GuildMessageReceivedEvent.class)
                .filter(this::isAlot)
                .handler(this::reactAlot));
    }

    private void learn(@NotNull GuildMessageReceivedEvent event) {
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

    private void speak(@NotNull GuildMessageReceivedEvent event) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return;
        }

        String seed  =getSeed(event.getMessage().getContentStripped());
        if (seed == null) {
            return;
        }

        event.getChannel().sendMessage(cachedConfig.getMegaHal().getSentence(seed)).queue();
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
        List<Emote> emotes = event.getGuild().getEmotesByName("alot_2", true);
        if (emotes.isEmpty()) {
            emotes = event.getGuild().getEmotesByName("alot", true);
            if (emotes.isEmpty()) {
                logger.warn("Could not find alot emote for guild.");
                return;
            }
        }

        event.getMessage().addReaction(emotes.get(0)).queue();
    }

    private boolean isAlot(@NotNull GuildMessageReceivedEvent event) {
        String[] split = RE_SPACE.split(event.getMessage().getContentStripped().toLowerCase());
        for (String word : split) {
            word = RE_NOT_WORD.matcher(word).replaceAll("");
            if (word.equalsIgnoreCase("alot")) {
                return true;
            }
        }
        return false;
    }
}
