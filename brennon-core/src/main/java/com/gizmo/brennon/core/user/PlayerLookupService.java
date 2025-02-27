package com.gizmo.brennon.core.user;

import com.google.gson.Gson;
import com.google.inject.Inject;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLookupService {
    private final Logger logger;
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final Map<String, CachedUUID> uuidCache;
    private final Map<UUID, CachedUsername> usernameCache;
    private final Gson gson;  // Added Gson field

    @Inject
    public PlayerLookupService(Logger logger, RedisClient redisClient) {
        this.logger = logger;
        this.redisClient = redisClient;
        this.connection = redisClient.connect();
        this.uuidCache = new ConcurrentHashMap<>();
        this.usernameCache = new ConcurrentHashMap<>();
        this.gson = new Gson();  // Initialize Gson
    }

    /**
     * Gets a player's UUID from their username.
     *
     * @param username The username to look up
     * @return The UUID if found, otherwise null
     */
    public UUID getUUID(String username) {
        // Check cache first
        CachedUUID cached = uuidCache.get(username.toLowerCase());
        if (cached != null && !cached.isExpired()) {
            return cached.uuid();
        }

        // Try Redis for online players
        try {
            RedisCommands<String, String> sync = connection.sync();
            String uuidStr = sync.hget("player:online", username.toLowerCase());
            if (uuidStr != null) {
                UUID uuid = UUID.fromString(uuidStr);
                uuidCache.put(username.toLowerCase(), new CachedUUID(uuid, System.currentTimeMillis()));
                return uuid;
            }
        } catch (Exception e) {
            logger.error("Failed to lookup UUID from Redis for {}", username, e);
        }

        return null;
    }

    /**
     * Gets a player's username from their UUID.
     *
     * @param uuid The UUID to look up
     * @return The username if found, otherwise null
     */
    public String getUsername(UUID uuid) {
        // Check cache first
        CachedUsername cached = usernameCache.get(uuid);
        if (cached != null && !cached.isExpired()) {
            return cached.username();
        }

        // Try Redis for online players
        try {
            RedisCommands<String, String> sync = connection.sync();
            String username = sync.hget("player:online:reverse", uuid.toString());
            if (username != null) {
                usernameCache.put(uuid, new CachedUsername(username, System.currentTimeMillis()));
                return username;
            }
        } catch (Exception e) {
            logger.error("Failed to lookup username from Redis for {}", uuid, e);
        }

        return null;
    }

    public UserInfo getUserInfo(UUID uuid) {
        String username = getUsername(uuid);
        if (username == null) {
            return null;
        }

        try {
            RedisCommands<String, String> sync = connection.sync();
            Map<String, String> info = sync.hgetall("player:info:" + uuid);
            if (!info.isEmpty()) {
                return new UserInfo(
                        uuid,
                        username,
                        info.getOrDefault("displayName", username),
                        info.getOrDefault("currentServer", ""),
                        UserStatus.valueOf(info.getOrDefault("status", UserStatus.OFFLINE.name())),
                        info.getOrDefault("ipAddress", ""),
                        info.getOrDefault("locale", "en_US"),
                        parseJsonMap(info.getOrDefault("metadata", "{}")),
                        parseJsonMap(info.getOrDefault("properties", "{}")),
                        Instant.parse(info.getOrDefault("firstJoin", Instant.now().toString())),
                        Instant.parse(info.getOrDefault("lastSeen", Instant.now().toString()))
                );
            }
        } catch (Exception e) {
            logger.error("Failed to lookup user info from Redis for {}", uuid, e);
        }

        return UserInfo.createBasic(uuid, username);
    }

    public void updateCache(UUID uuid, String username, UserInfo userInfo) {
        uuidCache.put(username.toLowerCase(), new CachedUUID(uuid, System.currentTimeMillis()));
        usernameCache.put(uuid, new CachedUsername(username, System.currentTimeMillis()));

        try {
            RedisCommands<String, String> sync = connection.sync();
            // Update online player mappings
            sync.hset("player:online", username.toLowerCase(), uuid.toString());
            sync.hset("player:online:reverse", uuid.toString(), username);

            // Update additional info if provided
            if (userInfo != null) {
                Map<String, String> info = new HashMap<>();
                info.put("displayName", userInfo.displayName());
                info.put("status", userInfo.status().name());
                info.put("locale", userInfo.locale());
                info.put("firstJoin", userInfo.firstJoin().toString());
                info.put("lastSeen", userInfo.lastSeen().toString());
                info.put("ipAddress", userInfo.ipAddress());
                info.put("currentServer", userInfo.currentServer());
                info.put("metadata", gson.toJson(userInfo.metadata()));
                info.put("properties", gson.toJson(userInfo.properties()));
                sync.hmset("player:info:" + uuid, info);
            }
        } catch (Exception e) {
            logger.error("Failed to update cache in Redis for {} ({})", username, uuid, e);
        }
    }

    /**
     * Removes a player from the cache.
     *
     * @param uuid The player's UUID
     * @param username The player's username
     */
    public void removeFromCache(UUID uuid, String username) {
        uuidCache.remove(username.toLowerCase());
        usernameCache.remove(uuid);

        try {
            RedisCommands<String, String> sync = connection.sync();
            sync.hdel("player:online", username.toLowerCase());
            sync.hdel("player:online:reverse", uuid.toString());
            sync.del("player:info:" + uuid);
        } catch (Exception e) {
            logger.error("Failed to remove from cache in Redis for {} ({})", username, uuid, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseJsonMap(String json) {
        try {
            return gson.fromJson(json, Map.class);
        } catch (Exception e) {
            logger.error("Failed to parse JSON map: {}", json, e);
            return new HashMap<>();
        }
    }

    public void shutdown() {
        if (connection != null) {
            connection.close();
        }
    }

    private record CachedUUID(UUID uuid, long timestamp) {
        private static final long CACHE_DURATION = 3600000; // 1 hour in milliseconds

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }

    private record CachedUsername(String username, long timestamp) {
        private static final long CACHE_DURATION = 3600000; // 1 hour in milliseconds

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }
}