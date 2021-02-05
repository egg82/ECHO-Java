package me.egg82.echo.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import flexjson.JSONDeserializer;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.lang.Message;
import me.egg82.echo.utils.WebUtil;
import me.egg82.echo.web.models.JavadocModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("javadoc|jd")
public class JavadocCommand extends AbstractCommand {
    private static final String API_URL = "https://docdex.helpch.at/index?javadoc=%s&query=%s&limit=%d";
    private static final int ITEM_LIMIT = 1;

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
        CachedConfig cachedConfig = getCachedConfig(issuer);
        if (cachedConfig == null || !canRun(event, cachedConfig)) {
            return;
        }

        getModel(repo, search).whenCompleteAsync((val, ex) -> {
            if (!canCompleteContinue(issuer, val, ex)) {
                return;
            }

            JavadocModel first = val.get(0);

            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(new Color(0xB11ACB));

            addData(first, embed);

            embed.setFooter("For " + (event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getAsTag()));

            if (embed.getFields().isEmpty()) {
                issuer.sendError(Message.ERROR__INTERNAL);
            } else {
                event.getChannel().sendMessage(embed.build()).queue();
            }
        });
    }

    public static @NotNull CompletableFuture<List<JavadocModel>> getModel(@NotNull String repo, @NotNull String query) {
        return WebUtil.getUnclosedResponse(String.format(API_URL, WebUtil.urlEncode(repo), WebUtil.urlEncode(query.replaceAll("\\s", "").replace("#", "~").replace("%", "-")), ITEM_LIMIT), "application/json").thenApplyAsync(response -> {
            try (response) {
                JSONDeserializer<List<JavadocModel>> modelDeserializer = new JSONDeserializer<>();
                modelDeserializer.use("values", JavadocModel.class);
                List<JavadocModel> retVal = modelDeserializer.deserialize(response.body().charStream());
                return retVal == null || retVal.isEmpty() ? null : retVal;
            }
        });
    }

    private void addData(@NotNull JavadocModel model, @NotNull EmbedBuilder embed) {
        switch (model.getObject().getType()) {
            case "METHOD":
                addMethodData(model, embed);
                break;
            case "CLASS":
                addClassData(model, embed);
                break;
            case "INTERFACE":
                addInterfaceData(model, embed);
                break;
            case "FIELD":
                addFieldData(model, embed);
                break;
            /*case "ANNOTATION":
                addAnnotationData(model, embed);
                break;*/
            case "ENUM":
                addEnumData(model, embed);
                break;
            case "CONSTRUCTOR":
                addConstructorData(model, embed);
                break;
            default:
                logger.warn("Could not get javadoc type: " + model.getObject().getType());
                break;
        }
    }

    private void addMethodData(@NotNull JavadocModel model, @NotNull EmbedBuilder embed) {
        StringBuilder title = new StringBuilder();
        for (String annotation : model.getObject().getAnnotations()) {
            title.append('@');
            title.append(annotation);
            title.append(' ');
        }
        for (String modifier : model.getObject().getModifiers()) {
            title.append(modifier);
            title.append(' ');
        }
        title.append(model.getObject().getMetadata().getReturns());
        title.append(' ');
        title.append(model.getObject().getPackageName());
        title.append('.');
        title.append(model.getObject().getMetadata().getOwner());
        title.append('#');
        if (model.getObject().isDeprecated()) {
            title.append("~~");
        }
        title.append(model.getObject().getName());
        title.append('(');
        for (String param : model.getObject().getMetadata().getParameters()) {
            title.append(param);
            title.append(", ");
        }
        if (!model.getObject().getMetadata().getParameters().isEmpty()) {
            title.delete(title.length() - 2, title.length());
        }
        title.append(')');
        if (model.getObject().isDeprecated()) {
            title.append("~~");
        }

        embed.setTitle(title.toString(), model.getObject().getLink());
        if (model.getObject().isDeprecated()) {
            embed.addField("\u2757 DEPRECATED", "`" + model.getObject().getDeprecationMessage() + "`", false);
        }
        String description = model.getObject().getStrippedDescription();
        if (description.length() > 250) {
            description = description.substring(0, 250) + "...";
        }
        embed.addField("Description", "```" + description + "```", false);

        StringBuilder params = new StringBuilder();
        for (Map.Entry<String, Object> kvp : model.getObject().getMetadata().getParameterDescriptions().entrySet()) {
            params.append("**" + kvp.getKey() + "**");
            params.append(' ');
            params.append("*" + kvp.getValue() + "*");
            params.append('\n');
        }
        if (params.length() > 0) {
            params.deleteCharAt(params.length() - 1);
            embed.addField("Parameters", params.toString(), false);
        }

        if (!model.getObject().getMetadata().getReturns().equals("void")) {
            embed.addField("Returns ", "**" + model.getObject().getMetadata().getReturns() + "** *" + model.getObject().getMetadata().getReturnsDescription() + "*", false);
        }

        StringBuilder javadocThrows = new StringBuilder();
        for (JavadocModel.JavadocObjectModel.JavadocMetadataModel.JavadocThrowsModel throwsModel : model.getObject().getMetadata().getJavadocThrows()) {
            javadocThrows.append("**" + throwsModel.getKey() + "**");
            javadocThrows.append(' ');
            javadocThrows.append("*" + throwsModel.getValue() + "*");
            javadocThrows.append('\n');
        }
        if (javadocThrows.length() > 0) {
            javadocThrows.deleteCharAt(javadocThrows.length() - 1);
            embed.addField("Throws", javadocThrows.toString(), false);
        }
    }

    private void addClassData(@NotNull JavadocModel model, @NotNull EmbedBuilder embed) {
        String link = model.getObject().getLink();

        StringBuilder title = new StringBuilder();
        for (String annotation : model.getObject().getAnnotations()) {
            title.append('@');
            title.append(annotation);
            title.append(' ');
        }
        for (String modifier : model.getObject().getModifiers()) {
            title.append(modifier);
            title.append(' ');
        }
        if (model.getObject().isDeprecated()) {
            title.append("~~");
        }
        title.append(model.getObject().getPackageName());
        title.append('.');
        title.append(model.getObject().getName());
        if (model.getObject().isDeprecated()) {
            title.append("~~");
        }
        if (!model.getObject().getMetadata().getExtensions().isEmpty()) {
            title.append(" extends");
        }
        for (String extension : model.getObject().getMetadata().getExtensions()) {
            title.append(' ');
            title.append(extension.substring(extension.lastIndexOf('.') + 1));
            title.append(", ");
        }
        if (!model.getObject().getMetadata().getExtensions().isEmpty()) {
            title.delete(title.length() - 2, title.length());
        }
        if (!model.getObject().getMetadata().getImplementations().isEmpty()) {
            title.append(" implements");
        }
        for (String implementation : model.getObject().getMetadata().getImplementations()) {
            title.append(' ');
            title.append(implementation.substring(implementation.lastIndexOf('.') + 1));
            title.append(", ");
        }
        if (!model.getObject().getMetadata().getImplementations().isEmpty()) {
            title.delete(title.length() - 2, title.length());
        }

        embed.setTitle(title.toString(), model.getObject().getLink());
        if (model.getObject().isDeprecated()) {
            embed.addField("\u2757 DEPRECATED", "`" + model.getObject().getDeprecationMessage() + "`", false);
        }
        String description = model.getObject().getStrippedDescription();
        if (description.length() > 250) {
            description = description.substring(0, 250) + "...";
        }
        embed.addField("Description", "```" + description + "```", false);

        if (!model.getObject().getMetadata().getAllImplementations().isEmpty()) {
            StringBuilder implementations = new StringBuilder();
            for (String implementation : model.getObject().getMetadata().getAllImplementations()) {
                implementations.append("**" + implementation.substring(implementation.lastIndexOf('.') + 1) + "**");
                implementations.append(" \u2014 ");
            }
            implementations.delete(implementations.length() - 3, implementations.length());
            embed.addField("All Implemented Interfaces", implementations.toString(), false);
        }

        if (!model.getObject().getMetadata().getSubClasses().isEmpty()) {
            StringBuilder subclasses = new StringBuilder();
            for (String subclass : model.getObject().getMetadata().getSubClasses()) {
                subclasses.append("**" + subclass.substring(subclass.lastIndexOf('.') + 1) + "**");
                subclasses.append(" \u2014 ");
            }
            subclasses.delete(subclasses.length() - 3, subclasses.length());
            embed.addField("Subclasses", subclasses.toString(), false);
        }

        if (!model.getObject().getMetadata().getMethods().isEmpty()) {
            StringBuilder methods = new StringBuilder();
            for (String method : model.getObject().getMetadata().getMethods()) {
                methods.append("*" + method.substring(method.lastIndexOf('#') + 1) + "*");
                methods.append(" \u2014 ");
            }
            methods.delete(methods.length() - 3, methods.length());
            embed.addField("Methods", methods.toString(), false);
        }

        if (!model.getObject().getMetadata().getFields().isEmpty()) {
            StringBuilder fields = new StringBuilder();
            for (String field : model.getObject().getMetadata().getFields()) {
                fields.append("[" + field.substring(field.lastIndexOf('%') + 1) + "](" + link + "#" + field.substring(field.lastIndexOf('%') + 1) + ")");
                fields.append(" \u2014 ");
            }
            fields.delete(fields.length() - 3, fields.length());
            embed.addField("Fields", fields.toString(), false);
        }
    }

    private void addInterfaceData(@NotNull JavadocModel model, @NotNull EmbedBuilder embed) {
        String link = model.getObject().getLink();

        StringBuilder title = new StringBuilder();
        for (String annotation : model.getObject().getAnnotations()) {
            title.append('@');
            title.append(annotation);
            title.append(' ');
        }
        for (String modifier : model.getObject().getModifiers()) {
            title.append(modifier);
            title.append(' ');
        }
        if (model.getObject().isDeprecated()) {
            title.append("~~");
        }
        title.append(model.getObject().getPackageName());
        title.append('.');
        title.append(model.getObject().getName());
        if (model.getObject().isDeprecated()) {
            title.append("~~");
        }
        if (!model.getObject().getMetadata().getSuperInterfaces().isEmpty()) {
            title.append(" extends");
        }
        for (String extension : model.getObject().getMetadata().getSuperInterfaces()) {
            title.append(' ');
            title.append(extension.substring(extension.lastIndexOf('.') + 1));
            title.append(", ");
        }
        if (!model.getObject().getMetadata().getExtensions().isEmpty()) {
            title.delete(title.length() - 2, title.length());
        }

        embed.setTitle(title.toString(), model.getObject().getLink());
        if (model.getObject().isDeprecated()) {
            embed.addField("\u2757 DEPRECATED", "`" + model.getObject().getDeprecationMessage() + "`", false);
        }
        String description = model.getObject().getStrippedDescription();
        if (description.length() > 250) {
            description = description.substring(0, 250) + "...";
        }
        embed.addField("Description", "```" + description + "```", false);

        if (!model.getObject().getMetadata().getSubInterfaces().isEmpty()) {
            StringBuilder subinterfaces = new StringBuilder();
            for (String subinterface : model.getObject().getMetadata().getSubInterfaces()) {
                subinterfaces.append("**" + subinterface.substring(subinterface.lastIndexOf('.') + 1) + "**");
                subinterfaces.append(" \u2014 ");
            }
            subinterfaces.delete(subinterfaces.length() - 3, subinterfaces.length());
            embed.addField("All Subinterfaces", subinterfaces.toString(), false);
        }

        if (!model.getObject().getMetadata().getImplementingClasses().isEmpty()) {
            StringBuilder implementations = new StringBuilder();
            for (String implementation : model.getObject().getMetadata().getImplementingClasses()) {
                implementations.append("**" + implementation.substring(implementation.lastIndexOf('.') + 1) + "**");
                implementations.append(" \u2014 ");
            }
            implementations.delete(implementations.length() - 3, implementations.length());
            embed.addField("All Implementing Classes", implementations.toString(), false);
        }

        if (!model.getObject().getMetadata().getMethods().isEmpty()) {
            StringBuilder methods = new StringBuilder();
            for (String method : model.getObject().getMetadata().getMethods()) {
                methods.append("*" + method.substring(method.lastIndexOf('#') + 1) + "*");
                methods.append(" \u2014 ");
            }
            methods.delete(methods.length() - 3, methods.length());
            embed.addField("Methods", methods.toString(), false);
        }

        if (!model.getObject().getMetadata().getFields().isEmpty()) {
            StringBuilder fields = new StringBuilder();
            for (String field : model.getObject().getMetadata().getFields()) {
                fields.append("[" + field.substring(field.lastIndexOf('%') + 1) + "](" + link + "#" + field.substring(field.lastIndexOf('%') + 1) + ")");
                fields.append(" \u2014 ");
            }
            fields.delete(fields.length() - 3, fields.length());
            embed.addField("Fields", fields.toString(), false);
        }
    }

    private void addFieldData(@NotNull JavadocModel model, @NotNull EmbedBuilder embed) {
        StringBuilder title = new StringBuilder();
        for (String annotation : model.getObject().getAnnotations()) {
            title.append('@');
            title.append(annotation);
            title.append(' ');
        }
        for (String modifier : model.getObject().getModifiers()) {
            title.append(modifier);
            title.append(' ');
        }
        title.append(model.getObject().getMetadata().getReturns());
        title.append(' ');
        if (model.getObject().isDeprecated()) {
            title.append("~~");
        }
        title.append(model.getObject().getPackageName());
        title.append('.');
        title.append(model.getObject().getMetadata().getOwner());
        title.append('#');
        title.append(model.getObject().getName());
        if (model.getObject().isDeprecated()) {
            title.append("~~");
        }

        embed.setTitle(title.toString(), model.getObject().getLink());
        if (model.getObject().isDeprecated()) {
            embed.addField("\u2757 DEPRECATED", "`" + model.getObject().getDeprecationMessage() + "`", false);
        }
        String description = model.getObject().getStrippedDescription();
        if (description.length() > 250) {
            description = description.substring(0, 250) + "...";
        }
        embed.addField("Description", "```" + description + "```", false);
    }

    private void addEnumData(@NotNull JavadocModel model, @NotNull EmbedBuilder embed) {
        String link = model.getObject().getLink();

        StringBuilder title = new StringBuilder();
        for (String annotation : model.getObject().getAnnotations()) {
            title.append('@');
            title.append(annotation);
            title.append(' ');
        }
        for (String modifier : model.getObject().getModifiers()) {
            title.append(modifier);
            title.append(' ');
        }
        title.append("enum ");
        if (model.getObject().isDeprecated()) {
            title.append("~~");
        }
        title.append(model.getObject().getPackageName());
        title.append('.');
        title.append(model.getObject().getName());
        if (model.getObject().isDeprecated()) {
            title.append("~~");
        }
        if (!model.getObject().getMetadata().getImplementations().isEmpty()) {
            title.append(" implements");
        }
        for (String implementation : model.getObject().getMetadata().getImplementations()) {
            title.append(' ');
            title.append(implementation.substring(implementation.lastIndexOf('.') + 1));
            title.append(", ");
        }
        if (!model.getObject().getMetadata().getImplementations().isEmpty()) {
            title.delete(title.length() - 2, title.length());
        }

        embed.setTitle(title.toString(), model.getObject().getLink());
        if (model.getObject().isDeprecated()) {
            embed.addField("\u2757 DEPRECATED", "`" + model.getObject().getDeprecationMessage() + "`", false);
        }
        String description = model.getObject().getStrippedDescription();
        if (description.length() > 250) {
            description = description.substring(0, 250) + "...";
        }
        embed.addField("Description", "```" + description + "```", false);

        if (!model.getObject().getMetadata().getAllImplementations().isEmpty()) {
            StringBuilder implementations = new StringBuilder();
            for (String implementation : model.getObject().getMetadata().getAllImplementations()) {
                implementations.append("**" + implementation.substring(implementation.lastIndexOf('.') + 1) + "**");
                implementations.append(" \u2014 ");
            }
            implementations.delete(implementations.length() - 3, implementations.length());
            embed.addField("All Implemented Interfaces", implementations.toString(), false);
        }

        if (!model.getObject().getMetadata().getMethods().isEmpty()) {
            StringBuilder methods = new StringBuilder();
            for (String method : model.getObject().getMetadata().getMethods()) {
                methods.append("*" + method.substring(method.lastIndexOf('#') + 1) + "*");
                methods.append(" \u2014 ");
            }
            methods.delete(methods.length() - 3, methods.length());
            embed.addField("Methods", methods.toString(), false);
        }

        if (!model.getObject().getMetadata().getFields().isEmpty()) {
            StringBuilder fields = new StringBuilder();
            for (String field : model.getObject().getMetadata().getFields()) {
                fields.append("[" + field.substring(field.lastIndexOf('%') + 1) + "](" + link + "#" + field.substring(field.lastIndexOf('%') + 1) + ")");
                fields.append(" \u2014 ");
            }
            fields.delete(fields.length() - 3, fields.length());
            embed.addField("Fields", fields.toString(), false);
        }
    }

    private void addConstructorData(@NotNull JavadocModel model, @NotNull EmbedBuilder embed) {
        StringBuilder title = new StringBuilder();
        for (String annotation : model.getObject().getAnnotations()) {
            title.append('@');
            title.append(annotation);
            title.append(' ');
        }
        for (String modifier : model.getObject().getModifiers()) {
            title.append(modifier);
            title.append(' ');
        }
        title.append(model.getObject().getPackageName());
        title.append('.');
        title.append(model.getObject().getMetadata().getOwner());
        title.append('#');
        if (model.getObject().isDeprecated()) {
            title.append("~~");
        }
        title.append(model.getObject().getName());
        title.append('(');
        for (String param : model.getObject().getMetadata().getParameters()) {
            title.append(param);
            title.append(", ");
        }
        if (!model.getObject().getMetadata().getParameters().isEmpty()) {
            title.delete(title.length() - 2, title.length());
        }
        title.append(')');
        if (model.getObject().isDeprecated()) {
            title.append("~~");
        }

        embed.setTitle(title.toString(), model.getObject().getLink());
        if (model.getObject().isDeprecated()) {
            embed.addField("\u2757 DEPRECATED", "`" + model.getObject().getDeprecationMessage() + "`", false);
        }
        String description = model.getObject().getStrippedDescription();
        if (description.length() > 250) {
            description = description.substring(0, 250) + "...";
        }
        embed.addField("Description", "```" + description + "```", false);

        StringBuilder params = new StringBuilder();
        for (Map.Entry<String, Object> kvp : model.getObject().getMetadata().getParameterDescriptions().entrySet()) {
            params.append("**" + kvp.getKey() + "**");
            params.append(' ');
            params.append("*" + kvp.getValue() + "*");
            params.append('\n');
        }
        if (params.length() > 0) {
            params.deleteCharAt(params.length() - 1);
            embed.addField("Parameters", params.toString(), false);
        }

        StringBuilder javadocThrows = new StringBuilder();
        for (JavadocModel.JavadocObjectModel.JavadocMetadataModel.JavadocThrowsModel throwsModel : model.getObject().getMetadata().getJavadocThrows()) {
            javadocThrows.append("**" + throwsModel.getKey() + "**");
            javadocThrows.append(' ');
            javadocThrows.append("*" + throwsModel.getValue() + "*");
            javadocThrows.append('\n');
        }
        if (javadocThrows.length() > 0) {
            javadocThrows.deleteCharAt(javadocThrows.length() - 1);
            embed.addField("Throws", javadocThrows.toString(), false);
        }
    }
}
