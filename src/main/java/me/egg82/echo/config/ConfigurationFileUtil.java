package me.egg82.echo.config;

import co.aikar.commands.JDACommandManager;
import io.paradaux.ai.MarkovMegaHal;
import me.egg82.echo.core.GameStatus;
import me.egg82.echo.messaging.*;
import me.egg82.echo.storage.*;
import me.egg82.echo.utils.BotLogUtil;
import me.egg82.echo.utils.LogUtil;
import me.egg82.echo.utils.PacketUtil;
import me.egg82.echo.utils.TimeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import redis.clients.jedis.exceptions.JedisException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConfigurationFileUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationFileUtil.class);

    private ConfigurationFileUtil() { }

    public static void reloadConfig(
            @NotNull File dataDirectory,
            @NotNull JDACommandManager manager,
            @NotNull MessagingHandler messagingHandler,
            @NotNull MarkovMegaHal megaHal
    ) {
        ConfigurationNode config;
        try {
            config = getConfig("config.yml", new File(dataDirectory, "config.yml"));
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        boolean debug = config.node("debug").getBoolean(false);
        if (!debug) {
            Reflections.log = null;
        }
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Debug</c2> <c1>enabled</c1>");
        }

        UUID serverId = ServerIDUtil.getId(new File(dataDirectory, "server-id.txt"));
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Server ID:</c2> <c1>" + serverId.toString() + "</c1>");
        }

        Set<String> commandPrefixes = getCommandPrefixes(config, debug, manager);
        List<String> prefixes = manager.getDefaultConfig().getCommandPrefixes();
        prefixes.clear();
        prefixes.addAll(commandPrefixes);

        String googleKey = config.node("keys", "google").getString("");
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Google API key:</c2> <c1>" + googleKey + "</c1>");
        }

        String wolframKey = config.node("keys", "wolfram").getString("");
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Wolfram Alpha app ID:</c2> <c1>" + wolframKey + "</c1>");
        }

        String imgurKey = config.node("keys", "imgur").getString("");
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Imgur client ID:</c2> <c1>" + imgurKey + "</c1>");
        }

        String deepAiKey = config.node("keys", "deepai").getString("");
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>DeepAI key:</c2> <c1>" + deepAiKey + "</c1>");
        }

        String extractorKey = config.node("keys", "extractor").getString("");
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Extractor key:</c2> <c1>" + extractorKey + "</c1>");
        }

        String traktKey = config.node("keys", "trakt").getString("");
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Trakt key:</c2> <c1>" + traktKey + "</c1>");
        }

        String adminRole = config.node("roles", "admin").getString("owner");
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Admin role:</c2> <c1>" + adminRole + "</c1>");
        }

        String disallowedRole = config.node("roles", "disallowed").getString("");
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Disallowed role:</c2> <c1>" + disallowedRole + "</c1>");
        }

        String alotEmote = config.node("emotes", "alot").getString("");
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Alot emote:</c2> <c1>" + alotEmote + "</c1>");
        }

        String disallowedEmote = config.node("emotes", "disallowed").getString("");
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Disallowed emote:</c2> <c1>" + disallowedEmote + "</c1>");
        }

        double replyChance = config.node("chat", "random").getDouble(0.15d);
        replyChance = Math.max(0.0d, Math.min(1.0d, replyChance));
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Reply chance:</c2> <c1>" + replyChance + "</c1>");
        }

        CachedConfig cachedConfig = CachedConfig.builder()
                .debug(debug)
                .language(getLanguage(config, debug, manager))
                .megaHal(megaHal)
                .storage(getStorage(config, dataDirectory, debug, manager))
                .messaging(getMessaging(config, serverId, messagingHandler, debug, manager))
                .serverId(serverId)
                .commandPrefixes(commandPrefixes)
                .googleKey(googleKey)
                .wolframKey(wolframKey)
                .imgurKey(imgurKey)
                .deepAiKey(deepAiKey)
                .extractorKey(extractorKey)
                .traktKey(traktKey)
                .adminRole(adminRole)
                .disallowedRole(disallowedRole)
                .alotEmote(alotEmote)
                .disallowedEmote(disallowedEmote)
                .disabledCommands(getDisabledCommands(config, debug, manager))
                .replyChance(replyChance)
                .replyPhrases(getReplyPhrases(config, debug, manager))
                .laziness(getLaziness(config, debug, manager))
                .games(getGames(config, debug, manager))
                .build();

        PacketUtil.setPoolSize(cachedConfig.getMessaging().size() + 1);

        ConfigUtil.setConfiguration(config, cachedConfig);
    }

    private static @NotNull Locale getLanguage(@NotNull ConfigurationNode config, boolean debug, @NotNull JDACommandManager manager) {
        String configLanguage = config.node("lang").getString("en");
        Locale retVal = null;
        for (Locale locale : Locale.getAvailableLocales()) {
            String l = locale.getCountry() == null || locale.getCountry().isEmpty() ? locale.getLanguage() : locale.getLanguage() + "-" + locale.getCountry();
            if (locale.getLanguage().equalsIgnoreCase(configLanguage) || l.equalsIgnoreCase(configLanguage)) {
                retVal = locale;
                break;
            }
        }

        if (retVal == null) {
            retVal = Locale.ENGLISH;
            BotLogUtil.sendInfo(
                    logger,
                    manager,
                    LogUtil.HEADING + "<c9>lang</c9> <c1>" + configLanguage + "</c1> <c9>is not a valid language. Using default value of</c9> <c1>" + (retVal.getCountry() == null || retVal
                            .getCountry()
                            .isEmpty() ? retVal.getLanguage() : retVal.getLanguage() + "-" + retVal.getCountry()) + "</c1>"
            );
        }
        if (debug) {
            BotLogUtil.sendInfo(
                    logger,
                    manager,
                    LogUtil.HEADING + "<c2>Default language:</c2> <c1>" + (retVal.getCountry() == null || retVal.getCountry().isEmpty()
                                                                           ? retVal.getLanguage()
                                                                           : retVal.getLanguage() + "-" + retVal.getCountry()) + "</c1>"
            );
        }

        return retVal;
    }

    private static @NotNull Set<String> getCommandPrefixes(@NotNull ConfigurationNode config, boolean debug, @NotNull JDACommandManager manager) {
        Set<String> retVal;
        try {
            retVal = new HashSet<>(!config.node("prefixes").empty() ? config.node("prefixes").getList(String.class) : new ArrayList<>());
        } catch (SerializationException ex) {
            logger.error(ex.getMessage(), ex);
            retVal = new HashSet<>();
        }

        for (String prefix : retVal) {
            if (debug) {
                BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Adding command prefix:</c2> <c1>" + prefix + "</c1>");
            }
        }

        return retVal;
    }

    private static @NotNull List<StorageService> getStorage(
            @NotNull ConfigurationNode config,
            @NotNull File dataDirectory,
            boolean debug,
            @NotNull JDACommandManager manager
    ) {
        List<StorageService> retVal = new ArrayList<>();

        PoolSettings poolSettings = new PoolSettings(config.node("storage", "settings"));
        for (Map.Entry<Object, ? extends ConfigurationNode> kvp : config.node("storage", "engines").childrenMap().entrySet()) {
            StorageService service = getStorageOf((String) kvp.getKey(), kvp.getValue(), dataDirectory, poolSettings, debug, manager);
            if (service == null) {
                continue;
            }

            if (debug) {
                BotLogUtil.sendInfo(
                        logger,
                        manager,
                        LogUtil.HEADING + "<c2>Added storage:</c2> <c1>" + service.getName() + " (" + service.getClass().getSimpleName() + ")</c1>"
                );
            }
            retVal.add(service);
        }

        return retVal;
    }

    private static @Nullable StorageService getStorageOf(
            @NotNull String name,
            @NotNull ConfigurationNode engineNode,
            @NotNull File dataDirectory,
            @NotNull PoolSettings poolSettings,
            boolean debug,
            @NotNull JDACommandManager manager
    ) {
        if (!engineNode.node("enabled").getBoolean()) {
            if (debug) {
                BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c9>Storage engine</c9> <c1>" + name + "</c1> <c9>is disabled. Removing.</c9>");
            }
            return null;
        }

        String type = engineNode.node("type").getString("").toLowerCase();
        ConfigurationNode connectionNode = engineNode.node("connection");
        switch (type) {
            case "old_mysql": {
                AddressPort url = new AddressPort(connectionNode.key() + ".address", connectionNode.node("address").getString("127.0.0.1:3306"), 3306);
                if (debug) {
                    BotLogUtil.sendInfo(
                            logger,
                            manager,
                            LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type old_mysql with address</c2> <c1>" + url.getAddress() + ":" + url
                                    .getPort() + "/" + connectionNode.node("database").getString("echo") + "</c1>"
                    );
                }
                String options = connectionNode.node("options").getString("useSSL=false&useUnicode=true&characterEncoding=utf8");
                if (options.length() > 0 && options.charAt(0) == '?') {
                    options = options.substring(1);
                }
                if (debug) {
                    BotLogUtil.sendInfo(
                            logger,
                            manager,
                            LogUtil.HEADING + "<c2>Setting options for engine</c2> <c1>" + name + "</c1> <c2>to</c2> <c1>" + options.replace(
                                    "&",
                                    "&\\"
                            ) + "</c1>"
                    );
                }
                try {
                    return MySQL55StorageService.builder(name)
                            .url(url.address, url.port, connectionNode.node("database").getString("anti_vpn"))
                            .credentials(connectionNode.node("username").getString(""), connectionNode.node("password").getString(""))
                            .options(options)
                            .poolSize(poolSettings.minPoolSize, poolSettings.maxPoolSize)
                            .life(poolSettings.maxLifetime, poolSettings.timeout)
                            .build();
                } catch (Exception ex) {
                    logger.error("Could not create engine \"" + name + "\".", ex);
                }
                break;
            }
            case "mysql": {
                AddressPort url = new AddressPort(connectionNode.key() + ".address", connectionNode.node("address").getString("127.0.0.1:3306"), 3306);
                if (debug) {
                    BotLogUtil.sendInfo(
                            logger,
                            manager,
                            LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type mysql with address</c2> <c1>" + url.getAddress() + ":" + url.getPort() + "/" + connectionNode.node("database")
                                    .getString("echo") + "</c1>"
                    );
                }
                String options = connectionNode.node("options").getString("useSSL=false&useUnicode=true&characterEncoding=utf8");
                if (options.length() > 0 && options.charAt(0) == '?') {
                    options = options.substring(1);
                }
                if (debug) {
                    BotLogUtil.sendInfo(
                            logger,
                            manager,
                            LogUtil.HEADING + "<c2>Setting options for engine</c2> <c1>" + name + "</c1> <c2>to</c2> <c1>" + options.replace(
                                    "&",
                                    "&\\"
                            ) + "</c1>"
                    );
                }
                try {
                    return MySQLStorageService.builder(name)
                            .url(url.address, url.port, connectionNode.node("database").getString("anti_vpn"))
                            .credentials(connectionNode.node("username").getString(""), connectionNode.node("password").getString(""))
                            .options(options)
                            .poolSize(poolSettings.minPoolSize, poolSettings.maxPoolSize)
                            .life(poolSettings.maxLifetime, poolSettings.timeout)
                            .build();
                } catch (Exception ex) {
                    logger.error("Could not create engine \"" + name + "\".", ex);
                }
                break;
            }
            case "mariadb": {
                AddressPort url = new AddressPort(connectionNode.key() + ".address", connectionNode.node("address").getString("127.0.0.1:3306"), 3306);
                if (debug) {
                    BotLogUtil.sendInfo(
                            logger,
                            manager,
                            LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type mariadb with address</c2> <c1>" + url.getAddress() + ":" + url.getPort() + "/" + connectionNode.node("database")
                                    .getString("echo") + "</c1>"
                    );
                }
                String options = connectionNode.node("options").getString("useSSL=false&useUnicode=true&characterEncoding=utf8");
                if (options.length() > 0 && options.charAt(0) == '?') {
                    options = options.substring(1);
                }
                if (debug) {
                    BotLogUtil.sendInfo(
                            logger,
                            manager,
                            LogUtil.HEADING + "<c2>Setting options for engine</c2> <c1>" + name + "</c1> <c2>to</c2> <c1>" + options.replace(
                                    "&",
                                    "&\\"
                            ) + "</c1>"
                    );
                }
                try {
                    return MariaDBStorageService.builder(name)
                            .url(url.address, url.port, connectionNode.node("database").getString("anti_vpn"))
                            .credentials(connectionNode.node("username").getString(""), connectionNode.node("password").getString(""))
                            .options(options)
                            .poolSize(poolSettings.minPoolSize, poolSettings.maxPoolSize)
                            .life(poolSettings.maxLifetime, poolSettings.timeout)
                            .build();
                } catch (Exception ex) {
                    logger.error("Could not create engine \"" + name + "\".", ex);
                }
                break;
            }
            case "postgresql": {
                AddressPort url = new AddressPort(connectionNode.key() + ".address", connectionNode.node("address").getString("127.0.0.1:5432"), 5432);
                if (debug) {
                    BotLogUtil.sendInfo(
                            logger,
                            manager,
                            LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type postgresql with address</c2> <c1>" + url.getAddress() + ":" + url
                                    .getPort() + "/" + connectionNode.node("database").getString("echo") + "</c1>"
                    );
                }
                String options = connectionNode.node("options").getString("useSSL=false&useUnicode=true&characterEncoding=utf8");
                if (options.length() > 0 && options.charAt(0) == '?') {
                    options = options.substring(1);
                }
                if (debug) {
                    BotLogUtil.sendInfo(
                            logger,
                            manager,
                            LogUtil.HEADING + "<c2>Setting options for engine</c2> <c1>" + name + "</c1> <c2>to</c2> <c1>" + options.replace(
                                    "&",
                                    "&\\"
                            ) + "</c1>"
                    );
                }
                try {
                    return PostgreSQLStorageService.builder(name)
                            .url(url.address, url.port, connectionNode.node("database").getString("anti_vpn"))
                            .credentials(connectionNode.node("username").getString(""), connectionNode.node("password").getString(""))
                            .options(options)
                            .poolSize(poolSettings.minPoolSize, poolSettings.maxPoolSize)
                            .life(poolSettings.maxLifetime, poolSettings.timeout)
                            .build();
                } catch (Exception ex) {
                    logger.error("Could not create engine \"" + name + "\".", ex);
                }
                break;
            }
            case "h2": {
                if (debug) {
                    BotLogUtil.sendInfo(
                            logger,
                            manager,
                            LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type h2 with file</c2> <c1>" + connectionNode.node("file")
                                    .getString("echo") + "</c1>"
                    );
                }
                String options = connectionNode.node("options").getString("useUnicode=true&characterEncoding=utf8");
                if (options.length() > 0 && options.charAt(0) == '?') {
                    options = options.substring(1);
                }
                if (debug) {
                    BotLogUtil.sendInfo(
                            logger,
                            manager,
                            LogUtil.HEADING + "<c2>Setting options for engine</c2> <c1>" + name + "</c1> <c2>to</c2> <c1>" + options.replace(
                                    "&",
                                    "&\\"
                            ) + "</c1>"
                    );
                }
                try {
                    return H2StorageService.builder(name)
                            .file(new File(dataDirectory, connectionNode.node("file").getString("anti_vpn")))
                            .options(options)
                            .poolSize(poolSettings.minPoolSize, poolSettings.maxPoolSize)
                            .life(poolSettings.maxLifetime, poolSettings.timeout)
                            .build();
                } catch (Exception ex) {
                    logger.error("Could not create engine \"" + name + "\".", ex);
                }
                break;
            }
            case "sqlite": {
                if (debug) {
                    BotLogUtil.sendInfo(
                            logger,
                            manager,
                            LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type sqlite with file</c2> <c1>" + connectionNode.node("file")
                                    .getString("echo.db") + "</c1>"
                    );
                }
                String options = connectionNode.node("options").getString("useUnicode=true&characterEncoding=utf8");
                if (options.length() > 0 && options.charAt(0) == '?') {
                    options = options.substring(1);
                }
                if (debug) {
                    BotLogUtil.sendInfo(
                            logger,
                            manager,
                            LogUtil.HEADING + "<c2>Setting options for engine</c2> <c1>" + name + "</c1> <c2>to</c2> <c1>" + options.replace(
                                    "&",
                                    "&\\"
                            ) + "</c1>"
                    );
                }
                try {
                    return SQLiteStorageService.builder(name)
                            .file(new File(dataDirectory, connectionNode.node("file").getString("anti_vpn.db")))
                            .options(options)
                            .poolSize(poolSettings.minPoolSize, poolSettings.maxPoolSize)
                            .life(poolSettings.maxLifetime, poolSettings.timeout)
                            .build();
                } catch (Exception ex) {
                    logger.error("Could not create engine \"" + name + "\".", ex);
                }
                break;
            }
            default: {
                BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c9>Unknown storage type</c9> <c1>" + type + "</c1> <c9>in engine</c9> <c1>" + name + "</c1>");
                break;
            }
        }
        return null;
    }

    private static @NotNull List<MessagingService> getMessaging(
            @NotNull ConfigurationNode config,
            @NotNull UUID serverId,
            @NotNull MessagingHandler handler,
            boolean debug,
            @NotNull JDACommandManager manager
    ) {
        List<MessagingService> retVal = new ArrayList<>();

        PoolSettings poolSettings = new PoolSettings(config.node("messaging", "settings"));
        for (Map.Entry<Object, ? extends ConfigurationNode> kvp : config.node("messaging", "engines").childrenMap().entrySet()) {
            MessagingService service = getMessagingOf((String) kvp.getKey(), kvp.getValue(), serverId, handler, poolSettings, debug, manager);
            if (service == null) {
                continue;
            }

            if (debug) {
                BotLogUtil.sendInfo(
                        logger,
                        manager,
                        LogUtil.HEADING + "<c2>Added messaging:</c2> <c1>" + service.getName() + " (" + service.getClass().getSimpleName() + ")</c1>"
                );
            }
            retVal.add(service);
        }

        return retVal;
    }

    private static @Nullable MessagingService getMessagingOf(
            @NotNull String name,
            @NotNull ConfigurationNode engineNode,
            @NotNull UUID serverId,
            @NotNull MessagingHandler handler,
            @NotNull PoolSettings poolSettings,
            boolean debug,
            @NotNull JDACommandManager manager
    ) {
        if (!engineNode.node("enabled").getBoolean()) {
            if (debug) {
                BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c9>Messaging engine</c9> <c1>" + name + "</c1> <c9>is disabled. Removing.</c9>");
            }
            return null;
        }

        String type = engineNode.node("type").getString("").toLowerCase();
        ConfigurationNode connectionNode = engineNode.node("connection");
        switch (type) {
            case "rabbitmq": {
                AddressPort url = new AddressPort(connectionNode.key() + ".address", connectionNode.node("address").getString("127.0.0.1:5672"), 5672);
                if (debug) {
                    BotLogUtil.sendInfo(
                            logger,
                            manager,
                            LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type rabbitmq with address</c2> <c1>" + url.getAddress() + ":" + url.getPort() + connectionNode.node("v-host")
                                    .getString("/") + "</c1>"
                    );
                }
                try {
                    return RabbitMQMessagingService.builder(name, serverId, handler)
                            .url(url.address, url.port, connectionNode.node("v-host").getString("/"))
                            .credentials(connectionNode.node("username").getString("guest"), connectionNode.node("password").getString("guest"))
                            .timeout((int) poolSettings.timeout)
                            .build();
                } catch (IOException | TimeoutException ex) {
                    logger.error("Could not create engine \"" + name + "\".", ex);
                }
                break;
            }
            case "redis": {
                AddressPort url = new AddressPort(connectionNode.key() + ".address", connectionNode.node("address").getString("127.0.0.1:6379"), 6379);
                if (debug) {
                    BotLogUtil.sendInfo(
                            logger,
                            manager,
                            LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type redis with address</c2> <c1>" + url.getAddress() + ":" + url.getPort() + "</c1>"
                    );
                }
                try {
                    return RedisMessagingService.builder(name, serverId, handler)
                            .url(url.address, url.port)
                            .credentials(connectionNode.node("password").getString(""))
                            .poolSize(poolSettings.minPoolSize, poolSettings.maxPoolSize)
                            .life(poolSettings.maxLifetime, (int) poolSettings.timeout)
                            .build();
                } catch (JedisException ex) {
                    logger.error("Could not create engine \"" + name + "\".", ex);
                }
                break;
            }
            default: {
                BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c9>Unknown messaging type</c9> <c1>" + type + "</c1> <c9>in engine</c9> <c1>" + name + "</c1>");
                break;
            }
        }
        return null;
    }

    private static @NotNull Set<String> getDisabledCommands(@NotNull ConfigurationNode config, boolean debug, @NotNull JDACommandManager manager) {
        Set<String> retVal;
        try {
            retVal = new HashSet<>(!config.node("disabled-commands").empty() ? config.node("disabled-commands").getList(String.class) : new ArrayList<>());
        } catch (SerializationException ex) {
            logger.error(ex.getMessage(), ex);
            retVal = new HashSet<>();
        }

        for (String command : retVal) {
            if (debug) {
                BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Disabling command:</c2> <c1>" + command + "</c1>");
            }
        }

        return retVal;
    }

    private static @NotNull Set<String> getReplyPhrases(@NotNull ConfigurationNode config, boolean debug, @NotNull JDACommandManager manager) {
        Set<String> phrases;
        try {
            phrases = new HashSet<>(!config.node("chat", "respond").empty() ? config.node("chat", "respond").getList(String.class) : new ArrayList<>());
        } catch (SerializationException ex) {
            logger.error(ex.getMessage(), ex);
            phrases = new HashSet<>();
        }

        Set<String> retVal = new HashSet<>();
        for (String phrase : phrases) {
            retVal.add(phrase.toLowerCase());
        }

        for (String phrase : retVal) {
            if (debug) {
                BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Enabling reply phrase:</c2> <c1>" + phrase + "</c1>");
            }
        }

        return retVal;
    }

    private static double getLaziness(ConfigurationNode config, boolean debug, @NotNull JDACommandManager manager) {
        double retVal = config.node("laziness").getDouble(0.1d);
        retVal = Math.max(0.0d, Math.min(1.0d, retVal));

        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Laziness value:</c2> <c1>" + retVal + "</c1>");
        }

        return retVal;
    }

    private static @NotNull List<GameStatus> getGames(@NotNull ConfigurationNode config, boolean debug, @NotNull JDACommandManager manager) {
        List<GameStatus> retVal = new ArrayList<>();

        for (Map.Entry<Object, ? extends ConfigurationNode> kvp : config.node("games").childrenMap().entrySet()) {
            GameStatus status = getGameOf((String) kvp.getKey(), kvp.getValue());
            if (status == null) {
                continue;
            }

            if (debug) {
                BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Added game:</c2> <c1>" + status.getDisplayName() + " (" + status.getName() + ")</c1>");
            }
            retVal.add(status);
        }

        return retVal;
    }

    private static @Nullable GameStatus getGameOf(@NotNull String name, @NotNull ConfigurationNode gameNode) {
        String displayName = gameNode.node("name").getString("");
        if (displayName.isEmpty()) {
            logger.error("Could not create game \"" + name + "\" due to invalid name.");
            return null;
        }

        TimeUtil.Time min = TimeUtil.getTime(gameNode.node("min").getString(""));
        if (min == null) {
            logger.error("Could not create game \"" + name + "\" due to invalid min time.");
            return null;
        }

        TimeUtil.Time max = TimeUtil.getTime(gameNode.node("max").getString(""));
        if (max == null) {
            logger.error("Could not create game \"" + name + "\" due to invalid max time.");
            return null;
        }

        return new GameStatus(name, displayName, min, max);
    }

    private static @NotNull CommentedConfigurationNode getConfig(@NotNull String resourcePath, @NotNull File fileOnDisk) throws IOException {
        File parentDir = fileOnDisk.getParentFile();
        if (parentDir.exists() && !parentDir.isDirectory()) {
            Files.delete(parentDir.toPath());
        }
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Could not create parent directory structure.");
            }
        }
        if (fileOnDisk.exists() && fileOnDisk.isDirectory()) {
            Files.delete(fileOnDisk.toPath());
        }

        if (!fileOnDisk.exists()) {
            try (InputStream inStream = ConfigurationFileUtil.class.getResourceAsStream("/" + resourcePath)) {
                if (inStream != null) {
                    try (FileOutputStream outStream = new FileOutputStream(fileOnDisk)) {
                        int read;
                        byte[] buffer = new byte[4096];
                        while ((read = inStream.read(buffer, 0, buffer.length)) > 0) {
                            outStream.write(buffer, 0, read);
                        }
                    }
                }
            }
        }

        ConfigurationLoader<CommentedConfigurationNode> loader = YamlConfigurationLoader.builder().nodeStyle(NodeStyle.BLOCK).indent(2).file(fileOnDisk).build();
        CommentedConfigurationNode config = loader.load(ConfigurationOptions.defaults().header("Comments are gone because update :("));
        ConfigurationVersionUtil.conformVersion(loader, config, fileOnDisk);

        return config;
    }

    private static class AddressPort {
        private final String address;
        private final int port;

        public AddressPort(@NotNull String node, @NotNull String raw, int defaultPort) {
            String a = raw;
            int portIndex = a.indexOf(':');
            int p;
            if (portIndex > -1) {
                p = Integer.parseInt(a.substring(portIndex + 1));
                a = a.substring(0, portIndex);
            } else {
                logger.warn(node + " port is an unknown value. Using default value.");
                p = defaultPort;
            }

            this.address = a;
            this.port = p;
        }

        public @NotNull String getAddress() { return address; }

        public int getPort() { return port; }
    }

    private static class PoolSettings {
        private final int minPoolSize;
        private final int maxPoolSize;
        private final long maxLifetime;
        private final long timeout;

        public PoolSettings(ConfigurationNode settingsNode) {
            minPoolSize = settingsNode.node("min-idle").getInt();
            maxPoolSize = settingsNode.node("max-pool-size").getInt();

            TimeUtil.Time l = TimeUtil.getTime(settingsNode.node("max-lifetime").getString("30minutes"));
            if (l == null) {
                l = new TimeUtil.Time(30L, TimeUnit.MINUTES);
            }
            maxLifetime = l.getMillis();

            TimeUtil.Time t = TimeUtil.getTime(settingsNode.node("timeout").getString("5seconds"));
            if (t == null) {
                t = new TimeUtil.Time(5L, TimeUnit.SECONDS);
            }
            timeout = t.getMillis();
        }

        public int getMinPoolSize() { return minPoolSize; }

        public int getMaxPoolSize() { return maxPoolSize; }

        public long getMaxLifetime() { return maxLifetime; }

        public long getTimeout() { return timeout; }
    }
}
