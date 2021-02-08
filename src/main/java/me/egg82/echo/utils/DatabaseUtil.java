package me.egg82.echo.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Weigher;
import com.google.common.hash.HashCode;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import me.egg82.echo.compression.ZstdCompressionStream;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.core.Pair;
import me.egg82.echo.storage.StorageService;
import me.egg82.echo.storage.models.WebModel;
import me.egg82.echo.web.transformers.InstantTransformer;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);

    private static final ZstdCompressionStream ZSTD_COMPRESSION = new ZstdCompressionStream();
    private static final MessageDigest DIGEST;
    private static final File PATH = new File(FileUtil.getCwd(), "web");

    static {
        try {
            DIGEST = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Digest could not be instantiated.", ex);
        }

        try {
            if (PATH.exists() && !PATH.isDirectory()) {
                Files.delete(PATH.toPath());
            }
            if (!PATH.exists()) {
                if (!PATH.mkdirs()) {
                    throw new IOException("Could not create parent directory structure.");
                }
            }
            if (PATH.exists() && PATH.isDirectory()) {
                Files.delete(PATH.toPath());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create PATH.", ex);
        }
    }

    private DatabaseUtil() { }

    public static <T> @Nullable T getModel(@NotNull String hash, @NotNull String service, @NotNull Class<T> modelClass) {
        byte[] retVal = getBytes(hash, service);
        if (retVal == null) {
            return null;
        }

        JSONDeserializer<T> modelDeserializer = new JSONDeserializer<>();
        modelDeserializer.use(Instant.class, new InstantTransformer());
        return modelDeserializer.deserialize(new String(retVal, StandardCharsets.UTF_8), modelClass);
    }

    public static <T> @Nullable T getModel(@NotNull String hash, @NotNull String service, @NotNull Class<T> modelClass, long cacheTimeMillis) {
        byte[] retVal = getBytes(hash, service, cacheTimeMillis);
        if (retVal == null) {
            return null;
        }

        JSONDeserializer<T> modelDeserializer = new JSONDeserializer<>();
        modelDeserializer.use(Instant.class, new InstantTransformer());
        return modelDeserializer.deserialize(new String(retVal, StandardCharsets.UTF_8), modelClass);
    }

    public static <T> void storeModel(@NotNull String hash, @NotNull String service, @NotNull T model) {
        JSONSerializer modelSerializer = new JSONSerializer();
        modelSerializer.prettyPrint(false);
        modelSerializer.transform(new InstantTransformer(), Instant.class);
        storeBytes(hash, service, modelSerializer.exclude("*.class").deepSerialize(model).getBytes(StandardCharsets.UTF_8));
    }

    public static @Nullable String getString(@NotNull String hash, @NotNull String service) {
        byte[] retVal = getBytes(hash, service);
        if (retVal == null) {
            return null;
        }
        return new String(retVal, StandardCharsets.UTF_8);
    }

    public static @Nullable String getString(@NotNull String hash, @NotNull String service, long cacheTimeMillis) {
        byte[] retVal = getBytes(hash, service, cacheTimeMillis);
        if (retVal == null) {
            return null;
        }
        return new String(retVal, StandardCharsets.UTF_8);
    }

    public static void storeString(@NotNull String hash, @NotNull String service, @NotNull String string) { storeBytes(hash, service, string.getBytes(StandardCharsets.UTF_8)); }

    private static final Cache<Pair<String, String>, byte[]> dataCache = Caffeine.newBuilder()
            .expireAfterWrite(1L, TimeUnit.DAYS)
            .expireAfterAccess(4L, TimeUnit.HOURS)
            .maximumWeight(1024L * 1024L * 2L) // 2MB
            .weigher((Weigher<Pair<String, String>, byte[]>) (k, v) -> v.length)
            .build();

    public static byte @Nullable [] getBytes(@NotNull String hash, @NotNull String service) {
        return dataCache.get(new Pair<>(hash, service), k -> {
            CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
            if (cachedConfig == null) {
                throw new IllegalStateException("Could not get cached config.");
            }

            for (StorageService s : cachedConfig.getStorage()) {
                WebModel model = s.getWebModel(hash, service);
                if (model != null) {
                    File file = new File(PATH, model.getPath());
                    if (file.exists()) {
                        try {
                            return ZSTD_COMPRESSION.decompress(Files.readAllBytes(file.toPath()));
                        } catch (IOException ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                    }
                }
            }

            return null;
        });
    }

    public static byte @Nullable [] getBytes(@NotNull String hash, @NotNull String service, long cacheTimeMillis) {
        return dataCache.get(new Pair<>(hash, service), k -> {
            CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
            if (cachedConfig == null) {
                throw new IllegalStateException("Could not get cached config.");
            }

            for (StorageService s : cachedConfig.getStorage()) {
                WebModel model = s.getWebModel(hash, service, cacheTimeMillis);
                if (model != null) {
                    File file = new File(PATH, model.getPath());
                    if (file.exists()) {
                        try {
                            return ZSTD_COMPRESSION.decompress(Files.readAllBytes(file.toPath()));
                        } catch (IOException ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                    }
                }
            }

            return null;
        });
    }

    public static void storeBytes(@NotNull String hash, @NotNull String service, byte @NotNull [] data) {
        dataCache.put(new Pair<>(hash, service), data);

        String fileName;
        do {
            fileName = RandomStringUtils.randomAlphanumeric(12);
        } while (new File(PATH, fileName).exists());

        try {
            Files.write(new File(PATH, fileName).toPath(), ZSTD_COMPRESSION.compress(data));
        } catch (IOException ex) {
            throw new RuntimeException("Could not write file to disk.", ex);
        }

        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            throw new IllegalStateException("Could not get cached config.");
        }

        for (StorageService s : cachedConfig.getStorage()) {
            WebModel model = new WebModel();
            model.setHash(hash);
            model.setService(service);
            model.setPath(fileName);
            s.storeModel(model);
        }
    }

    public static @NotNull String sha512(@NotNull String content) { return sha512(content.getBytes(StandardCharsets.UTF_8)); }

    public static @NotNull String sha512(byte @NotNull [] content) { return HashCode.fromBytes(DIGEST.digest(content)).toString(); }
}
