package com.gizmo.brennon.core.permission;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.gizmo.brennon.core.service.Service;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PermissionService implements Service {
    private final Logger logger;
    private LuckPerms luckPerms;

    private final Cache<String, Object> permissionCache;

    @Inject
    public PermissionService(Logger logger) {
        this.logger = logger;
        this.permissionCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
    }

    @Override
    public void enable() throws Exception {
        try {
            this.luckPerms = LuckPermsProvider.get();
            logger.info("Successfully connected to LuckPerms");
        } catch (IllegalStateException e) {
            logger.error("Failed to connect to LuckPerms. Make sure LuckPerms is installed!", e);
            throw e;
        }
    }

    @Override
    public void disable() throws Exception {
        clearAllCache();
        this.luckPerms = null;
    }

    /**
     * Clears the permission cache for a specific player
     * @param playerId The UUID of the player whose cache should be cleared
     */
    public void clearCache(UUID playerId) {
        if (playerId == null) return;
        String playerKey = playerId.toString();
        permissionCache.asMap().keySet().removeIf(key -> key.startsWith(playerKey));
        logger.debug("Cleared permission cache for player: {}", playerId);
    }

    /**
     * Clears the entire permission cache
     */
    public void clearAllCache() {
        permissionCache.invalidateAll();
        logger.debug("Cleared all permission caches");
    }

    /**
     * Checks if a player has a permission
     *
     * @param playerId The UUID of the player
     * @param permission The permission to check
     * @return true if the player has the permission
     */
    public boolean hasPermission(UUID playerId, String permission) {
        String cacheKey = playerId + ":perm:" + permission;
        return (Boolean) permissionCache.get(cacheKey, k -> {
            User user = luckPerms.getUserManager().getUser(playerId);
            if (user == null) return false;
            return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        });
    }

    /**
     * Gets a player's primary group name
     *
     * @param playerId The UUID of the player
     * @return The name of the player's primary group
     */
    public String getPrimaryGroup(UUID playerId) {
        String cacheKey = playerId + ":primarygroup";
        return (String) permissionCache.get(cacheKey, k -> {
            User user = luckPerms.getUserManager().getUser(playerId);
            return user != null ? user.getPrimaryGroup() : "default";
        });
    }

    /**
     * Gets the display name of a player's primary group
     *
     * @param playerId The UUID of the player
     * @return The display name of the player's primary group
     */
    public String getGroupDisplayName(UUID playerId) {
        String cacheKey = playerId + ":groupdisplay";
        return (String) permissionCache.get(cacheKey, k -> {
            User user = luckPerms.getUserManager().getUser(playerId);
            if (user == null) return "Default";

            String groupName = user.getPrimaryGroup();
            var group = luckPerms.getGroupManager().getGroup(groupName);
            return group != null ? group.getFriendlyName() : "Default";
        });
    }

    /**
     * Gets the prefix for a player
     *
     * @param playerId The UUID of the player
     * @return The player's prefix
     */
    public String getPrefix(UUID playerId) {
        String cacheKey = playerId + ":prefix";
        return (String) permissionCache.get(cacheKey, k -> {
            User user = luckPerms.getUserManager().getUser(playerId);
            if (user == null) return "";
            String prefix = user.getCachedData().getMetaData().getPrefix();
            return prefix != null ? prefix : "";
        });
    }

    /**
     * Gets the suffix for a player
     *
     * @param playerId The UUID of the player
     * @return The player's suffix
     */
    public String getSuffix(UUID playerId) {
        String cacheKey = playerId + ":suffix";
        return (String) permissionCache.get(cacheKey, k -> {
            User user = luckPerms.getUserManager().getUser(playerId);
            if (user == null) return "";
            String suffix = user.getCachedData().getMetaData().getSuffix();
            return suffix != null ? suffix : "";
        });
    }

    /**
     * Gets a meta value for a player
     *
     * @param playerId The UUID of the player
     * @param key The meta key
     * @return The meta value
     */
    public String getMeta(UUID playerId, String key) {
        String cacheKey = playerId + ":meta:" + key;
        return (String) permissionCache.get(cacheKey, k -> {
            User user = luckPerms.getUserManager().getUser(playerId);
            if (user == null) return "";
            String value = user.getCachedData().getMetaData().getMetaValue(key);
            return value != null ? value : "";
        });
    }

    /**
     * Gets the weight of a player's primary group
     *
     * @param playerId The UUID of the player
     * @return The group weight, or 0 if not found
     */
    public int getGroupWeight(UUID playerId) {
        String cacheKey = playerId + ":weight";
        return (Integer) permissionCache.get(cacheKey, k -> {
            User user = luckPerms.getUserManager().getUser(playerId);
            if (user == null) return 0;
            var group = luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
            return group != null ? group.getWeight().orElse(0) : 0;
        });
    }

    /**
     * Checks if a player is in a specific group
     *
     * @param playerId The UUID of the player
     * @param groupName The name of the group
     * @return true if the player is in the group
     */
    public boolean isInGroup(UUID playerId, String groupName) {
        String cacheKey = playerId + ":ingroup:" + groupName;
        return (Boolean) permissionCache.get(cacheKey, k -> {
            User user = luckPerms.getUserManager().getUser(playerId);
            if (user == null) return false;
            return user.getInheritedGroups(QueryOptions.defaultContextualOptions())
                    .stream()
                    .anyMatch(group -> group.getName().equalsIgnoreCase(groupName));
        });
    }

    /**
     * Asynchronously loads a user's LuckPerms data
     *
     * @param playerId The UUID of the player
     * @return A CompletableFuture that completes when the data is loaded
     */
    public CompletableFuture<User> loadUser(UUID playerId) {
        return luckPerms.getUserManager().loadUser(playerId);
    }

    /**
     * Gets the LuckPerms API instance
     *
     * @return The LuckPerms API instance
     */
    public LuckPerms getLuckPerms() {
        return luckPerms;
    }
}