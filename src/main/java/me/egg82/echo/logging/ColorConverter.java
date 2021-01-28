package me.egg82.echo.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;

public class ColorConverter extends CompositeConverter<ILoggingEvent> {
    private static final Map<String, AnsiColor> ELEMENTS;

    static {
        Map<String, AnsiColor> elements = new HashMap<>();
        elements.put("faint", AnsiColor.FAINT);
        elements.put("red", AnsiColor.RED);
        elements.put("green", AnsiColor.GREEN);
        elements.put("yellow", AnsiColor.YELLOW);
        elements.put("blue", AnsiColor.BLUE);
        elements.put("magenta", AnsiColor.MAGENTA);
        elements.put("cyan", AnsiColor.CYAN);
        ELEMENTS = ImmutableMap.copyOf(elements);
    }

    private static final Map<Integer, AnsiColor> LEVELS;

    static {
        Map<Integer, AnsiColor> levels = new HashMap<>();
        levels.put(Level.ERROR_INTEGER, AnsiColor.RED);
        levels.put(Level.WARN_INTEGER, AnsiColor.YELLOW);
        LEVELS = ImmutableMap.copyOf(levels);
    }

    protected final String transform(ILoggingEvent event, String in) {
        AnsiColor element = ELEMENTS.get(getFirstOption());
        if (element == null) {
            element = LEVELS.get(event.getLevel().toInteger());
            element = (element == null ? AnsiColor.GREEN : element);
        }
        return toAnsiString(in, element);
    }

    private String toAnsiString(String in, AnsiColor element) {
        return AnsiOutput.toString(element, in);
    }
}
