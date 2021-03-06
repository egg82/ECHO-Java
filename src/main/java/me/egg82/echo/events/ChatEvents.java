package me.egg82.echo.events;

import co.aikar.commands.JDACommandManager;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import io.paradaux.ai.MarkovMegaHal;
import me.egg82.echo.commands.GoogleSearchCommand;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.messaging.packets.MessageUpdatePacket;
import me.egg82.echo.storage.StorageService;
import me.egg82.echo.storage.models.MessageModel;
import me.egg82.echo.utils.JDAUtil;
import me.egg82.echo.utils.PacketUtil;
import me.egg82.echo.utils.ResponseUtil;
import me.egg82.echo.web.models.GoogleSearchModel;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import ninja.egg82.events.JDAEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatEvents extends EventHolder {
    private final JDA jda;
    private final JDACommandManager manager;

    private final Random rand = new Random();

    private static final Pattern RE_SPACE = Pattern.compile("[\\s\\t]+");
    private static final Pattern RE_NOT_WORD = Pattern.compile("[^\\w]");
    private static final Pattern RE_URL = Pattern.compile("<url>");

    private static final StanfordCoreNLP tripletNlp = new StanfordCoreNLP(PropertiesUtils.asProperties(
            "annotators", "tokenize,ssplit,pos,lemma,ner,depparse,natlog,openie"
    ));

    private final Cache<Long, String> oldMessages = Caffeine.newBuilder()
            .expireAfterWrite(1L, TimeUnit.HOURS)
            .expireAfterAccess(30L, TimeUnit.MINUTES)
            .build();

    public ChatEvents(@NotNull JDA jda, @NotNull JDACommandManager manager) {
        this.jda = jda;
        this.manager = manager;

        events.add(JDAEvents.subscribe(jda, GuildMessageReceivedEvent.class)
                           .filter(e -> !e.getAuthor().isBot())
                           .filter(e -> !e.isWebhookMessage())
                           .filter(e -> {
                               CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
                               if (cachedConfig == null) {
                                   logger.error("Could not get cached config.");
                                   return false;
                               }
                               return !JDAUtil.isCommand(cachedConfig, e.getMessage().getContentRaw());
                           })
                           .filter(e -> {
                               boolean retVal = ResponseUtil.canLearn(e.getAuthor());
                               if (!retVal && ConfigUtil.getDebugOrFalse()) {
                                   logger.info("Not learning from " + e.getAuthor().getAsTag());
                               }
                               return retVal;
                           })
                           .handler(this::learn));

        events.add(JDAEvents.subscribe(jda, MessageReceivedEvent.class)
                           .filter(e -> !e.getAuthor().isBot())
                           .filter(e -> !e.isWebhookMessage())
                           .filter(e -> {
                               CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
                               if (cachedConfig == null) {
                                   logger.error("Could not get cached config.");
                                   return false;
                               }
                               return !JDAUtil.isCommand(cachedConfig, e.getMessage().getContentRaw());
                           })
                           .handler(this::speak));

        events.add(JDAEvents.subscribe(jda, GuildMessageUpdateEvent.class)
                           .filter(e -> !e.getAuthor().isBot())
                           .filter(e -> {
                               CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
                               if (cachedConfig == null) {
                                   logger.error("Could not get cached config.");
                                   return false;
                               }
                               return !JDAUtil.isCommand(cachedConfig, e.getMessage().getContentRaw());
                           })
                           .filter(e -> {
                               boolean retVal = ResponseUtil.canLearn(e.getAuthor());
                               if (!retVal && ConfigUtil.getDebugOrFalse()) {
                                   logger.info("Not learning from " + e.getAuthor().getAsTag());
                               }
                               return retVal;
                           })
                           .handler(this::replace));
    }

    private void learn(@NotNull GuildMessageReceivedEvent event) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return;
        }

        oldMessages.put(event.getMessageIdLong(), event.getMessage().getContentStripped());

        ResponseUtil.learn(cachedConfig, event.getMessage().getContentStripped());
    }

    private void speak(@NotNull MessageReceivedEvent event) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return;
        }

        boolean contains = event.getMessage().isMentioned(jda.getSelfUser());
        if (!contains) {
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

        if (event.isFromGuild() && !contains && rand.nextDouble() > cachedConfig.getReplyChance()) {
            return;
        }

        String seed = reversed ? getSeed(reverse(event.getMessage().getContentStripped())) : getSeed(event.getMessage().getContentStripped());
        if (seed == null) {
            return;
        }

        if (cachedConfig.getDebug()) {
            logger.info("Got seed: " + seed);
        }

        String message;
        if (reversed) {
            message = reverse(generateSentence(cachedConfig.getMegaHal(), cachedConfig.getGoogleKey(), reverse(event.getMessage().getContentStripped()), seed));
        } else {
            message = generateSentence(cachedConfig.getMegaHal(), cachedConfig.getGoogleKey(), event.getMessage().getContentStripped(), seed);
        }

        if (!message.isEmpty()) {
            event.getChannel().sendMessage(message).queue();
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

        //megaHal.remove(old); // TODO: Add MegaHal removal once that becomes a thing in the library
        ResponseUtil.learn(cachedConfig, event.getMessage().getContentStripped());

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
        Annotation doc = new Annotation(message);
        tripletNlp.annotate(doc);

        List<String> options = new ArrayList<>();

        List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
            for (RelationTriple triple : triples) {
                String subject = triple.subjectLemmaGloss().trim();
                if (!subject.isEmpty()) {
                    options.add(swapSubjects(subject));
                }
                String object = triple.objectLemmaGloss().trim();
                if (!object.isEmpty()) {
                    options.add(object);
                }
            }
        }

        if (!options.isEmpty()) {
            return options.get(rand.nextInt(options.size()));
        }

        // Fallback
        List<String> words = new ArrayList<>(Arrays.asList(RE_SPACE.split(message)));
        for (int i = 0; i < words.size(); i++) {
            words.set(i, RE_NOT_WORD.matcher(words.get(i)).replaceAll(""));
        }
        words.removeIf(word -> word.length() < 4);
        return words.isEmpty() ? null : words.get(rand.nextInt(words.size()));
    }

    private @NotNull String swapSubjects(@NotNull String subject) {
        if (subject.equalsIgnoreCase("you")) {
            return "I";
        } else if (subject.equalsIgnoreCase("i") || subject.equalsIgnoreCase("me")) {
            return "you";
        }
        return subject;
    }

    private @NotNull String generateSentence(@NotNull MarkovMegaHal megaHal, @NotNull String googleKey, @NotNull String sentence, @NotNull String seed) {
        Set<String> previousUrls = new HashSet<>();

        String retVal = megaHal.getSentence(seed);
        if (googleKey.isEmpty()) {
            return RE_URL.matcher(retVal).replaceAll("");
        } else {
            return RE_URL.matcher(retVal).replaceAll(v -> {
                String s = rand.nextDouble() >= 0.5 ? getSeed(sentence) : getSeed(retVal);
                if (s == null) {
                    s = seed;
                }
                try {
                    List<GoogleSearchModel.GoogleSearchItemModel> items = GoogleSearchCommand.getModel(googleKey, s).get().getItems();
                    for (GoogleSearchModel.GoogleSearchItemModel item : items) {
                        if (previousUrls.add(item.getLink())) {
                            return Matcher.quoteReplacement(item.getLink());
                        }
                    }
                } catch (ExecutionException ex) {
                    if (ConfigUtil.getDebugOrFalse()) {
                        logger.error(ex.getMessage(), ex);
                    } else {
                        logger.error(ex.getMessage());
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                return "";
            });
        }
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
