package com.gizmo.brennon.core.player;

import com.gizmo.brennon.core.player.rank.NetworkRank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.luckperms.api.model.user.User;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<String, Long> cooldowns;

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
        this.cooldowns = new ConcurrentHashMap<>();
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
        if (user != null) {
            String primaryGroupName = user.getPrimaryGroup();
            this.rank = NetworkRank.fromGroupName(primaryGroupName);
        }
    }

    // Server and status
    public String getCurrentServer() { return currentServer; }
    public void setCurrentServer(String server) {
        this.currentServer = server;
        this.lastSeen = Instant.now();
    }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) {
        this.online = online;
        if (online) {
            this.lastSeen = Instant.now();
        }
    }

    // Timestamps
    public Instant getFirstJoin() { return firstJoin; }
    public void setFirstJoin(Instant firstJoin) { this.firstJoin = firstJoin; }

    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }

    public Duration getPlaytime() {
        return Duration.between(firstJoin, isOnline() ? Instant.now() : lastSeen);
    }

    // Stats and settings
    public PlayerStats getStats() { return stats; }
    public PlayerSettings getSettings() { return settings; }

    // Metadata
    public void setMetadata(String key, Object value) { metadata.put(key, value); }
    public Object getMetadata(String key) { return metadata.get(key); }
    public boolean hasMetadata(String key) { return metadata.containsKey(key); }
    public Map<String, Object> getAllMetadata() { return new HashMap<>(metadata); }

    // Cooldown system
    public void setCooldown(String key, long durationMillis) {
        cooldowns.put(key, System.currentTimeMillis() + durationMillis);
    }

    public boolean hasCooldown(String key) {
        Long cooldown = cooldowns.get(key);
        if (cooldown == null) return false;
        if (System.currentTimeMillis() >= cooldown) {
            cooldowns.remove(key);
            return false;
        }
        return true;
    }

    public long getRemainingCooldown(String key) {
        Long cooldown = cooldowns.get(key);
        if (cooldown == null) return 0;
        long remaining = cooldown - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    // Adventure API text components
    public Component getColoredName() {
        return rank.colorize(username);
    }

    public Component getFormattedName() {
        return Component.text()
                .append(rank.getPrefix())
                .append(Component.text(" "))
                .append(Component.text(username))
                .build();
    }

    public Component getGradientName() {
        return rank.getGradientText(username);
    }

    public Component getStaffName() {
        if (!rank.isStaff()) return getFormattedName();
        return Component.text()
                .append(rank.getStyledPrefix())
                .append(Component.text(" "))
                .append(Component.text(username).color(rank.getTextColor())
                        .decorate(TextDecoration.BOLD))
                .build();
    }

    public Component getChatFormat(String message) {
        return rank.getChatFormat(username, message);
    }

    public Component getTabListName() {
        return rank.getTabListName(username);
    }

    // Permission checks
    public boolean hasPermission(String permission) {
        return luckPermsUser != null &&
                luckPermsUser.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    public boolean outranks(BrennonPlayer other) {
        return this.rank.outranks(other.getRank());
    }

    // Staff checks
    public boolean isStaff() {
        return rank.isStaff();
    }

    public boolean isAdmin() {
        return rank.isAdmin();
    }

    public boolean isManagement() {
        return rank.isManagement();
    }

    public boolean isDeveloper() {
        return rank.isDeveloper();
    }

    public boolean isBuilder() {
        return rank.isBuilder();
    }

    public boolean isModerator() {
        return rank.isModerator();
    }

    public boolean isVIP() {
        return rank.isVIP();
    }

    // Server management checks
    public boolean canManageStaff() {
        return rank.canManageStaff();
    }

    public boolean canManageServer() {
        return rank.canManageServer();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BrennonPlayer other)) return false;
        return uniqueId.equals(other.uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }

    @Override
    public String toString() {
        return "BrennonPlayer{" +
                "uuid=" + uniqueId +
                ", username='" + username + '\'' +
                ", rank=" + rank +
                ", online=" + online +
                '}';
    }
}