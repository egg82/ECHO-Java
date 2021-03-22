package me.egg82.echo.messaging;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.buffer.ByteBuf;
import me.egg82.echo.config.ConfigUtil;
import me.egg82.echo.messaging.packets.Packet;
import me.egg82.echo.utils.PacketUtil;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RedisMessagingService extends AbstractMessagingService {
    private final ExecutorService workPool = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("ECHO-Redis-%d").build());

    private JedisPool pool;
    private final PubSub pubSub = new PubSub(this);

    private volatile boolean closed = false;
    private final ReadWriteLock queueLock = new ReentrantReadWriteLock();

    private static final String CHANNEL_NAME = "echo-data";
    private static final byte[] CHANNEL_NAME_BYTES = CHANNEL_NAME.getBytes(StandardCharsets.UTF_8);

    private RedisMessagingService(@NotNull String name) {
        super(name);
    }

    @Override
    public void close() {
        queueLock.writeLock().lock();
        try {
            closed = true;
            workPool.shutdown();
            try {
                if (!workPool.awaitTermination(4L, TimeUnit.SECONDS)) {
                    workPool.shutdownNow();
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            pool.close();
        } finally {
            queueLock.writeLock().unlock();
        }
    }

    @Override
    public boolean isClosed() { return closed || pool.isClosed(); }

    public static Builder builder(@NotNull String name, @NotNull UUID serverId, @NotNull MessagingHandler handler) { return new Builder(name, serverId, handler); }

    public static class Builder {
        private final RedisMessagingService service;
        private final JedisPoolConfig config = new JedisPoolConfig();

        private String address = "127.0.0.1";
        private int port = 6379;
        private int timeout = 5000;
        private String pass = "";

        public Builder(@NotNull String name, @NotNull UUID serverId, @NotNull MessagingHandler handler) {
            service = new RedisMessagingService(name);
            service.serverId = serverId;
            service.serverIdString = serverId.toString();
            ByteBuf buffer = alloc.buffer(16, 16);
            try {
                buffer.writeLong(serverId.getMostSignificantBits());
                buffer.writeLong(serverId.getLeastSignificantBits());
                if (buffer.isDirect()) {
                    service.serverIdBytes = new byte[16];
                    buffer.readBytes(service.serverIdBytes);
                } else {
                    service.serverIdBytes = buffer.array();
                }
            } finally {
                buffer.release();
            }

            service.handler = handler;
        }

        public Builder url(@NotNull String address, int port) {
            this.address = address;
            this.port = port;
            return this;
        }

        public Builder credentials(@NotNull String pass) {
            this.pass = pass;
            return this;
        }

        public Builder poolSize(int min, int max) {
            config.setMinIdle(min);
            config.setMaxTotal(max);
            return this;
        }

        public Builder life(long lifetime, int timeout) {
            config.setMinEvictableIdleTimeMillis(lifetime);
            config.setMaxWaitMillis(timeout);
            this.timeout = timeout;
            return this;
        }

        public @NotNull RedisMessagingService build() {
            service.pool = new JedisPool(config, address, port, timeout, pass == null || pass.isEmpty() ? null : pass);
            // Warm up pool
            // https://partners-intl.aliyun.com/help/doc-detail/98726.htm
            warmup(service.pool);
            // Indefinite subscription
            subscribe();
            return service;
        }

        private void subscribe() {
            service.workPool.execute(() -> {
                while (!service.isClosed()) {
                    try (Jedis redis = service.pool.getResource()) {
                        redis.subscribe(service.pubSub,
                            CHANNEL_NAME_BYTES
                        );
                    } catch (JedisException ex) {
                        if (!service.isClosed()) {
                            service.logger.warn("Redis pub/sub disconnected. Reconnecting..");
                        }
                    }
                }
            });
        }

        private void warmup(@NotNull JedisPool pool) {
            Jedis[] warmpupArr = new Jedis[config.getMinIdle()];

            for (int i = 0; i < config.getMinIdle(); i++) {
                Jedis jedis;
                jedis = pool.getResource();
                warmpupArr[i] = jedis;
                jedis.ping();
            }
            // Two loops because we need to ensure we don't pull a freshly-created resource from the pool
            for (int i = 0; i < config.getMinIdle(); i++) {
                Jedis jedis;
                jedis = warmpupArr[i];
                jedis.close();
            }
        }
    }

    private static class PubSub extends BinaryJedisPubSub {
        private final RedisMessagingService service;

        private PubSub(@NotNull RedisMessagingService service) { this.service = service; }

        @Override
        public void onMessage(byte @NotNull [] c, byte @NotNull [] m) {
            String channel = new String(c, StandardCharsets.UTF_8);
            if (ConfigUtil.getDebugOrFalse()) {
                service.logger.info("Got message from channel: " + channel);
            }

            try {
                switch (channel) {
                    case CHANNEL_NAME:
                        handleMessage(m);
                        break;
                    default:
                        service.logger.warn("Got data from channel that should not exist: " + channel);
                        break;
                }
            } catch (IOException ex) {
                service.logger.error("Could not handle message.", ex);
            }
        }

        private void handleMessage(byte @NotNull [] body) throws IOException {
            ByteBuf b = alloc.buffer(body.length, body.length);
            ByteBuf data = null;
            try {
                b.writeBytes(body);
                data = service.decompressData(b);

                UUID sender = new UUID(data.readLong(), data.readLong());
                if (service.serverId.equals(sender)) {
                    return;
                }
                UUID messageId = new UUID(data.readLong(), data.readLong());

                byte packetId = data.readByte();
                Class<Packet> packetClass = PacketUtil.getPacketCache().get(packetId);
                if (packetClass == null) {
                    service.logger.warn("Got packet ID that doesn't exist: " + packetId);
                    return;
                }

                try {
                    service.handler.handlePacket(messageId, service.getName(), packetClass.getConstructor(ByteBuf.class).newInstance(data));
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException | ExceptionInInitializerError | SecurityException ex) {
                    service.logger.error("Could not instantiate packet.", ex);
                }
            } finally {
                b.release();
                if (data != null) {
                    data.release();
                }
            }
        }
    }

    @Override
    public void sendPacket(@NotNull UUID messageId, @NotNull Packet packet) throws IOException {
        queueLock.readLock().lock();
        try (Jedis redis = pool.getResource()) {
            ByteBuf buffer = alloc.buffer(getInitialCapacity());
            try {
                buffer.writeBytes(serverIdBytes);
                buffer.writeLong(messageId.getMostSignificantBits());
                buffer.writeLong(messageId.getLeastSignificantBits());
                buffer.writeByte(packet.getPacketId());
                packet.write(buffer);
                addCapacity(buffer.writerIndex());

                redis.publish(CHANNEL_NAME_BYTES, compressData(buffer));
            } finally {
                buffer.release();
            }
        } catch (JedisException ex) {
            throw new IOException(ex);
        } finally {
            queueLock.readLock().unlock();
        }
    }
}
