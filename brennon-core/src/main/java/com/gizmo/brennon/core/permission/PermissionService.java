package com.gizmo.brennon.core.permission;

import com.google.inject.Inject;
import com.gizmo.brennon.core.service.Service;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PermissionService implements Service {
    private final Logger logger;
    private LuckPerms luckPerms;

    @Inject
    public PermissionService(Logger logger) {
        this.logger = logger;
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
        this.luckPerms = null;
    }

    /**
     * Checks if a player has a permission
     *
     * @param playerId The UUID of the player
     * @param permission The permission to check
     * @return true if the player has the permission
     */
    public boolean hasPermission(UUID playerId, String permission) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) return false;

        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    /**
     * Gets a player's primary group name
     *
     * @param playerId The UUID of the player
     * @return The name of the player's primary group
     */
    public String getPrimaryGroup(UUID playerId) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) return "default";

        return user.getPrimaryGroup();
    }

    /**
     * Gets the display name of a player's primary group
     *
     * @param playerId The UUID of the player
     * @return The display name of the player's primary group
     */
    public String getGroupDisplayName(UUID playerId) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) return "Default";

        String groupName = user.getPrimaryGroup();
        var group = luckPerms.getGroupManager().getGroup(groupName);
        if (group == null) return "Default";

        return group.getFriendlyName();
    }

    /**
     * Gets the prefix for a player
     *
     * @param playerId The UUID of the player
     * @return The player's prefix
     */
    public String getPrefix(UUID playerId) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) return "";

        String prefix = user.getCachedData().getMetaData().getPrefix();
        return prefix != null ? prefix : "";
    }

    /**
     * Gets the suffix for a player
     *
     * @param playerId The UUID of the player
     * @return The player's suffix
     */
    public String getSuffix(UUID playerId) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) return "";

        String suffix = user.getCachedData().getMetaData().getSuffix();
        return suffix != null ? suffix : "";
    }

    /**
     * Gets a meta value for a player
     *
     * @param playerId The UUID of the player
     * @param key The meta key
     * @return The meta value
     */
    public String getMeta(UUID playerId, String key) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) return "";

        String value = user.getCachedData().getMetaData().getMetaValue(key);
        return value != null ? value : "";
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
     * Gets the weight of a player's primary group
     *
     * @param playerId The UUID of the player
     * @return The group weight, or 0 if not found
     */
    public int getGroupWeight(UUID playerId) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) return 0;

        var group = luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
        return group != null ? group.getWeight().orElse(0) : 0;
    }

    /**
     * Checks if a player is in a specific group
     *
     * @param playerId The UUID of the player
     * @param groupName The name of the group
     * @return true if the player is in the group
     */
    public boolean isInGroup(UUID playerId, String groupName) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) return false;

        return user.getInheritedGroups(QueryOptions.defaultContextualOptions())
                .stream()
                .anyMatch(group -> group.getName().equalsIgnoreCase(groupName));
    }
}
