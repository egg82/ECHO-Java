package me.egg82.echo.config;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;

public class ConfigurationVersionUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationVersionUtil.class);

    private ConfigurationVersionUtil() { }

    public static void conformVersion(@NotNull ConfigurationLoader<CommentedConfigurationNode> loader, @NotNull CommentedConfigurationNode config, @NotNull File fileOnDisk) throws IOException {
        double oldVersion = config.node("version").getDouble(1.0d);

        /*if (config.node("version").getDouble(1.0d) == 1.0d) {
            to20(config);
        }
        if (config.node("version").getDouble() == 2.0d) {
            to21(config);
        }*/

        if (config.node("version").getDouble() != oldVersion) {
            File backupFile = new File(fileOnDisk.getParent(), fileOnDisk.getName() + ".bak");
            if (backupFile.exists()) {
                java.nio.file.Files.delete(backupFile.toPath());
            }

            Files.copy(fileOnDisk, backupFile);
            loader.save(config);
        }
    }

    /*private static void to20(@@NotNull CommentedConfigurationNode config) throws SerializationException {
        // Rabbit -> Messaging
        boolean rabbitEnabled = config.node("rabbit", "enabled").getBoolean();
        String rabbitAddress = config.node("rabbit", "address").getString("");
        int rabbitPort = config.node("rabbit", "port").getInt(5672);
        String rabbitUser = config.node("rabbit", "user").getString("guest");
        String rabbitPass = config.node("rabbit", "pass").getString("guest");
        config.removeChild("rabbit");
        config.node("messaging", "type").set((rabbitEnabled) ? "rabbit" : "bungee");
        config.node("messaging", "rabbit", "address").set(rabbitAddress);
        config.node("messaging", "rabbit", "port").set(rabbitPort);
        config.node("messaging", "rabbit", "user").set(rabbitUser);
        config.node("messaging", "rabbit", "pass").set(rabbitPass);

        // sources.order String -> List
        String[] order = config.node("sources", "order").getString("").split(",\\s?");
        config.node("sources", "order").setList(String.class, Arrays.asList(order));

        // Add ignore
        config.node("ignore").setList(String.class, Arrays.asList("127.0.0.1", "localhost", "::1"));

        // Remove async
        config.removeChild("async");

        // Version
        config.node("version").set(2.0d);
    }

    private static void to21(@@NotNull CommentedConfigurationNode config) throws SerializationException {
        // Add consensus
        config.node("consensus").set(-1.0d);

        // Version
        config.node("version").set(2.1d);
    }*/
}
