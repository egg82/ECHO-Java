package me.egg82.echo.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("javadoc|jd")
public class JavadocCommand extends AbstractCommand {
    public JavadocCommand() { }

    public boolean requiresAdmin() { return false; }

    public @Nullable EmbedBuilder getDescription() {
        EmbedBuilder retVal = new EmbedBuilder();
        retVal.addField("Examples", "```" +
                "!javadoc luckperms DeletionCause%command\n" +
                "!javadoc luckperms NodeMap#toMap()\n" +
                "!javadoc adventure Audience#showTitle(title)" +
                "```", false);
        retVal.addField("Info", "This javadoc search command is powered by " +
                "[DocDex](https://github.com/PiggyPiglet/DocDex)." +
                " This function is *not* provided by the DocDex bot." +
                " As such, problems with this discord function should not be reported to DocDex.", false);
        return retVal;
    }

    @Default
    @Description("{@@description.javadoc}")
    @Syntax("<repo> [search]")
    public void submit(@NotNull CommandIssuer issuer, @NotNull MessageReceivedEvent event, @NotNull String repo, @NotNull String search) {

    }
}
