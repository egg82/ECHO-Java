package me.egg82.echo.config;

import co.aikar.commands.CommandManager;
import io.paradaux.ai.MarkovMegaHal;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeoutException;
import me.egg82.echo.messaging.*;
import me.egg82.echo.storage.*;
import me.egg82.echo.utils.BotLogUtil;
import me.egg82.echo.utils.LogUtil;
import me.egg82.echo.utils.PacketUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import redis.clients.jedis.exceptions.JedisException;

public class ConfigurationFileUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationFileUtil.class);

    private ConfigurationFileUtil() { }

    public static void reloadConfig(@NotNull File dataDirectory, @NotNull CommandManager manager, @NotNull MessagingHandler messagingHandler, @NotNull MarkovMegaHal megaHal) {
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

        String googleKey = config.node("keys", "google").getString("");
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Google API key:</c2> <c1>" + googleKey + "</c1>");
        }

        String alotEmote = config.node("emotes", "alot").getString("");
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Alot emote:</c2> <c1>" + alotEmote + "</c1>");
        }

        CachedConfig cachedConfig = CachedConfig.builder()
                .debug(debug)
                .language(getLanguage(config, debug, manager))
                .megaHal(megaHal)
                .storage(getStorage(config, dataDirectory, debug, manager))
                .messaging(getMessaging(config, serverId, messagingHandler, debug, manager))
                .serverId(serverId)
                .googleKey(googleKey)
                .alotEmote(alotEmote)
                .build();

        PacketUtil.setPoolSize(cachedConfig.getMessaging().size() + 1);

        ConfigUtil.setConfiguration(config, cachedConfig);
    }

    private static @NotNull Locale getLanguage(@NotNull ConfigurationNode config, boolean debug, @NotNull CommandManager manager) {
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
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c9>lang</c9> <c1>" + configLanguage + "</c1> <c9>is not a valid language. Using default value of</c9> <c1>" + (retVal.getCountry() == null || retVal.getCountry().isEmpty() ? retVal.getLanguage() : retVal.getLanguage() + "-" + retVal.getCountry()) + "</c1>");
        }
        if (debug) {
            BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Default language:</c2> <c1>" + (retVal.getCountry() == null || retVal.getCountry().isEmpty() ? retVal.getLanguage() : retVal.getLanguage() + "-" + retVal.getCountry()) + "</c1>");
        }

        return retVal;
    }

    private static @NotNull List<StorageService> getStorage(@NotNull ConfigurationNode config, @NotNull File dataDirectory, boolean debug, @NotNull CommandManager manager) {
        List<StorageService> retVal = new ArrayList<>();

        PoolSettings poolSettings = new PoolSettings(config.node("storage", "settings"));
        for (Map.Entry<Object, ? extends ConfigurationNode> kvp : config.node("storage", "engines").childrenMap().entrySet()) {
            StorageService service = getStorageOf((String) kvp.getKey(), kvp.getValue(), dataDirectory, poolSettings, debug, manager);
            if (service == null) {
                continue;
            }

            if (debug) {
                BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Added storage:</c2> <c1>" + service.getName() + " (" + service.getClass().getSimpleName() + ")</c1>");
            }
            retVal.add(service);
        }

        return retVal;
    }

    private static @Nullable StorageService getStorageOf(@NotNull String name, @NotNull ConfigurationNode engineNode, @NotNull File dataDirectory, @NotNull PoolSettings poolSettings, boolean debug, @NotNull CommandManager manager) {
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
                    BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type old_mysql with address</c2> <c1>" + url.getAddress() + ":" + url.getPort() + "/" + connectionNode.node("database").getString("echo") + "</c1>");
                }
                String options = connectionNode.node("options").getString("useSSL=false&useUnicode=true&characterEncoding=utf8");
                if (options.length() > 0 && options.charAt(0) == '?') {
                    options = options.substring(1);
                }
                if (debug) {
                    BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Setting options for engine</c2> <c1>" + name + "</c1> <c2>to</c2> <c1>" + options.replace("&", "&\\") + "</c1>");
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
                    BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type mysql with address</c2> <c1>" + url.getAddress() + ":" + url.getPort() + "/" + connectionNode.node("database").getString("echo") + "</c1>");
                }
                String options = connectionNode.node("options").getString("useSSL=false&useUnicode=true&characterEncoding=utf8");
                if (options.length() > 0 && options.charAt(0) == '?') {
                    options = options.substring(1);
                }
                if (debug) {
                    BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Setting options for engine</c2> <c1>" + name + "</c1> <c2>to</c2> <c1>" + options.replace("&", "&\\") + "</c1>");
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
                    BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type mariadb with address</c2> <c1>" + url.getAddress() + ":" + url.getPort() + "/" + connectionNode.node("database").getString("echo") + "</c1>");
                }
                String options = connectionNode.node("options").getString("useSSL=false&useUnicode=true&characterEncoding=utf8");
                if (options.length() > 0 && options.charAt(0) == '?') {
                    options = options.substring(1);
                }
                if (debug) {
                    BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Setting options for engine</c2> <c1>" + name + "</c1> <c2>to</c2> <c1>" + options.replace("&", "&\\") + "</c1>");
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
                    BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type postgresql with address</c2> <c1>" + url.getAddress() + ":" + url.getPort() + "/" + connectionNode.node("database").getString("echo") + "</c1>");
                }
                String options = connectionNode.node("options").getString("useSSL=false&useUnicode=true&characterEncoding=utf8");
                if (options.length() > 0 && options.charAt(0) == '?') {
                    options = options.substring(1);
                }
                if (debug) {
                    BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Setting options for engine</c2> <c1>" + name + "</c1> <c2>to</c2> <c1>" + options.replace("&", "&\\") + "</c1>");
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
                    BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type h2 with file</c2> <c1>" + connectionNode.node("file").getString("echo") + "</c1>");
                }
                String options = connectionNode.node("options").getString("useUnicode=true&characterEncoding=utf8");
                if (options.length() > 0 && options.charAt(0) == '?') {
                    options = options.substring(1);
                }
                if (debug) {
                    BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Setting options for engine</c2> <c1>" + name + "</c1> <c2>to</c2> <c1>" + options.replace("&", "&\\") + "</c1>");
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
                    BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type sqlite with file</c2> <c1>" + connectionNode.node("file").getString("echo.db") + "</c1>");
                }
                String options = connectionNode.node("options").getString("useUnicode=true&characterEncoding=utf8");
                if (options.length() > 0 && options.charAt(0) == '?') {
                    options = options.substring(1);
                }
                if (debug) {
                    BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Setting options for engine</c2> <c1>" + name + "</c1> <c2>to</c2> <c1>" + options.replace("&", "&\\") + "</c1>");
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

    private static @NotNull List<MessagingService> getMessaging(@NotNull ConfigurationNode config, @NotNull UUID serverId, @NotNull MessagingHandler handler, boolean debug, @NotNull CommandManager manager) {
        List<MessagingService> retVal = new ArrayList<>();

        PoolSettings poolSettings = new PoolSettings(config.node("messaging", "settings"));
        for (Map.Entry<Object, ? extends ConfigurationNode> kvp : config.node("messaging", "engines").childrenMap().entrySet()) {
            MessagingService service = getMessagingOf((String) kvp.getKey(), kvp.getValue(), serverId, handler, poolSettings, debug, manager);
            if (service == null) {
                continue;
            }

            if (debug) {
                BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Added messaging:</c2> <c1>" + service.getName() + " (" + service.getClass().getSimpleName() + ")</c1>");
            }
            retVal.add(service);
        }

        return retVal;
    }

    private static @Nullable MessagingService getMessagingOf(@NotNull String name, @NotNull ConfigurationNode engineNode, @NotNull UUID serverId, @NotNull MessagingHandler handler, @NotNull PoolSettings poolSettings, boolean debug, @NotNull CommandManager manager) {
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
                    BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type rabbitmq with address</c2> <c1>" + url.getAddress() + ":" + url.getPort() + connectionNode.node("v-host").getString("/") + "</c1>");
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
                    BotLogUtil.sendInfo(logger, manager, LogUtil.HEADING + "<c2>Creating engine</c2> <c1>" + name + "</c1> <c2>of type redis with address</c2> <c1>" + url.getAddress() + ":" + url.getPort() + "</c1>");
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
            maxLifetime = settingsNode.node("max-lifetime").getLong();
            timeout = settingsNode.node("timeout").getLong();
        }

        public int getMinPoolSize() { return minPoolSize; }

        public int getMaxPoolSize() { return maxPoolSize; }

        public long getMaxLifetime() { return maxLifetime; }

        public long getTimeout() { return timeout; }
    }
}
