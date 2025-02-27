package com.gizmo.brennon.core.messaging;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.gizmo.brennon.core.redis.RedisManager;
import com.gizmo.brennon.core.service.Service;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MessageBroker implements Service {
    private final Logger logger;
    private final RedisManager redisManager;
    private final String serverId;
    private final Gson gson;
    private final Map<String, Consumer<Message>> subscribers;
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;

    @Inject
    public MessageBroker(Logger logger, RedisManager redisManager, String serverId) {
        this.logger = logger;
        this.redisManager = redisManager;
        this.serverId = serverId;
        this.gson = new Gson();
        this.subscribers = new ConcurrentHashMap<>();
    }

    @Override
    public void enable() {
        pubSubConnection = redisManager.createPubSubConnection();
        pubSubConnection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, String message) {
                try {
                    Message msg = gson.fromJson(message, Message.class);
                    // Don't process messages from self
                    if (!serverId.equals(msg.source())) {
                        Consumer<Message> subscriber = subscribers.get(channel);
                        if (subscriber != null) {
                            subscriber.accept(msg);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing message on channel: " + channel, e);
                }
            }
        });
    }

    @Override
    public void disable() {
        if (pubSubConnection != null) {
            pubSubConnection.close();
        }
        subscribers.clear();
    }

    public void publish(String channel, JsonElement data) {
        Message message = Message.create(channel, serverId, data);
        String json = gson.toJson(message);
        redisManager.sync().publish(channel, json);
    }

    public void subscribe(String channel, Consumer<Message> subscriber) {
        subscribers.put(channel, subscriber);
        pubSubConnection.sync().subscribe(channel);
    }

    public void unsubscribe(String channel) {
        subscribers.remove(channel);
        pubSubConnection.sync().unsubscribe(channel);
    }

    private void handleUserAction(UUID uuid, String server) {
        // Update to handle the action with only uuid and server
        if (server != null) {
            // This is a join or switch action
            userManager.handleUserJoin(uuid, "Unknown", "0.0.0.0", server);
        } else {
            // This is a quit action
            userManager.handleUserQuit(uuid);
        }
    }
}
