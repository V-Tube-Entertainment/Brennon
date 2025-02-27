package com.gizmo.brennon.core.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {
    private static final Gson GSON = new GsonBuilder().create();

    private final UUID uuid;
    private String username;
    private Instant firstJoin;
    private Instant lastSeen;
    private long playtime;
    private String lastServer;
    private final Map<String, Object> settings;
    private final Map<String, Long> statistics;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.firstJoin = Instant.now();
        this.lastSeen = Instant.now();
        this.settings = new ConcurrentHashMap<>();
        this.statistics = new ConcurrentHashMap<>();
    }

    public static PlayerData fromResultSet(ResultSet rs) throws SQLException {
        PlayerData data = new PlayerData(UUID.fromString(rs.getString("uuid")));
        data.username = rs.getString("username");
        data.firstJoin = rs.getTimestamp("first_join").toInstant();
        data.lastSeen = rs.getTimestamp("last_seen").toInstant();
        data.playtime = rs.getLong("playtime");
        data.lastServer = rs.getString("last_server");

        // Parse JSON data
        String settingsJson = rs.getString("settings");
        String statsJson = rs.getString("statistics");

        if (settingsJson != null) {
            data.settings.putAll(GSON.fromJson(settingsJson,
                    new TypeToken<Map<String, Object>>(){}.getType()));
        }

        if (statsJson != null) {
            data.statistics.putAll(GSON.fromJson(statsJson,
                    new TypeToken<Map<String, Long>>(){}.getType()));
        }

        return data;
    }

    // Getters and setters
    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getFirstJoin() {
        return firstJoin;
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

    public void updatePlaytime(long additionalTime) {
        this.playtime += additionalTime;
    }

    public String getLastServer() {
        return lastServer;
    }

    public void setLastServer(String lastServer) {
        this.lastServer = lastServer;
    }

    public Object getSetting(String key) {
        return settings.get(key);
    }

    public void setSetting(String key, Object value) {
        settings.put(key, value);
    }

    public long getStatistic(String key) {
        return statistics.getOrDefault(key, 0L);
    }

    public void incrementStatistic(String key) {
        statistics.compute(key, (k, v) -> (v == null) ? 1 : v + 1);
    }

    public void setStatistic(String key, long value) {
        statistics.put(key, value);
    }

    public String getSettingsJson() {
        return GSON.toJson(settings);
    }

    public String getStatisticsJson() {
        return GSON.toJson(statistics);
    }
}
