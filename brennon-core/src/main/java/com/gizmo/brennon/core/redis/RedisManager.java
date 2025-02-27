package com.gizmo.brennon.core.redis;

import com.google.inject.Inject;
import com.gizmo.brennon.core.config.CoreConfig;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.slf4j.Logger;

public class RedisManager {
    private final Logger logger;
    private final CoreConfig config;
    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;

    @Inject
    public RedisManager(Logger logger, CoreConfig config) {
        this.logger = logger;
        this.config = config;
    }

    public void initialize() {
        logger.info("Initializing Redis connection...");
        try {
            RedisURI redisUri = RedisURI.create(config.getRedisConfig().getRedisUrl());
            this.redisClient = RedisClient.create(redisUri);
            this.connection = redisClient.connect();
            logger.info("Redis connection established successfully!");
        } catch (Exception e) {
            logger.error("Failed to initialize Redis connection!", e);
            throw new RuntimeException("Failed to initialize Redis connection", e);
        }
    }

    public void shutdown() {
        if (connection != null) {
            connection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
        logger.info("Redis connection closed successfully!");
    }

    public RedisCommands<String, String> sync() {
        return connection.sync();
    }

    public StatefulRedisConnection<String, String> getConnection() {
        return connection;
    }
    public StatefulRedisPubSubConnection<String, String> createPubSubConnection() {
        return client.connectPubSub();
    }

}
