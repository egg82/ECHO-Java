package me.egg82.echo.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.paradaux.ai.MarkovMegaHal;
import java.util.*;
import me.egg82.echo.messaging.MessagingService;
import me.egg82.echo.storage.StorageService;
import org.jetbrains.annotations.NotNull;

public class CachedConfig {
    private CachedConfig() { }

    private MarkovMegaHal megaHal = new MarkovMegaHal();
    public @NotNull MarkovMegaHal getMegaHal() { return megaHal; }

    private ImmutableList<StorageService> storage = ImmutableList.of();
    public @NotNull ImmutableList<StorageService> getStorage() { return storage; }

    private ImmutableList<MessagingService> messaging = ImmutableList.of();
    public @NotNull ImmutableList<MessagingService> getMessaging() { return messaging; }

    private boolean debug = false;
    public boolean getDebug() { return debug; }

    private Locale language = Locale.ENGLISH;
    public @NotNull Locale getLanguage() { return language; }

    private UUID serverId = UUID.randomUUID();
    public @NotNull UUID getServerId() { return serverId; }

    private String serverIdString = serverId.toString();
    public @NotNull String getServerIdString() { return serverIdString; }

    private String googleKey = "";
    public @NotNull String getGoogleKey() { return googleKey; }

    private String alotEmote = "alot";
    public @NotNull String getAlotEmote() { return alotEmote; }

    private Set<String> disabledCommands = ImmutableSet.of();
    public @NotNull Set<String> getDisabledCommands() { return disabledCommands; }

    private double replyChance = 0.15d;
    public double getReplyChance() { return replyChance; }

    private Set<String> replyPhrases = ImmutableSet.of();
    public @NotNull Set<String> getReplyPhrases() { return replyPhrases; }

    private Set<String> replyPhrasesReversed = ImmutableSet.of();
    public @NotNull Set<String> getReplyPhrasesReversed() { return replyPhrasesReversed; }

    public static @NotNull CachedConfig.Builder builder() { return new CachedConfig.Builder(); }

    public static class Builder {
        private final CachedConfig values = new CachedConfig();

        private Builder() { }

        public @NotNull CachedConfig.Builder debug(boolean value) {
            values.debug = value;
            return this;
        }

        public @NotNull CachedConfig.Builder language(@NotNull Locale value) {
            values.language = value;
            return this;
        }

        public @NotNull CachedConfig.Builder megaHal(@NotNull MarkovMegaHal value) {
            values.megaHal = value;
            return this;
        }

        public @NotNull CachedConfig.Builder storage(@NotNull List<StorageService> value) {
            values.storage = ImmutableList.copyOf(value);
            return this;
        }

        public @NotNull CachedConfig.Builder messaging(@NotNull List<MessagingService> value) {
            values.messaging = ImmutableList.copyOf(value);
            return this;
        }

        public @NotNull CachedConfig.Builder serverId(@NotNull UUID value) {
            values.serverId = value;
            values.serverIdString = value.toString();
            return this;
        }

        public @NotNull CachedConfig.Builder googleKey(@NotNull String value) {
            values.googleKey = value;
            return this;
        }

        public @NotNull CachedConfig.Builder alotEmote(@NotNull String value) {
            values.alotEmote = value;
            return this;
        }

        public @NotNull CachedConfig.Builder disabledCommands(@NotNull Set<String> value) {
            values.disabledCommands = ImmutableSet.copyOf(value);
            return this;
        }

        public @NotNull CachedConfig.Builder replyChance(@NotNull double value) {
            values.replyChance = value;
            return this;
        }

        public @NotNull CachedConfig.Builder replyPhrases(@NotNull Set<String> value) {
            values.replyPhrases = ImmutableSet.copyOf(value);
            Set<String> reversedPhrases = new HashSet<>();
            for (String phrase : value) {
                reversedPhrases.add(reverse(phrase));
            }
            values.replyPhrasesReversed = ImmutableSet.copyOf(reversedPhrases);
            return this;
        }

        public CachedConfig build() { return values; }

        private @NotNull String reverse(@NotNull String input) {
            StringBuilder builder = new StringBuilder();
            builder.append(input);
            builder.reverse();
            return builder.toString();
        }
    }
}
