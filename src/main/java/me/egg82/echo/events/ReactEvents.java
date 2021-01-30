package me.egg82.echo.events;

import co.aikar.commands.JDACommandManager;
import java.util.regex.Pattern;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.utils.EmoteUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import ninja.egg82.events.JDAEvents;
import org.jetbrains.annotations.NotNull;

public class ReactEvents extends EventHolder {
    private final JDA jda;
    private final JDACommandManager manager;

    private static final Pattern RE_SPACE = Pattern.compile("[\\s\\t]+");
    private static final Pattern RE_NOT_WORD = Pattern.compile("[^\\w]");

    public ReactEvents(@NotNull JDA jda, @NotNull JDACommandManager manager) {
        this.jda = jda;
        this.manager = manager;

        events.add(JDAEvents.subscribe(jda, GuildMessageReceivedEvent.class)
                .filter(e -> containsWord(e.getMessage().getContentStripped(), "alot"))
                .handler(this::reactAlot));
    }

    private void reactAlot(@NotNull GuildMessageReceivedEvent event) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return;
        }

        Emote emote = EmoteUtil.getEmote(cachedConfig.getAlotEmote(), event.getGuild());
        if (emote == null) {
            logger.warn("Could not find alot emote \"" + cachedConfig.getAlotEmote() + "\" for guild \"" + event.getGuild().getName() + "\".");
            return;
        }

        event.getMessage().addReaction(emote).queue();
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
}
