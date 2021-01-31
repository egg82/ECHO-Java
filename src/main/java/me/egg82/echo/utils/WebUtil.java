package me.egg82.echo.utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import me.egg82.echo.web.WebConstants;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class WebUtil {
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(WebConstants.CONNECT_TIMEOUT.getTime(), WebConstants.CONNECT_TIMEOUT.getUnit())
            .readTimeout(WebConstants.READ_TIMEOUT.getTime(), WebConstants.READ_TIMEOUT.getUnit())
            .followRedirects(true)
            .followSslRedirects(true)
            .build();

    private WebUtil() { }

    public static @NotNull String urlEncode(@NotNull String part) { return URLEncoder.encode(part, StandardCharsets.UTF_8); }

    public static @NotNull Request.Builder getDefaultRequestBuilder(@NotNull URL url) {
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
        });
    }
}
