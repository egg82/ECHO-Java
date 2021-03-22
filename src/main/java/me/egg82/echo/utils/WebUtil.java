package me.egg82.echo.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.egg82.echo.compression.GZIPCompressionStream;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.web.WebConstants;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

public class WebUtil {
    private static final Logger logger = LoggerFactory.getLogger(WebUtil.class);

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(WebConstants.CONNECT_TIMEOUT.getTime(), WebConstants.CONNECT_TIMEOUT.getUnit())
            .readTimeout(WebConstants.READ_TIMEOUT.getTime(), WebConstants.READ_TIMEOUT.getUnit())
            .followRedirects(true)
            .followSslRedirects(true)
            .build();

    private WebUtil() { }

    public static @NotNull String urlEncode(@NotNull String part) { return URLEncoder.encode(part, StandardCharsets.UTF_8); }

    public static @NotNull Request.Builder getDefaultRequestBuilder(@NotNull URL url) {
        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Getting URL: " + url);
        }

        return new Request.Builder()
                .url(url)
                .header("Accept-Language", "en-US,en;q=0.8")
                .header("User-Agent", WebConstants.USER_AGENT);
    }

    public static @NotNull Response getResponse(@NotNull Request request) throws IOException { return client.newCall(request).execute(); }

    public static @NotNull CompletableFuture<String> getString(@NotNull String url) {
        return getUnclosedResponse(url, "text/plain").thenApplyAsync(response -> {
            try {
                try (response) {
                    return response.body().string();
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }

    public static @NotNull CompletableFuture<byte[]> getBytes(@NotNull String url) {
        return getUnclosedResponse(url).thenApplyAsync(response -> {
            try {
                try (response) {
                    return response.body().bytes();
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }

    public static @NotNull CompletableFuture<HttpUrl> getRedirectedUrl(@NotNull String url) {
        return getUnclosedResponse(url).thenApplyAsync(response -> {
            try (response) {
                return response.request().url();
            }
        });
    }

    public static @NotNull CompletableFuture<Response> getUnclosedResponse(@NotNull String url) { return getUnclosedResponse(url, null); }

    public static @NotNull CompletableFuture<Response> getUnclosedResponse(@NotNull String url, String accept) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Request.Builder builder = getDefaultRequestBuilder(new URL(url));
                if (accept != null) {
                    builder.header("Accept", accept);
                }
                Request request = builder.build();

                Response response = getResponse(request);
                if (!response.isSuccessful()) {
                    throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
                }
                return response;
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        }).exceptionally(ex -> ExceptionUtil.handleException(ex, logger));
    }

    private static final GZIPCompressionStream GZIP_COMPRESSION = new GZIPCompressionStream();

    private static final String BYTEBIN_URL = "https://bytebin.egg82.me/%s";

    private static final Cache<String, String> bytebinCache = Caffeine.newBuilder()
            .expireAfterWrite(1L, TimeUnit.DAYS)
            .expireAfterAccess(4L, TimeUnit.HOURS)
            .build();

    public static @NotNull CompletableFuture<String> uploadBytebinContent(byte @NotNull [] content) {
        return CompletableFuture.supplyAsync(() -> bytebinCache.get(DatabaseUtil.sha512(content), k -> uploadBytebinContentExpensive(k, content)))
                .exceptionally(ex -> ExceptionUtil.handleException(ex, logger));
    }

    private static @NotNull String uploadBytebinContentExpensive(@NotNull String hash, byte @NotNull [] content) {
        try {
            String retVal = DatabaseUtil.getString(hash, "bytebin");
            if (retVal != null) {
                return retVal;
            }

            RequestBody body = RequestBody.create(GZIP_COMPRESSION.compress(content), MediaType.get("text/plain"));

            Request request = getDefaultRequestBuilder(new URL(String.format(BYTEBIN_URL, "post")))
                    .header("Content-Encoding", "gzip")
                    .post(body)
                    .build();

            System.out.println("Bytebin request: " + request.toString());

            try (Response response = getResponse(request)) {
                System.out.println("Bytebin response: " + response.code());

                if (!response.isSuccessful()) {
                    throw new IOException("Could not get connection (HTTP status " + response.code() + ")");
                }

                retVal = String.format(BYTEBIN_URL, response.header("Location"));
                System.out.println("Bytebin URL: " + retVal);
                DatabaseUtil.storeString(hash, "bytebin", retVal);
                return retVal;
            }
        } catch (IOException ex) {
            throw new CompletionException(ex);
        }
    }
}
