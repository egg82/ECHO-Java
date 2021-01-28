package me.egg82.echo.logging;

public class AnsiOutput {
    private AnsiOutput() { }

    private static final String ENCODE_JOIN = ";";
    private static Enabled enabled = Enabled.DETECT;
    private static Boolean consoleAvailable;
    private static Boolean ansiCapable;

    private static final String OPERATING_SYSTEM_NAME = System.getProperty("os.name").toLowerCase();
    private static final String ENCODE_START = "\033[";
    private static final String ENCODE_END = "m";

    public static void setEnabled(Enabled enabled) {
        AnsiOutput.enabled = enabled;
    }

    public static void setConsoleAvailable(boolean consoleAvailable) {
        AnsiOutput.consoleAvailable = consoleAvailable;
    }

    static Enabled getEnabled() {
        return AnsiOutput.enabled;
    }

    public static String encode(AnsiColor element) {
        if (isEnabled()) {
            return ENCODE_START + element + ENCODE_END;
        }
        return "";
    }

    public static String toString(Object... elements) {
        StringBuilder sb = new StringBuilder();
        if (isEnabled()) {
            buildEnabled(sb, elements);
        } else {
            buildDisabled(sb, elements);
        }
        return sb.toString();
    }

    private static void buildEnabled(StringBuilder sb, Object[] elements) {
        boolean writingAnsi = false;
        boolean containsEncoding = false;
        for (Object element : elements) {
            if (element instanceof AnsiColor) {
                containsEncoding = true;
                if (!writingAnsi) {
                    sb.append(ENCODE_START);
                    writingAnsi = true;
                } else {
                    sb.append(ENCODE_JOIN);
                }
            } else {
                if (writingAnsi) {
                    sb.append(ENCODE_END);
                    writingAnsi = false;
                }
            }
            sb.append(element);
        }
        if (containsEncoding) {
            sb.append(writingAnsi ? ENCODE_JOIN : ENCODE_START);
            sb.append(AnsiColor.RESET);
            sb.append(ENCODE_END);
        }
    }

    private static void buildDisabled(StringBuilder sb, Object[] elements) {
        for (Object element : elements) {
            if (!(element instanceof AnsiColor) && element != null) {
                sb.append(element);
            }
        }
    }

    private static boolean isEnabled() {
        if (enabled == Enabled.DETECT) {
            if (ansiCapable == null) {
                ansiCapable = detectIfAnsiCapable();
            }
            return ansiCapable;
        }
        return enabled == Enabled.ALWAYS;
    }

    private static boolean detectIfAnsiCapable() {
        try {
            if (Boolean.FALSE.equals(consoleAvailable)) {
                return false;
            }
            if ((consoleAvailable == null) && (System.console() == null)) {
                return false;
            }
            return !(OPERATING_SYSTEM_NAME.contains("win"));
        } catch (Exception ignored) {
            return false;
        }
    }

    public enum Enabled {
        DETECT,
        ALWAYS,
        NEVER
    }
}
