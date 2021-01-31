package me.egg82.echo.utils;

import java.util.List;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDAUtil {
    private static Logger logger = LoggerFactory.getLogger(JDAUtil.class);

    private JDAUtil() { }

    public static @Nullable Emote getEmote(@NotNull String emote, @NotNull JDA jda, @NotNull Guild guild) {
        List<Emote> emotes = guild.getEmotesByName(emote, true);
        if (emotes.isEmpty()) {
            emotes = jda.getEmotesByName(emote, true);
        }
        return emotes.isEmpty() ? null : emotes.get(0);
    }

    public static boolean isAdmin(@NotNull Member member) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return false;
        }

        List<Role> roles = member.getRoles();
        for (Role role : roles) {
            if (role.getName().equalsIgnoreCase(cachedConfig.getAdminRole())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAllowed(@NotNull Member member) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return true;
        }

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

    public static boolean isCommand(@NotNull String content) {
        CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
        if (cachedConfig == null) {
            logger.error("Could not get cached config.");
            return false;
        }

        for (String prefix : cachedConfig.getCommandPrefixes()) {
            if (content.length() >= prefix.length() && prefix.equals(content.substring(0, prefix.length()))) {
                return true;
            }
        }
        return false;
    }
}
