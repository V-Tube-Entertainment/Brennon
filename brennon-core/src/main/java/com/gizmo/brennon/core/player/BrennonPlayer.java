package com.gizmo.brennon.core.player;

import com.gizmo.brennon.core.player.rank.NetworkRank;
import net.luckperms.api.model.user.User;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BrennonPlayer {
    private final UUID uniqueId;
    private String username;
    private String displayName;
    private NetworkRank rank;
    private User luckPermsUser;
    private final PlayerStats stats;
    private final PlayerSettings settings;
    private String currentServer;
    private boolean online;
    private Instant firstJoin;
    private Instant lastSeen;
    private final Map<String, Object> metadata;

    public BrennonPlayer(UUID uniqueId, String username) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.displayName = username;
        this.rank = NetworkRank.DEFAULT;
        this.stats = new PlayerStats();
        this.settings = new PlayerSettings();
        this.firstJoin = Instant.now();
        this.lastSeen = Instant.now();
        this.metadata = new HashMap<>();
    }

    // Core getters and setters
    public UUID getUniqueId() { return uniqueId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    // Rank and permissions
    public NetworkRank getRank() { return rank; }
    public void setRank(NetworkRank rank) { this.rank = rank; }
    public User getLuckPermsUser() { return luckPermsUser; }
    public void setLuckPermsUser(User user) {
        this.luckPermsUser = user;
        this.rank = NetworkRank.fromGroup(user.getPrimaryGroup());
    }

    // Server and status
    public String getCurrentServer() { return currentServer; }
    public void setCurrentServer(String server) { this.currentServer = server; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    // Timestamps
    public Instant getFirstJoin() { return firstJoin; }
    public void setFirstJoin(Instant firstJoin) { this.firstJoin = firstJoin; }
    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }

    // Stats and settings
    public PlayerStats getStats() { return stats; }
    public PlayerSettings getSettings() { return settings; }

    // Metadata
    public void setMetadata(String key, Object value) { metadata.put(key, value); }
    public Object getMetadata(String key) { return metadata.get(key); }
    public boolean hasMetadata(String key) { return metadata.containsKey(key); }
    public Map<String, Object> getAllMetadata() { return new HashMap<>(metadata); }

    // Utility methods
    public Duration getPlaytime() {
        return Duration.between(firstJoin, isOnline() ? Instant.now() : lastSeen);
    }

    public String getFormattedName() {
        return rank.getColor() + username;
    }

    public String getFormattedDisplayName() {
        return rank.getPrefix() + " " + displayName;
    }

    public boolean isStaff() {
        return rank.isStaff();
    }

    public boolean isAdmin() {
        return rank.isAdmin();
    }
}
