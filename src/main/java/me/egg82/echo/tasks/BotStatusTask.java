package me.egg82.echo.tasks;

import flexjson.JSONDeserializer;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import me.egg82.echo.config.CachedConfig;
import me.egg82.echo.core.GameStatus;
import me.egg82.echo.core.Pair;
import me.egg82.echo.messaging.packets.ShowPacket;
import me.egg82.echo.storage.StorageService;
import me.egg82.echo.storage.models.ShowModel;
import me.egg82.echo.utils.*;
import me.egg82.echo.web.models.EpisodeModel;
import me.egg82.echo.web.models.SeasonModel;
import me.egg82.echo.web.models.TrendingShowModel;
import me.egg82.echo.web.transformers.InstantTransformer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BotStatusTask extends AbstractTask {
    private static final Logger logger = LoggerFactory.getLogger(BotStatusTask.class);

    private static final String TRENDING_URL = "https://api.trakt.tv/shows/trending";
    private static final String SEASONS_URL = "https://api.trakt.tv/shows/%d/seasons?extended=full";
    private static final String EPISODE_URL = "https://api.trakt.tv/shows/%d/seasons/%d/episodes/%d?extended=full";

    private final Random random = new Random();

    public BotStatusTask(@NotNull JDA jda, @NotNull IntList tasks) {
        super(jda, tasks);
    }

    public void accept(int id) {
        CachedConfig cachedConfig = getCachedConfig();
        if (cachedConfig == null) {
            return;
        }

        if (random.nextDouble() <= cachedConfig.getLaziness()) {
            if (!cachedConfig.getTraktKey().isEmpty() && random.nextDouble() <= 0.5d) {
                getTrendingModel(cachedConfig.getTraktKey())
                        .thenApplyAsync(v -> {
                            if (v == null) {
                                return null;
                            }

                            List<TrendingShowModel> tempShowList = new ArrayList<>(v);

                            int seasonNum = -1;
                            int episodeNum = -1;
                            TrendingShowModel trendingModel;
                            EpisodeModel episodeModel = null;

                            do {
                                int index = random.nextInt(tempShowList.size());
                                trendingModel = tempShowList.get(index);

                                ShowModel dbModel = null;
                                for (StorageService service : cachedConfig.getStorage()) {
                                    dbModel = service.getOrCreateShowModel(trendingModel.getShow().getIds().getTvdb(), 1, 0);
                                }

                                if (dbModel == null) {
                                    logger.error("dbModel was null.");
                                    return null;
                                }

                                List<SeasonModel> seasons = getSeasonsModel(cachedConfig.getTraktKey(), trendingModel.getShow().getIds().getTrakt()).join();
                                for (SeasonModel season : seasons) {
                                    if (season.getNumber() == dbModel.getSeason() && season.getAiredEpisodes() > dbModel.getEpisode()) {
                                        // Same season, new episode
                                        for (StorageService service : cachedConfig.getStorage()) {
                                            ShowModel m = service.getOrCreateShowModel(trendingModel.getShow().getIds().getTvdb(), dbModel.getSeason(), dbModel.getEpisode() + 1);
                                            boolean modified = false;
                                            if (m.getSeason() != dbModel.getSeason()) {
                                                m.setSeason(dbModel.getSeason());
                                                modified = true;
                                            }
                                            if (m.getEpisode() != dbModel.getEpisode() + 1) {
                                                m.setEpisode(dbModel.getEpisode() + 1);
                                                modified = true;
                                            }
                                            if (modified) {
                                                service.storeModel(m);
                                            }
                                        }
                                        seasonNum = dbModel.getSeason();
                                        episodeNum = dbModel.getEpisode() + 1;
                                        break;
                                    } else if (season.getNumber() > dbModel.getSeason()) {
                                        // New season
                                        for (StorageService service : cachedConfig.getStorage()) {
                                            ShowModel m = service.getOrCreateShowModel(trendingModel.getShow().getIds().getTvdb(), season.getNumber(), 1);
                                            boolean modified = false;
                                            if (m.getSeason() != season.getNumber()) {
                                                m.setSeason(season.getNumber());
                                                modified = true;
                                            }
                                            if (m.getEpisode() != 1) {
                                                m.setEpisode(1);
                                                modified = true;
                                            }
                                            if (modified) {
                                                service.storeModel(m);
                                            }
                                        }
                                        seasonNum = season.getNumber();
                                        episodeNum = 1;
                                        break;
                                    }
                                }

                                if (seasonNum != -1 && episodeNum != -1) {
                                    episodeModel = getEpisodeModel(cachedConfig.getTraktKey(), trendingModel.getShow().getIds().getTrakt(), seasonNum, episodeNum).join();
                                    if (episodeModel != null) {
                                        ResponseUtil.learn(cachedConfig, episodeModel.getOverview());
                                    }
                                }

                                if (episodeModel == null) {
                                    tempShowList.remove(index);
                                    continue;
                                }

                                ShowPacket packet = new ShowPacket();
                                packet.setTvdb(trendingModel.getShow().getIds().getTvdb());
                                packet.setSeason(seasonNum);
                                packet.setEpisode(episodeNum);
                                packet.setOverview(episodeModel.getOverview());
                                PacketUtil.queuePacket(packet);
                                break;
                            } while (!tempShowList.isEmpty());

                            if (episodeModel == null) {
                                return null;
                            }

                            return new Pair<>(trendingModel.getShow().getTitle(), episodeModel);
                        })
                        .whenCompleteAsync((val, ex) -> {
                            if (!canCompleteContinue(val, ex)) {
                                return;
                            }

                            jda.getPresence().setActivity(Activity.watching(val.getT1() + " S" + val.getT2().getSeason() + "E" + val.getT2().getNumber()));

                            TaskScheduler.cancelTask(id);
                            tasks.rem(id);
                            tasks.add(TaskScheduler.createRepeatingTask(
                                    new BotStatusTask(jda, tasks),
                                    new TimeUtil.Time(val.getT2().getRuntime(), TimeUnit.MINUTES),
                                    new TimeUtil.Time(10L, TimeUnit.MINUTES)
                            ));
                        });

                jda.getPresence().setActivity(Activity.playing("\"find a show\""));
                return;
            } else if (!cachedConfig.getGames().isEmpty()) {
                GameStatus status;
                int tries = 0;
                do {
                    status = cachedConfig.getGames().get(random.nextInt(cachedConfig.getGames().size()));
                    tries++;
                } while (Activity.playing(status.getDisplayName()).equals(jda.getPresence().getActivity()) && tries < 25);

                jda.getPresence().setActivity(Activity.playing(status.getDisplayName()));

                long time = (long) (random.nextDouble() * (status.getMax().getMillis() - status.getMin().getMillis()) + status.getMin().getMillis());

                TaskScheduler.cancelTask(id);
                tasks.rem(id);
                tasks.add(TaskScheduler.createRepeatingTask(
                        new BotStatusTask(jda, tasks),
                        new TimeUtil.Time(time, TimeUnit.MILLISECONDS),
                        new TimeUtil.Time(10L, TimeUnit.MINUTES)
                ));

                return;
            }
        }

        if (!jda.getGuilds().isEmpty()) {
            Guild guild = jda.getGuilds().get(random.nextInt(jda.getGuilds().size()));
            guild.findMembers(m -> !m.getUser().isBot()).onSuccess(members -> {
                if (members.isEmpty()) {
                    return;
                }

                Member member = members.get(random.nextInt(members.size()));
                jda.getPresence().setActivity(Activity.watching(member.getEffectiveName()));
            });
        }
    }

    public static CompletableFuture<List<TrendingShowModel>> getTrendingModel(@NotNull String key) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Request request = WebUtil.getDefaultRequestBuilder(new URL(TRENDING_URL))
                        .header("trakt-api-version", "2")
                        .header("trakt-api-key", key)
                        .header("Accept", "application/json")
                        .build();

                try (Response response = WebUtil.getResponse(request)) {
                    JSONDeserializer<List<TrendingShowModel>> modelDeserializer = new JSONDeserializer<>();
                    modelDeserializer.use("values", TrendingShowModel.class);
                    List<TrendingShowModel> retVal = modelDeserializer.deserialize(response.body().charStream());
                    return retVal == null || retVal.isEmpty() ? null : retVal;
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        }).exceptionally(ex -> ExceptionUtil.handleException(ex, logger));
    }

    public static CompletableFuture<List<SeasonModel>> getSeasonsModel(@NotNull String key, long traktShowId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Request request = WebUtil.getDefaultRequestBuilder(new URL(String.format(SEASONS_URL, traktShowId)))
                        .header("trakt-api-version", "2")
                        .header("trakt-api-key", key)
                        .header("Accept", "application/json")
                        .build();

                try (Response response = WebUtil.getResponse(request)) {
                    JSONDeserializer<List<SeasonModel>> modelDeserializer = new JSONDeserializer<>();
                    modelDeserializer.use("values", SeasonModel.class);
                    modelDeserializer.use(Instant.class, new InstantTransformer());
                    List<SeasonModel> retVal = modelDeserializer.deserialize(response.body().charStream());
                    if (retVal != null) {
                        retVal.sort(Comparator.comparingInt(SeasonModel::getNumber));
                    }
                    return retVal == null || retVal.isEmpty() ? null : retVal;
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        }).exceptionally(ex -> ExceptionUtil.handleException(ex, logger));
    }

    public static CompletableFuture<EpisodeModel> getEpisodeModel(@NotNull String key, long traktShowId, int season, int episode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Request request = WebUtil.getDefaultRequestBuilder(new URL(String.format(EPISODE_URL, traktShowId, season, episode)))
                        .header("trakt-api-version", "2")
                        .header("trakt-api-key", key)
                        .header("Accept", "application/json")
                        .build();

                try (Response response = WebUtil.getResponse(request)) {
                    JSONDeserializer<EpisodeModel> modelDeserializer = new JSONDeserializer<>();
                    modelDeserializer.use(Instant.class, new InstantTransformer());
                    return modelDeserializer.deserialize(response.body().charStream(), EpisodeModel.class);
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        }).exceptionally(ex -> ExceptionUtil.handleException(ex, logger));
    }
}
