package com.gizmo.brennon.core.messaging;

import com.google.inject.Inject;
import com.gizmo.brennon.core.redis.RedisManager;
import com.gizmo.brennon.core.service.Service;
import com.gizmo.brennon.core.user.UserManager;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.slf4j.Logger;

import java.util.UUID;

public class MessageBroker implements Service {
    private final Logger logger;
    private final RedisManager redisManager;
    private final UserManager userManager;
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;

    @Inject
    public MessageBroker(Logger logger, RedisManager redisManager, UserManager userManager) {
        this.logger = logger;
        this.redisManager = redisManager;
        this.userManager = userManager;
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
            // ... rest of the implementation
        });
    }

    private void handleUserAction(UUID uuid, String server) {
        if (server != null) {
            userManager.handleUserJoin(uuid, "Unknown", "0.0.0.0", server);
        } else {
            userManager.handleUserQuit(uuid);
        }
    }
}
