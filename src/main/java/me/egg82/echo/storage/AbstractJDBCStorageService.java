package me.egg82.echo.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.Transaction;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import me.egg82.echo.storage.models.*;
import me.egg82.echo.storage.models.query.*;
import me.egg82.echo.utils.VersionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.ResourcesScanner;

import javax.persistence.PersistenceException;
import java.io.File;
import java.time.Instant;
import java.util.*;

public abstract class AbstractJDBCStorageService extends AbstractStorageService {
    protected Database connection;
    protected HikariDataSource source;

    protected AbstractJDBCStorageService(@NotNull String name) {
        super(name);
    }

    @Override
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

    @Override
    public void storeModel(@NotNull BaseModel model) {
        queueLock.readLock().lock();
        try {
            createOrUpdate(model, false);
        } finally {
            queueLock.readLock().unlock();
        }
    }

    @Override
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

    @Override
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

    @Override
    public @NotNull ShowModel getOrCreateShowModel(long tvdbId, int season, int episode) {
        queueLock.readLock().lock();
        try {
            ShowModel model = new QShowModel(connection)
                    .tvdb.equalTo(tvdbId)
                    .findOne();
            if (model == null) {
                model = new ShowModel();
                model.setTvdb(tvdbId);
                model.setSeason(season);
                model.setEpisode(episode);
                connection.save(model);
                model = new QShowModel(connection)
                        .tvdb.equalTo(tvdbId)
                        .findOne();
                if (model == null) {
                    throw new PersistenceException("findOne() returned null after saving.");
                }
            }
            if (model.getSeason() != season || model.getEpisode() != episode) {
                model.setSeason(season);
                model.setEpisode(episode);
                model.setModified(null);
                connection.save(model);
            }
            return model;
        } finally {
            queueLock.readLock().unlock();
        }
    }

    @Override
    public @Nullable ShowModel getShowModel(long idOrTvdbId) {
        queueLock.readLock().lock();
        try {
            ShowModel retVal = new QShowModel(connection)
                    .tvdb.equalTo(idOrTvdbId)
                    .findOne();
            if (retVal == null) {
                retVal = new QShowModel(connection)
                        .id.equalTo(idOrTvdbId)
                        .findOne();
            }
            return retVal;
        } finally {
            queueLock.readLock().unlock();
        }
    }

    @Override
    public @NotNull Set<ShowModel> getAllShows(int start, int max) {
        queueLock.readLock().lock();
        try {
            return new QShowModel(connection)
                    .id.between(start, start + max - 1)
                    .findSet();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    @Override
    public @NotNull WebModel getOrCreateWebModel(@NotNull String hash, @NotNull String service, @NotNull String path) {
        queueLock.readLock().lock();
        try {
            WebModel model = new QWebModel(connection)
                    .hash.equalTo(hash)
                    .service.equalTo(service)
                    .findOne();
            if (model == null) {
                model = new WebModel();
                model.setHash(hash);
                model.setPath(path);
                connection.save(model);
                model = new QWebModel(connection)
                        .hash.equalTo(hash)
                        .service.equalTo(service)
                        .findOne();
                if (model == null) {
                    throw new PersistenceException("findOne() returned null after saving.");
                }
            }
            if (!Objects.equals(model.getPath(), path)) {
                model.setPath(path);
                model.setModified(null);
                connection.save(model);
            }
            return model;
        } finally {
            queueLock.readLock().unlock();
        }
    }

    @Override
    public @Nullable WebModel getWebModel(@NotNull String hash, @NotNull String service) {
        queueLock.readLock().lock();
        try {
            return new QWebModel(connection)
                    .hash.equalTo(hash)
                    .service.equalTo(service)
                    .findOne();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    @Override
    public @Nullable WebModel getWebModel(@NotNull String hash, @NotNull String service, long cacheTimeMillis) {
        queueLock.readLock().lock();
        try {
            return new QWebModel(connection)
                    .hash.equalTo(hash)
                    .service.equalTo(service)
                    .modified.after(Instant.now().minusMillis(cacheTimeMillis))
                    .findOne();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    @Override
    public @Nullable WebModel getWebModel(long uploadId) {
        queueLock.readLock().lock();
        try {
            return new QWebModel(connection)
                    .id.equalTo(uploadId)
                    .findOne();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    @Override
    public @Nullable WebModel getWebModel(long uploadId, long cacheTimeMillis) {
        queueLock.readLock().lock();
        try {
            return new QWebModel(connection)
                    .id.equalTo(uploadId)
                    .modified.after(Instant.now().minusMillis(cacheTimeMillis))
                    .findOne();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    @Override
    public @NotNull Set<WebModel> getAllWebs(long cacheTimeMillis) {
        queueLock.readLock().lock();
        try {
            return new QWebModel(connection)
                    .modified.after(Instant.now().minusMillis(cacheTimeMillis))
                    .findSet();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    @Override
    public @NotNull Set<WebModel> getAllWebs(int start, int max) {
        queueLock.readLock().lock();
        try {
            return new QWebModel(connection)
                    .id.between(start, start + max - 1)
                    .findSet();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public @NotNull Set<MessageModel> getAllMessages(int start, int max) {
        queueLock.readLock().lock();
        try {
            return new QMessageModel(connection)
                    .id.between(start, start + max - 1)
                    .findSet();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    @Override
    public @NotNull LearnModel getOrCreateLearnModel(long user, boolean learn) {
        queueLock.readLock().lock();
        try {
            LearnModel model = new QLearnModel(connection)
                    .user.equalTo(user)
                    .findOne();
            if (model == null) {
                model = new LearnModel();
                model.setUser(user);
                model.setLearning(learn);
                connection.save(model);
                model = new QLearnModel(connection)
                        .user.equalTo(user)
                        .findOne();
                if (model == null) {
                    throw new PersistenceException("findOne() returned null after saving.");
                }
            }
            if (model.isLearning() != learn) {
                model.setLearning(learn);
                model.setModified(null);
                connection.save(model);
            }
            return model;
        } finally {
            queueLock.readLock().unlock();
        }
    }

    @Override
    public @Nullable LearnModel getLearnModel(long userOrId) {
        queueLock.readLock().lock();
        try {
            LearnModel retVal = new QLearnModel(connection)
                    .user.equalTo(userOrId)
                    .findOne();
            if (retVal == null) {
                retVal = new QLearnModel(connection)
                        .id.equalTo(userOrId)
                        .findOne();
            }
            return retVal;
        } finally {
            queueLock.readLock().unlock();
        }
    }

    @Override
    public @NotNull Set<LearnModel> getAllLearning(int start, int max) {
        queueLock.readLock().lock();
        try {
            return new QLearnModel(connection)
                    .id.between(start, start + max - 1)
                    .findSet();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    @Override
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

    @Override
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

    @Override
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
        dbConfig.setClasses(Arrays.asList(BaseModel.class, ShowModel.class, WebModel.class, MessageModel.class, LearnModel.class, DataModel.class));
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
        if (model instanceof ShowModel) {
            ShowModel m = new ShowModel();
            m.setTvdb(((ShowModel) model).getTvdb());
            m.setSeason(((ShowModel) model).getSeason());
            m.setEpisode(((ShowModel) model).getEpisode());
            retVal = m;
        } else if (model instanceof WebModel) {
            WebModel m = new WebModel();
            m.setHash(((WebModel) model).getHash());
            m.setService(((WebModel) model).getService());
            m.setPath(((WebModel) model).getPath());
            retVal = m;
        } else if (model instanceof MessageModel) {
            MessageModel m = new MessageModel();
            m.setMessage(((MessageModel) model).getMessage());
            retVal = m;
        } else if (model instanceof LearnModel) {
            LearnModel m = new LearnModel();
            m.setUser(((LearnModel) model).getUser());
            m.setLearning(((LearnModel) model).isLearning());
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
        if (model instanceof ShowModel) {
            ShowModel m = new QShowModel(connection)
                    .tvdb.equalTo(((ShowModel) model).getTvdb())
                    .findOne();
            if (m == null) {
                m = (ShowModel) duplicateModel(model, keepModified);
                if (m == null) {
                    return;
                }
                connection.save(m);
            } else {
                m.setTvdb(((ShowModel) model).getTvdb());
                m.setSeason(((ShowModel) model).getSeason());
                m.setEpisode(((ShowModel) model).getEpisode());
                m.setCreated(model.getCreated());
                m.setModified(keepModified ? model.getModified() : null);
                connection.update(m);
            }
        } else if (model instanceof WebModel) {
            WebModel m = new QWebModel(connection)
                    .hash.equalTo(((WebModel) model).getHash())
                    .service.equalTo(((WebModel) model).getService())
                    .findOne();
            if (m == null) {
                m = (WebModel) duplicateModel(model, keepModified);
                if (m == null) {
                    return;
                }
                connection.save(m);
            } else {
                m.setHash(((WebModel) model).getHash());
                m.setService(((WebModel) model).getService());
                m.setPath(((WebModel) model).getPath());
                m.setCreated(model.getCreated());
                m.setModified(keepModified ? model.getModified() : null);
                connection.update(m);
            }
        } else if (model instanceof MessageModel) {
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
                m.setMessage(((MessageModel) model).getMessage());
                m.setCreated(model.getCreated());
                m.setModified(keepModified ? model.getModified() : null);
                connection.update(m);
            }
        } else if (model instanceof LearnModel) {
            LearnModel m = new QLearnModel(connection)
                    .user.equalTo(((LearnModel) model).getUser())
                    .findOne();
            if (m == null) {
                m = (LearnModel) duplicateModel(model, keepModified);
                if (m == null) {
                    return;
                }
                connection.save(m);
            } else {
                m.setUser(((LearnModel) model).getUser());
                m.setLearning(((LearnModel) model).isLearning());
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
