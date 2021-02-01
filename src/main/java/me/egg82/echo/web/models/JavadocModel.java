package me.egg82.echo.web.models;

import flexjson.JSON;
import java.io.Serializable;
import java.util.*;
import org.jetbrains.annotations.NotNull;

public class JavadocModel implements Serializable {
    private String name = "";
    private JavadocObjectModel object = new JavadocObjectModel();

    public JavadocModel() { }

    public @NotNull String getName() { return name; }

    public void setName(@NotNull String name) { this.name = name; }

    public @NotNull JavadocObjectModel getObject() { return object; }

    public void setObject(@NotNull JavadocObjectModel object) { this.object = object; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JavadocModel)) return false;
        JavadocModel that = (JavadocModel) o;
        return name.equals(that.name) && object.equals(that.object);
    }

    public int hashCode() { return Objects.hash(name, object); }

    public String toString() {
        return "JavadocModel{" +
                "name='" + name + '\'' +
                ", object=" + object +
                '}';
    }

    public static final class JavadocObjectModel implements Serializable {
        private String link = "";
        private String type = "";
        @JSON(name = "package")
        private String packageName = "";
        private String name = "";
        private String description = "";
        @JSON(name = "stripped_description")
        private String strippedDescription = "";
        private List<String> annotations = new ArrayList<>();
        private boolean deprecated = false;
        @JSON(name = "deprecation_message")
        private String deprecationMessage = "";
        private List<String> modifiers = new ArrayList<>();
        private JavadocMetadataModel metadata = new JavadocMetadataModel();

        public JavadocObjectModel() { }

        public @NotNull String getLink() { return link; }

        public void setLink(@NotNull String link) { this.link = link; }

        public @NotNull String getType() { return type; }

        public void setType(@NotNull String type) { this.type = type; }

        public @NotNull String getPackageName() { return packageName; }

        public void setPackageName(@NotNull String packageName) { this.packageName = packageName; }

        public @NotNull String getName() { return name; }

        public void setName(@NotNull String name) { this.name = name; }

        public @NotNull String getDescription() { return description; }

        public void setDescription(@NotNull String description) { this.description = description; }

        public @NotNull String getStrippedDescription() { return strippedDescription; }

        public void setStrippedDescription(@NotNull String strippedDescription) { this.strippedDescription = strippedDescription; }

        public @NotNull List<String> getAnnotations() { return annotations; }

        public void setAnnotations(@NotNull List<String> annotations) { this.annotations = annotations; }

        public boolean isDeprecated() { return deprecated; }

        public void setDeprecated(boolean deprecated) { this.deprecated = deprecated; }

        public @NotNull String getDeprecationMessage() { return deprecationMessage; }

        public void setDeprecationMessage(@NotNull String deprecationMessage) { this.deprecationMessage = deprecationMessage; }

        public @NotNull List<String> getModifiers() { return modifiers; }

        public void setModifiers(@NotNull List<String> modifiers) { this.modifiers = modifiers; }

        public @NotNull JavadocMetadataModel getMetadata() { return metadata; }

        public void setMetadata(@NotNull JavadocMetadataModel metadata) { this.metadata = metadata; }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JavadocObjectModel)) return false;
            JavadocObjectModel that = (JavadocObjectModel) o;
            return deprecated == that.deprecated && link.equals(that.link) && type.equals(that.type) && packageName.equals(that.packageName) && name.equals(that.name) && description.equals(that.description) && strippedDescription.equals(that.strippedDescription) && annotations.equals(that.annotations) && deprecationMessage.equals(that.deprecationMessage) && modifiers.equals(that.modifiers) && metadata.equals(that.metadata);
        }

        public int hashCode() { return Objects.hash(link, type, packageName, name, description, strippedDescription, annotations, deprecated, deprecationMessage, modifiers, metadata); }

        public String toString() {
            return "JavadocObjectModel{" +
                    "link='" + link + '\'' +
                    ", type='" + type + '\'' +
                    ", packageName='" + packageName + '\'' +
                    ", name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", strippedDescription='" + strippedDescription + '\'' +
                    ", annotations=" + annotations +
                    ", deprecated=" + deprecated +
                    ", deprecationMessage='" + deprecationMessage + '\'' +
                    ", modifiers=" + modifiers +
                    ", metadata=" + metadata +
                    '}';
        }

        public static final class JavadocMetadataModel implements Serializable {
            private String owner = "";
            private List<String> parameters = new ArrayList<>();
            @JSON(name = "parameter_descriptions")
            private Map<String, Object> parameterDescriptions = new HashMap<>();
            private String returns = "";
            @JSON(name = "returns_description")
            private String returnsDescription = "";
            @JSON(name = "throws")
            private List<JavadocThrowsModel> javadocThrows = new ArrayList<>();
            private List<String> extensions = new ArrayList<>();
            private List<String> implementations = new ArrayList<>();
            @JSON(name = "all_implementations")
            private List<String> allImplementations = new ArrayList<>();
            @JSON(name = "super_interfaces")
            private List<String> superInterfaces = new ArrayList<>();
            @JSON(name = "sub_interfaces")
            private List<String> subInterfaces = new ArrayList<>();
            @JSON(name = "sub_classes")
            private List<String> subClasses = new ArrayList<>();
            @JSON(name = "implementing_classes")
            private List<String> implementingClasses = new ArrayList<>();
            private List<String> methods = new ArrayList<>();
            private List<String> fields = new ArrayList<>();

            public JavadocMetadataModel() { }

            public @NotNull String getOwner() { return owner; }

            public void setOwner(@NotNull String owner) { this.owner = owner; }

            public @NotNull List<String> getParameters() { return parameters; }

            public void setParameters(@NotNull List<String> parameters) { this.parameters = parameters; }

            public @NotNull Map<String, Object> getParameterDescriptions() { return parameterDescriptions; }

            public void setParameterDescriptions(@NotNull Map<String, Object> parameterDescriptions) { this.parameterDescriptions = parameterDescriptions; }

            public @NotNull String getReturns() { return returns; }

            public void setReturns(@NotNull String returns) { this.returns = returns; }

            public @NotNull String getReturnsDescription() { return returnsDescription; }

            public void setReturnsDescription(@NotNull String returnsDescription) { this.returnsDescription = returnsDescription; }

            public @NotNull List<JavadocThrowsModel> getJavadocThrows() { return javadocThrows; }

            public void setJavadocThrows(@NotNull List<JavadocThrowsModel> javadocThrows) { this.javadocThrows = javadocThrows; }

            public @NotNull List<String> getExtensions() { return extensions; }

            public void setExtensions(@NotNull List<String> extensions) { this.extensions = extensions; }

            public @NotNull List<String> getImplementations() { return implementations; }

            public void setImplementations(@NotNull List<String> implementations) { this.implementations = implementations; }

            public @NotNull List<String> getAllImplementations() { return allImplementations; }

            public void setAllImplementations(@NotNull List<String> allImplementations) { this.allImplementations = allImplementations; }

            public @NotNull List<String> getSuperInterfaces() { return superInterfaces; }

            public void setSuperInterfaces(@NotNull List<String> superInterfaces) { this.superInterfaces = superInterfaces; }

            public @NotNull List<String> getSubInterfaces() { return subInterfaces; }

            public void setSubInterfaces(@NotNull List<String> subInterfaces) { this.subInterfaces = subInterfaces; }

            public @NotNull List<String> getSubClasses() { return subClasses; }

            public void setSubClasses(@NotNull List<String> subClasses) { this.subClasses = subClasses; }

            public @NotNull List<String> getImplementingClasses() { return implementingClasses; }

            public void setImplementingClasses(@NotNull List<String> implementingClasses) { this.implementingClasses = implementingClasses; }

            public @NotNull List<String> getMethods() { return methods; }

            public void setMethods(@NotNull List<String> methods) { this.methods = methods; }

            public @NotNull List<String> getFields() { return fields; }

            public void setFields(@NotNull List<String> fields) { this.fields = fields; }

            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof JavadocMetadataModel)) return false;
                JavadocMetadataModel that = (JavadocMetadataModel) o;
                return owner.equals(that.owner) && parameters.equals(that.parameters) && parameterDescriptions.equals(that.parameterDescriptions) && returns.equals(that.returns) && returnsDescription.equals(that.returnsDescription) && javadocThrows.equals(that.javadocThrows) && extensions.equals(that.extensions) && implementations.equals(that.implementations) && allImplementations.equals(that.allImplementations) && superInterfaces.equals(that.superInterfaces) && subInterfaces.equals(that.subInterfaces) && subClasses.equals(that.subClasses) && implementingClasses.equals(that.implementingClasses) && methods.equals(that.methods) && fields.equals(that.fields);
            }

            public int hashCode() { return Objects.hash(owner, parameters, parameterDescriptions, returns, returnsDescription, javadocThrows, extensions, implementations, allImplementations, superInterfaces, subInterfaces, subClasses, implementingClasses, methods, fields); }

            public String toString() {
                return "JavadocMetadataModel{" +
                        "owner='" + owner + '\'' +
                        ", parameters=" + parameters +
                        ", parameterDescriptions=" + parameterDescriptions +
                        ", returns='" + returns + '\'' +
                        ", returnsDescription='" + returnsDescription + '\'' +
                        ", javadocThrows=" + javadocThrows +
                        ", extensions=" + extensions +
                        ", implementations=" + implementations +
                        ", allImplementations=" + allImplementations +
                        ", superInterfaces=" + superInterfaces +
                        ", subInterfaces=" + subInterfaces +
                        ", subClasses=" + subClasses +
                        ", implementingClasses=" + implementingClasses +
                        ", methods=" + methods +
                        ", fields=" + fields +
                        '}';
            }

            public static final class JavadocThrowsModel implements Serializable {
                private String key = "";
                private String value = "";

                public JavadocThrowsModel() { }

                public @NotNull String getKey() { return key; }

                public void setKey(@NotNull String key) { this.key = key; }

                public @NotNull String getValue() { return value; }

                public void setValue(@NotNull String value) { this.value = value; }

                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (!(o instanceof JavadocThrowsModel)) return false;
                    JavadocThrowsModel that = (JavadocThrowsModel) o;
                    return key.equals(that.key) && value.equals(that.value);
                }

                public int hashCode() { return Objects.hash(key, value); }

                public String toString() {
                    return "JavadocThrowsModel{" +
                            "key='" + key + '\'' +
                            ", value='" + value + '\'' +
                            '}';
                }
            }
        }
    }
}
