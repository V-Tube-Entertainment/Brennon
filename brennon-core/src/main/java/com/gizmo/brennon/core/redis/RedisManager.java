package com.gizmo.brennon.core.redis;

import com.gizmo.brennon.core.service.Service;
import com.google.inject.Inject;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.slf4j.Logger;

public class RedisManager implements Service {
    private final Logger logger;
    private final RedisConfig config;
    private RedisClient client;
    private StatefulRedisConnection<String, String> connection;

    @Inject
    public RedisManager(Logger logger, RedisConfig config) {
        this.logger = logger;
        this.config = config;
    }

    @Override
    public void enable() throws Exception {
        RedisURI redisUri = RedisURI.builder()
                .withHost(config.host())
                .withPort(config.port())
                .withPassword(config.password().isEmpty() ? null : config.password())
                .withDatabase(config.database())
                .build();

        client = RedisClient.create(redisUri);
        connection = client.connect();
        logger.info("Connected to Redis at {}:{}", config.host(), config.port());
    }

    @Override
    public void disable() throws Exception {
        if (connection != null) {
            connection.close();
        }
        if (client != null) {
            client.shutdown();
        }
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
