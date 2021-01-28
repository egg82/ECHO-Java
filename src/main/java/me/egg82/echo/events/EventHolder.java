package me.egg82.echo.events;

import java.util.ArrayList;
import java.util.List;
import ninja.egg82.events.EventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EventHolder {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final List<EventSubscriber<?>> events = new ArrayList<>();

    public final int numEvents() { return events.size(); }

    public final void cancel() {
        for (EventSubscriber<?> event : events) {
            event.cancel();
        }
    }
}