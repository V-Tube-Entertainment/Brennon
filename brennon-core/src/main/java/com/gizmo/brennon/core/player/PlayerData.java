package com.gizmo.brennon.core.player;

import com.google.gson.Gson;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

public class PlayerData {
    private static final Gson gson = new Gson();

    private final UUID uuid;
    private String username;
    private PlayerRank rank;
    private Instant lastSeen;
    private long playtime;
    private PlayerSettings settings;
    private PlayerStats statistics;

    public PlayerData(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.rank = PlayerRank.DEFAULT;
        this.lastSeen = Instant.now();
        this.playtime = 0;
        this.settings = new PlayerSettings();
        this.statistics = new PlayerStats();
    }

    public static PlayerData fromResultSet(ResultSet rs) throws SQLException {
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        String username = rs.getString("username");
        PlayerData data = new PlayerData(uuid, username);

        data.rank = PlayerRank.fromString(rs.getString("rank"));
        data.lastSeen = rs.getTimestamp("last_seen").toInstant();
        data.playtime = rs.getLong("playtime");

        String settingsJson = rs.getString("settings");
        if (settingsJson != null) {
            data.settings = gson.fromJson(settingsJson, PlayerSettings.class);
        }

        String statsJson = rs.getString("statistics");
        if (statsJson != null) {
            data.statistics = gson.fromJson(statsJson, PlayerStats.class);
        }

        return data;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public PlayerRank getRank() {
        return rank;
    }

    public void setRank(PlayerRank rank) {
        this.rank = rank;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    public long getPlaytime() {
        return playtime;
    }

    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }

    public String getSettingsJson() {
        return gson.toJson(settings);
    }

    public String getStatisticsJson() {
        return gson.toJson(statistics);
    }

    public PlayerSettings getSettings() {
        return settings;
    }

    public PlayerStats getStatistics() {
        return statistics;
    }
}