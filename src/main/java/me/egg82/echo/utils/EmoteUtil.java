package me.egg82.echo.utils;

import java.util.List;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmoteUtil {
    private EmoteUtil() { }

    public static @Nullable Emote getEmote(@NotNull String emote, @NotNull Guild guild) {
        List<Emote> emotes = guild.getEmotesByName(emote, true);
        return emotes.isEmpty() ? null : emotes.get(0);
    }
}
