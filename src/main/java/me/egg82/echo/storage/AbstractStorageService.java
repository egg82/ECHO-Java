package me.egg82.echo.storage;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStorageService implements StorageService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected AbstractStorageService(@NotNull String name) {
        this.name = name;
    }

    protected final String name;

    public @NotNull String getName() { return name; }

    protected volatile boolean closed = false;
    protected final ReadWriteLock queueLock = new ReentrantReadWriteLock();

    public boolean isClosed() { return closed; }
}
