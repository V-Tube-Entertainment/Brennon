package com.gizmo.brennon.core.player;

import com.gizmo.brennon.core.util.TimeUtil;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BrennonPlayer {
    private final UUID uniqueId;
    private final String username;
    private String displayName;
    private User luckPermsUser;
    private Group primaryGroup;
    private String currentServer;
    private boolean online;
    private Instant firstJoin;
    private Instant lastSeen;
    private final Map<String, Object> metadata;
    private final PlayerStats stats;
    private final PlayerSettings settings;

    public BrennonPlayer(UUID uniqueId, String username) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.displayName = username;
        this.metadata = new HashMap<>();
        this.stats = new PlayerStats();
        this.settings = new PlayerSettings();
        this.firstJoin = Instant.now();
        this.lastSeen = Instant.now();
    }

    // Getters and Setters
    public UUID getUniqueId() { return uniqueId; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public User getLuckPermsUser() { return luckPermsUser; }
    public void setLuckPermsUser(User user) { this.luckPermsUser = user; }
    public Group getPrimaryGroup() { return primaryGroup; }
    public void setPrimaryGroup(Group group) { this.primaryGroup = group; }
    public String getCurrentServer() { return currentServer; }
    public void setCurrentServer(String server) { this.currentServer = server; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
    public Instant getFirstJoin() { return firstJoin; }
    public void setFirstJoin(Instant firstJoin) { this.firstJoin = firstJoin; }
    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }
    public PlayerStats getStats() { return stats; }
    public PlayerSettings getSettings() { return settings; }

    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }

    public String getPlaytime() {
        return TimeUtil.formatDuration(firstJoin, Instant.now());
    }
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }
}
