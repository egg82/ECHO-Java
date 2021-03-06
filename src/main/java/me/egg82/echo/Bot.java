package me.egg82.echo;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.JDACommandManager;
import co.aikar.commands.JDALocales;
import co.aikar.commands.MessageType;
import co.aikar.locales.MessageKey;
import io.paradaux.ai.MarkovMegaHal;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import joptsimple.OptionSet;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.config.ConfigurationFileUtil;
import me.egg82.echo.events.ChatEvents;
import me.egg82.echo.events.EventHolder;
import me.egg82.echo.events.ReactEvents;
import me.egg82.echo.events.ReplyEvents;
import me.egg82.echo.lang.LanguageFileUtil;
import me.egg82.echo.lang.Message;
import me.egg82.echo.logging.AnsiColor;
import me.egg82.echo.messaging.GenericMessagingHandler;
import me.egg82.echo.messaging.MessagingService;
import me.egg82.echo.services.CollectionProvider;
import me.egg82.echo.storage.StorageService;
import me.egg82.echo.tasks.BotStatusTask;
import me.egg82.echo.tasks.TaskScheduler;
import me.egg82.echo.utils.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import ninja.egg82.events.JDAEventSubscriber;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Bot {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JDACommandManager commandManager;

    private final List<EventHolder> eventHolders = new ArrayList<>();
    private final List<JDAEventSubscriber<?>> events = new ArrayList<>();
    private final List<BaseCommand> commands = new ArrayList<>();
    private final IntList tasks = new IntArrayList();

    private final JDA jda;

    public Bot(@NotNull OptionSet options, @NotNull String version) throws LoginException {
        JDABuilder builder = JDABuilder.createDefault((String) options.valueOf("token"));
        builder.setChunkingFilter(ChunkingFilter.ALL);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.enableCache(CacheFlag.EMOTE);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.setActivity(Activity.watching("you"));
        jda = builder.build();

        commandManager = new JDACommandManager(jda);
        commandManager.enableUnstableAPI("help");

        setChatColors();

        loadServices();
        loadLanguages();
        ResponseUtil.loadMegaHal(commandManager);
        loadCommands();
        loadEvents();
        loadTasks();

        int numEvents = events.size();
        for (EventHolder eventHolder : eventHolders) {
            numEvents += eventHolder.numEvents();
        }

        BotLogUtil.sendInfo(logger, commandManager, Message.GENERAL__ENABLED);
        BotLogUtil.sendInfo(logger, commandManager, Message.GENERAL__LOAD,
                            "{version}", version,
                            "{commands}", String.valueOf(commandManager.getRegisteredRootCommands().size()),
                            "{events}", String.valueOf(numEvents),
                            "{tasks}", String.valueOf(tasks.size())
        );
    }

    public void destroy() {
        for (BaseCommand command : commands) {
            commandManager.unregisterCommand(command);
        }

        for (int task : tasks) {
            TaskScheduler.cancelTask(task);
        }
        tasks.clear();

        for (EventHolder eventHolder : eventHolders) {
            eventHolder.cancel();
        }
        eventHolders.clear();
        for (JDAEventSubscriber<?> event : events) {
            event.cancel();
        }
        events.clear();

        unloadServices();

        BotLogUtil.sendInfo(logger, commandManager, Message.GENERAL__DISABLED);
    }

    private void loadLanguages() {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            throw new RuntimeException("CachedConfig seems to be null.");
        }

        JDALocales locales = (JDALocales) commandManager.getLocales();

        try {
            for (Locale locale : Locale.getAvailableLocales()) {
                Optional<File> localeFile = LanguageFileUtil.getLanguage(FileUtil.getCwd(), locale);
                if (localeFile.isPresent()) {
                    commandManager.addSupportedLanguage(locale);
                    loadYamlLanguageFile(locales, localeFile.get(), locale);
                }
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }

        locales.loadLanguages();
        locales.setDefaultLocale(cachedConfig.getLanguage());
        commandManager.usePerIssuerLocale(true);

        setChatColors();
    }

    private void setChatColors() {
        commandManager.setFormat(
                MessageType.ERROR,
                AnsiColor.RED.toString(),
                AnsiColor.BRIGHT_YELLOW.toString(),
                AnsiColor.BRIGHT_BLUE.toString(),
                AnsiColor.WHITE.toString()
        );
        commandManager.setFormat(
                MessageType.INFO,
                AnsiColor.WHITE.toString(),
                AnsiColor.BRIGHT_YELLOW.toString(),
                AnsiColor.BRIGHT_BLUE.toString(),
                AnsiColor.GREEN.toString(),
                AnsiColor.BRIGHT_RED.toString(),
                AnsiColor.YELLOW.toString(),
                AnsiColor.BLUE.toString(),
                AnsiColor.NORMAL.toString(),
                AnsiColor.RED.toString()
        );
    }

    private void loadServices() {
        ConfigurationFileUtil.reloadConfig(FileUtil.getCwd(), commandManager, new GenericMessagingHandler(), new MarkovMegaHal());
    }

    private void loadCommands() {
        commands.addAll(CollectionProvider.getCommands(commandManager));
        for (BaseCommand command : commands) {
            commandManager.registerCommand(command);
        }
    }

    private void loadEvents() {
        eventHolders.add(new ChatEvents(jda, commandManager));
        eventHolders.add(new ReactEvents(jda, commandManager));
        eventHolders.add(new ReplyEvents(jda, commandManager));
    }

    private void loadTasks() {
        tasks.add(TaskScheduler.createRepeatingTask(
                new BotStatusTask(jda, tasks),
                new TimeUtil.Time(10L, TimeUnit.SECONDS),
                new TimeUtil.Time(10L, TimeUnit.MINUTES)
        ));

        tasks.add(TaskScheduler.createRepeatingTask(PacketUtil::trySendQueue, new TimeUtil.Time(1L, TimeUnit.SECONDS), new TimeUtil.Time(1L, TimeUnit.SECONDS)));
    }

    public void unloadServices() {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig != null) {
            for (MessagingService service : cachedConfig.getMessaging()) {
                service.close();
            }
            for (StorageService service : cachedConfig.getStorage()) {
                service.close();
            }
        }
    }

    public boolean loadYamlLanguageFile(@NotNull JDALocales locales, @NotNull File file, @NotNull Locale locale) throws IOException {
        ConfigurationLoader<CommentedConfigurationNode> fileLoader = YamlConfigurationLoader.builder().nodeStyle(NodeStyle.BLOCK).indent(2).file(file).build();
        return loadLanguage(locales, fileLoader.load(), locale);
    }

    private boolean loadLanguage(@NotNull JDALocales locales, @NotNull CommentedConfigurationNode config, @NotNull Locale locale) {
        boolean loaded = false;
        for (Map.Entry<Object, CommentedConfigurationNode> kvp : config.childrenMap().entrySet()) {
            for (Map.Entry<Object, CommentedConfigurationNode> kvp2 : kvp.getValue().childrenMap().entrySet()) {
                String value = kvp2.getValue().getString();
                if (value != null && !value.isEmpty()) {
                    locales.addMessage(locale, MessageKey.of(kvp.getKey() + "." + kvp2.getKey()), value);
                    loaded = true;
                }
            }
        }
        return loaded;
    }
}
