package com.gizmo.brennon.core.messaging;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.gizmo.brennon.core.redis.RedisManager;
import com.gizmo.brennon.core.service.Service;
import com.gizmo.brennon.core.user.UserManager;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MessageBroker implements Service {
    private final Logger logger;
    private final RedisManager redisManager;
    private final UserManager userManager;
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private final Map<String, Consumer<String>> subscribers;

    @Inject
    public MessageBroker(Logger logger, RedisManager redisManager, UserManager userManager) {
        this.logger = logger;
        this.redisManager = redisManager;
        this.userManager = userManager;
        this.subscribers = new ConcurrentHashMap<>();
    }

    @Override
    public void enable() throws Exception {
        pubSubConnection = redisManager.createPubSubConnection();
        setupPubSub();
    }

    @Override
    public void disable() throws Exception {
        if (pubSubConnection != null) {
            pubSubConnection.close();
        }
    }

    private void setupPubSub() {
        pubSubConnection.addListener(new RedisPubSubListener<String, String>() {
            @Override
            public void message(String channel, String message) {
                Consumer<String> subscriber = subscribers.get(channel);
                if (subscriber != null) {
                    subscriber.accept(message);
                }
            }

            @Override
            public void message(String pattern, String channel, String message) {
                // Handle pattern-based messages if needed
            }

            @Override
            public void subscribed(String channel, long count) {
                logger.debug("Subscribed to channel: {}", channel);
            }

            @Override
            public void psubscribed(String pattern, long count) {
                logger.debug("Pattern subscribed: {}", pattern);
            }

            @Override
            public void unsubscribed(String channel, long count) {
                logger.debug("Unsubscribed from channel: {}", channel);
            }

            @Override
            public void punsubscribed(String pattern, long count) {
                logger.debug("Pattern unsubscribed: {}", pattern);
            }
        });
    }

    public void subscribe(String channel, Consumer<String> messageHandler) {
        subscribers.put(channel, messageHandler);
        RedisPubSubCommands<String, String> sync = pubSubConnection.sync();
        sync.subscribe(channel);
    }

    public void unsubscribe(String channel) {
        subscribers.remove(channel);
        RedisPubSubCommands<String, String> sync = pubSubConnection.sync();
        sync.unsubscribe(channel);
    }

    public void publish(String channel, JsonObject message) {
        redisManager.sync().publish(channel, message.toString());
    }

    public void publish(String channel, String message) {
        redisManager.sync().publish(channel, message);
    }
}
