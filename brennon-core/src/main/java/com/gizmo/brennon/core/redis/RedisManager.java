package com.gizmo.brennon.core.redis;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gizmo.brennon.core.config.Configuration;
import com.gizmo.brennon.core.config.ConfigurationManager;
import com.gizmo.brennon.core.config.ConfigurationType;
import com.gizmo.brennon.core.service.Service;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class RedisManager implements Service {
    private final Logger logger;
    private final ConfigurationManager configManager;
    private final Map<String, MessageHandler> channelHandlers;
    private final ExecutorService executorService;

    private RedisClient redisClient;
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private StatefulRedisPubSubConnection<String, String> subscriberConnection;
    private RedisPubSubAsyncCommands<String, String> pubSubAsync;

    @Inject
    public RedisManager(Logger logger, ConfigurationManager configManager) {
        this.logger = logger;
        this.configManager = configManager;
        this.channelHandlers = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void enable() throws Exception {
        Configuration config = configManager.loadConfig("redis", ConfigurationType.YAML, getDefaultConfig());

        RedisURI redisURI = RedisURI.builder()
                .withHost(config.get("host", "localhost"))
                .withPort(config.get("port", 6379))
                .withPassword(config.get("password", "").toCharArray())
                .withDatabase(config.get("database", 0))
                .build();

        try {
            this.redisClient = RedisClient.create(redisURI);
            this.pubSubConnection = redisClient.connectPubSub();
            this.subscriberConnection = redisClient.connectPubSub();
            this.pubSubAsync = pubSubConnection.async();

            // Set up the message listener
            subscriberConnection.addListener(new RedisPubSubAdapter<>() {
                @Override
                public void message(String channel, String message) {
                    handleMessage(channel, message);
                }
            });

            logger.info("Redis connection established successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Redis connection", e);
            throw e;
        }
    }

    @Override
    public void disable() throws Exception {
        channelHandlers.clear();
        executorService.shutdown();

        if (pubSubConnection != null) {
            pubSubConnection.close();
        }
        if (subscriberConnection != null) {
            subscriberConnection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }

        logger.info("Redis connection closed");
    }

    /**
     * Subscribe to a Redis channel
     *
     * @param channel The channel to subscribe to
     * @param handler The handler for messages on this channel
     */
    public void subscribe(String channel, MessageHandler handler) {
        channelHandlers.put(channel, handler);
        subscriberConnection.sync().subscribe(channel);
        logger.debug("Subscribed to Redis channel: {}", channel);
    }

    /**
     * Unsubscribe from a Redis channel
     *
     * @param channel The channel to unsubscribe from
     */
    public void unsubscribe(String channel) {
        channelHandlers.remove(channel);
        subscriberConnection.sync().unsubscribe(channel);
        logger.debug("Unsubscribed from Redis channel: {}", channel);
    }

    /**
     * Publish a message to a Redis channel
     *
     * @param channel The channel to publish to
     * @param message The message to publish
     */
    public void publish(String channel, String message) {
        pubSubAsync.publish(channel, message)
                .exceptionally(throwable -> {
                    logger.error("Failed to publish message to channel: " + channel, throwable);
                    return null;
                });
    }

    private void handleMessage(String channel, String message) {
        MessageHandler handler = channelHandlers.get(channel);
        if (handler != null) {
            executorService.submit(() -> {
                try {
                    handler.handle(message);
                } catch (Exception e) {
                    logger.error("Error handling message on channel: " + channel, e);
                }
            });
        }
    }

    private Map<String, Object> getDefaultConfig() {
        return Map.of(
                "host", "localhost",
                "port", 6379,
                "password", "",
                "database", 0
        );
    }

    /**
     * Check if Redis connection is healthy
     *
     * @return true if connected
     */
    public boolean isHealthy() {
        return redisClient != null && pubSubConnection != null && !pubSubConnection.isOpen();
    }
}
