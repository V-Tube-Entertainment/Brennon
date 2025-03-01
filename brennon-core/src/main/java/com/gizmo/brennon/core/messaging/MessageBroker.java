package com.gizmo.brennon.core.messaging;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.gizmo.brennon.core.redis.RedisManager;
import com.gizmo.brennon.core.redis.MessageHandler;
import com.gizmo.brennon.core.service.Service;
import com.gizmo.brennon.core.user.UserManager;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Handles messaging between servers using Redis pub/sub
 *
 * @author Gizmo0320
 * @since 2025-03-01 04:58:17
 */
public class MessageBroker implements Service {
    private final Logger logger;
    private final RedisManager redisManager;
    private final UserManager userManager;
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
        // Nothing to do here as RedisManager handles the connection
    }

    @Override
    public void disable() throws Exception {
        subscribers.clear();
    }

    /**
     * Reconnects to Redis and reestablishes all subscriptions
     *
     * @throws RuntimeException if reconnection fails
     */
    public void reconnect() {
        logger.info("Reconnecting message broker...");

        try {
            // Store current subscriptions
            Map<String, Consumer<String>> currentSubscribers = new ConcurrentHashMap<>(subscribers);

            // Clear current subscriptions
            subscribers.clear();

            // Reconnect Redis
            redisManager.reconnect();

            // Restore subscriptions
            currentSubscribers.forEach(this::subscribe);

            logger.info("Message broker reconnected successfully");
        } catch (Exception e) {
            logger.error("Failed to reconnect message broker", e);
            throw new RuntimeException("Failed to reconnect message broker", e);
        }
    }

    public void subscribe(String channel, Consumer<String> messageHandler) {
        subscribers.put(channel, messageHandler);
        redisManager.subscribe(channel, message -> {
            Consumer<String> handler = subscribers.get(channel);
            if (handler != null) {
                try {
                    handler.accept(message);
                } catch (Exception e) {
                    logger.error("Error processing message on channel: " + channel, e);
                }
            }
        });
    }

    public void unsubscribe(String channel) {
        subscribers.remove(channel);
        redisManager.unsubscribe(channel);
    }

    public void publish(String channel, JsonObject message) {
        redisManager.publish(channel, message.toString());
    }

    public void publish(String channel, String message) {
        redisManager.publish(channel, message);
    }
}