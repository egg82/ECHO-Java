package me.egg82.echo.events;

import co.aikar.commands.JDACommandManager;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.utils.JDAUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ninja.egg82.events.JDAEvents;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class ReplyEvents extends EventHolder {
    private final JDA jda;
    private final JDACommandManager manager;

    private static final Pattern RE_EXCLAMATION = Pattern.compile("^!{2,}$");
    private static final Pattern RE_QUESTION = Pattern.compile("^\\?{2,}$");

    public ReplyEvents(@NotNull JDA jda, @NotNull JDACommandManager manager) {
        this.jda = jda;
        this.manager = manager;

        events.add(JDAEvents.subscribe(jda, MessageReceivedEvent.class)
                           .filter(e -> {
                               CachedConfig cachedConfig = ConfigUtil.getCachedConfig();
                               if (cachedConfig == null) {
                                   logger.error("Could not get cached config.");
                                   return false;
                               }
                               return !JDAUtil.isCommand(cachedConfig, e.getMessage().getContentRaw());
                           })
                           .handler(this::replyBold));
    }

    private void replyBold(@NotNull MessageReceivedEvent event) {
        String trimmed = event.getMessage().getContentStripped().trim();

        if (RE_EXCLAMATION.matcher(trimmed).matches()) {
            event.getChannel().sendMessage(build("\u2757", trimmed.length())).queue();
        } else if (RE_QUESTION.matcher(trimmed).matches()) {
            event.getChannel().sendMessage(build("\u2753", trimmed.length())).queue();
        }
    }

    private @NotNull String build(@NotNull String characters, int length) { return characters.repeat(Math.max(0, length)); }
}
