package me.egg82.echo.utils;

import me.egg82.echo.config.CachedConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JDAUtil {
    private JDAUtil() { }

    public static @Nullable Emote getEmote(@NotNull String emote, @NotNull JDA jda) {
        List<Emote> emotes = jda.getEmotesByName(emote, true);
        return emotes.isEmpty() ? null : emotes.get(0);
    }

    public static @Nullable Emote getEmote(@NotNull String emote, @NotNull Guild guild) {
        List<Emote> emotes = guild.getEmotesByName(emote, true);
        return emotes.isEmpty() ? null : emotes.get(0);
    }

    public static boolean isAdmin(@NotNull CachedConfig cachedConfig, @NotNull Member member) {
        List<Role> roles = member.getRoles();
        for (Role role : roles) {
            if (role.getName().equalsIgnoreCase(cachedConfig.getAdminRole())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAllowed(@NotNull CachedConfig cachedConfig, @NotNull Member member) {
        if (cachedConfig.getDisallowedRole().isEmpty()) {
            return true;
        }

        List<Role> roles = member.getRoles();
        for (Role role : roles) {
            if (role.getName().equalsIgnoreCase(cachedConfig.getDisallowedRole())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCommand(@NotNull CachedConfig cachedConfig, @NotNull String content) {
        for (String prefix : cachedConfig.getCommandPrefixes()) {
            if (content.length() >= prefix.length() && prefix.equals(content.substring(0, prefix.length()))) {
                return true;
            }
        }
        return false;
    }
}
