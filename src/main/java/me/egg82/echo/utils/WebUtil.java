package me.egg82.echo.utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import me.egg82.echo.web.WebConstants;
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
}
