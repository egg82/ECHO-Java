package me.egg82.echo.utils;

import java.util.List;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoleUtil {
    private static final Logger logger = LoggerFactory.getLogger(RoleUtil.class);

    private RoleUtil() { }

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
}
