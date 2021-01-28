package me.egg82.echo.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.Transaction;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import java.io.File;
import java.util.*;
import javax.persistence.PersistenceException;
import me.egg82.echo.storage.models.BaseModel;
import me.egg82.echo.storage.models.DataModel;
import me.egg82.echo.storage.models.MessageModel;
import me.egg82.echo.storage.models.query.QMessageModel;
import me.egg82.echo.storage.models.query.QDataModel;
import me.egg82.echo.utils.VersionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.ResourcesScanner;

public abstract class AbstractJDBCStorageService extends AbstractStorageService {
    protected Database connection;
    protected HikariDataSource source;

    protected AbstractJDBCStorageService(@NotNull String name) {
        super(name);
    }

    public void close() {
        queueLock.writeLock().lock();
        try {
            closed = true;
            connection.shutdown(false, false);
            source.close();
        } finally {
            queueLock.writeLock().unlock();
        }
    }

    public void storeModel(@NotNull BaseModel model) {
        queueLock.readLock().lock();
        try {
            createOrUpdate(model, false);
        } finally {
            queueLock.readLock().unlock();
        }
    }

    public void storeModels(@NotNull Collection<? extends BaseModel> models) {
        queueLock.readLock().lock();
        try (Transaction tx = connection.beginTransaction()) {
            for (BaseModel model : models) {
                createOrUpdate(model, true);
            }
            tx.commit();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    public void deleteModel(@NotNull BaseModel model) {
        BaseModel newModel = duplicateModel(model, true);
        if (newModel == null) {
            return;
        }

        queueLock.readLock().lock();
        try {
            connection.delete(newModel);
        } finally {
            queueLock.readLock().unlock();
        }
    }

    public @NotNull MessageModel getOrCreateMessageModel(@NotNull String message) {
        queueLock.readLock().lock();
        try {
            MessageModel model = new QMessageModel(connection)
                    .message.equalTo(message)
                    .findOne();
            if (model == null) {
                model = new MessageModel();
                model.setMessage(message);
                connection.save(model);
                model = new QMessageModel(connection)
                        .message.equalTo(message)
                        .findOne();
                if (model == null) {
                    throw new PersistenceException("findOne() returned null after saving.");
                }
            }
            return model;
        } finally {
            queueLock.readLock().unlock();
        }
    }

    public @Nullable MessageModel getMessageModel(@NotNull String message) {
        queueLock.readLock().lock();
        try {
            return new QMessageModel(connection)
                    .message.equalTo(message)
                    .findOne();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    public @Nullable MessageModel getMessageModel(long messageId) {
        queueLock.readLock().lock();
        try {
            return new QMessageModel(connection)
                    .id.equalTo(messageId)
                    .findOne();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    public @NotNull Set<MessageModel> getAllMessages(int start, int end) {
        queueLock.readLock().lock();
        try {
            return new QMessageModel(connection)
                    .id.between(start - 1, end + 1)
                    .findSet();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    public @NotNull DataModel getOrCreateDataModel(@NotNull String key, String value) {
        queueLock.readLock().lock();
        try {
            DataModel model = new QDataModel(connection)
                .key.equalTo(key)
                .findOne();
            if (model == null) {
                model = new DataModel();
                model.setKey(key);
                model.setValue(value);
                connection.save(model);
                model = new QDataModel(connection)
                    .key.equalTo(key)
                    .findOne();
                if (model == null) {
                    throw new PersistenceException("findOne() returned null after saving.");
                }
            }
            if (!Objects.equals(model.getValue(), value)) {
                model.setValue(value);
                model.setModified(null);
                connection.save(model);
            }
            return model;
        } finally {
            queueLock.readLock().unlock();
        }
    }

    public @Nullable DataModel getDataModel(@NotNull String key) {
        queueLock.readLock().lock();
        try {
            return new QDataModel(connection)
                .key.equalTo(key)
                .findOne();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    public @Nullable DataModel getDataModel(long dataId) {
        queueLock.readLock().lock();
        try {
            return new QDataModel(connection)
                .id.equalTo(dataId)
                .findOne();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    protected final void createSource(HikariConfig config, DatabasePlatform platform, boolean quote, String scriptsName) {
        config.setAutoCommit(false);
        source = new HikariDataSource(config);
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setDataSource(source);
        dbConfig.setDatabasePlatform(platform);
        dbConfig.setAllQuotedIdentifiers(quote);
        dbConfig.setDefaultServer(false);
        dbConfig.setRegister(false);
        dbConfig.setName(name);
        dbConfig.setClasses(Arrays.asList(BaseModel.class, MessageModel.class, DataModel.class));
        connection = DatabaseFactory.createWithContextClassLoader(dbConfig, getClass().getClassLoader());

        DataModel model;
        try {
            model = getDataModel("schema-version");
        } catch (PersistenceException ignored) {
            connection.script().run("/db/" + scriptsName + ".sql");
            model = getDataModel("schema-version");
        }
        if (model == null) {
            model = new DataModel();
            model.setKey("schema-version");
            model.setValue("1.0");
        }
        if (model.getValue() == null) {
            model.setValue("1.0");
            model.setModified(null);
        }

        List<File> files = getResourceDirs("db.migration");
        for (File file : files) {
            if (!VersionUtil.isAtLeast(file.getParentFile().getName().substring(1), '_', model.getValue(), '.') && file.getName().equals(scriptsName + ".sql")) {
                connection.script().run("/" + file.getPath().replace('\\', '/'));
                model.setValue(file.getParentFile().getName().substring(1).replace('_', '.'));
                model.setModified(null);
            }
        }

        if (!files.isEmpty() && !VersionUtil.isAtLeast(model.getValue(), '.', files.get(files.size() - 1).getParentFile().getName().substring(1), '_')) {
            throw new PersistenceException("This bot is running against a database with a higher version than expected and requires an update to continue.");
        }

        if (model.getModified() == null) {
            connection.save(model);
        }
    }

    private List<File> getResourceDirs(@NotNull String prefix) {
        List<File> retVal = new ArrayList<>();

        Reflections reflections = new Reflections(prefix, new ResourcesScanner());
        Set<String> files;
        try {
            files = reflections.getResources(x -> true);
        } catch (ReflectionsException ex) {
            return retVal;
        }

        for (String file : files) {
            retVal.add(new File(file));
        }

        retVal.sort((f1, f2) -> {
            int[] v1 = VersionUtil.parseVersion(f1.getParentFile().getName().substring(1), '_');
            int[] v2 = VersionUtil.parseVersion(f2.getParentFile().getName().substring(1), '_');

            for (int i = 0; i < v1.length; i++) {
                if (i > v2.length) {
                    // We're looking for a version deeper than what we have
                    // eg. 1.12.2 -> 1.12
                    return 1;
                }
                if (v2[i] < v1[i]) {
                    // The version we're at now is less than the one we want
                    // eg. 1.13 -> 1.11
                    return -1;
                }
            }
            return 0;
        });
        return retVal;
    }

    private @Nullable BaseModel duplicateModel(@NotNull BaseModel model, boolean keepModified) {
        BaseModel retVal = null;
        if (model instanceof MessageModel) {
            MessageModel m = new MessageModel();
            m.setMessage(((MessageModel) model).getMessage());
            retVal = m;
        } else if (model instanceof DataModel) {
            DataModel m = new DataModel();
            m.setKey(((DataModel) model).getKey());
            m.setValue(((DataModel) model).getValue());
            retVal = m;
        }

        if (retVal != null) {
            retVal.setCreated(model.getCreated());
            retVal.setModified(keepModified ? model.getModified() : null);
        } else {
            logger.error("duplicateModel is returning null.");
        }

        return retVal;
    }

    private void createOrUpdate(@NotNull BaseModel model, boolean keepModified) {
        if (model instanceof MessageModel) {
            MessageModel m = new QMessageModel(connection)
                    .message.equalTo(((MessageModel) model).getMessage())
                    .findOne();
            if (m == null) {
                m = (MessageModel) duplicateModel(model, keepModified);
                if (m == null) {
                    return;
                }
                connection.save(m);
            } else {
                m.setCreated(model.getCreated());
                m.setModified(keepModified ? model.getModified() : null);
                connection.update(m);
            }
        } else if (model instanceof DataModel) {
            DataModel m = new QDataModel(connection)
                .key.equalTo(((DataModel) model).getKey())
                .findOne();
            if (m == null) {
                m = (DataModel) duplicateModel(model, keepModified);
                if (m == null) {
                    return;
                }
                connection.save(m);
            } else {
                m.setKey(((DataModel) model).getKey());
                m.setValue(((DataModel) model).getValue());
                m.setCreated(model.getCreated());
                m.setModified(keepModified ? model.getModified() : null);
                connection.update(m);
            }
        }
    }
}
