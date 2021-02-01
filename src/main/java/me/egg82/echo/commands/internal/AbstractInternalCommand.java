package me.egg82.echo.commands.internal;

import co.aikar.commands.CommandIssuer;
import me.egg82.echo.commands.AbstractCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractInternalCommand extends AbstractCommand implements Runnable {
    protected final CommandIssuer issuer;
    protected final MessageReceivedEvent event;

    protected AbstractInternalCommand(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event) {
        this.issuer = issuer;
        this.event = event;
    }

    public boolean requiresAdmin() { return true; }
}
