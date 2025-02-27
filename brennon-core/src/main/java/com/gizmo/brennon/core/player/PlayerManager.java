package com.gizmo.brennon.core.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gizmo.brennon.core.cache.CacheManager;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class PlayerManager implements Service {
    private final Logger logger;
    private final DatabaseManager databaseManager;
    private final CacheManager cacheManager;
    private final Map<UUID, PlayerData> playerCache;

    @Inject
    public PlayerManager(Logger logger, DatabaseManager databaseManager, CacheManager cacheManager) {
        this.logger = logger;
        this.databaseManager = databaseManager;
        this.cacheManager = cacheManager;
        this.playerCache = new ConcurrentHashMap<>();
    }

    @Override
    public void enable() throws Exception {
        createTables();
        logger.info("PlayerManager enabled successfully");
    }

    @Override
    public void disable() throws Exception {
        saveAllData();
        playerCache.clear();
        logger.info("PlayerManager disabled successfully");
    }

    private void createTables() {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS player_data (" +
                             "uuid VARCHAR(36) PRIMARY KEY, " +
                             "username VARCHAR(16) NOT NULL, " +
                             "first_join TIMESTAMP NOT NULL, " +
                             "last_seen TIMESTAMP NOT NULL, " +
                             "playtime BIGINT DEFAULT 0, " +
                             "last_server VARCHAR(64), " +
                             "settings JSON, " +
                             "statistics JSON)"
             )) {
            stmt.executeUpdate();
        } catch (Exception e) {
            logger.error("Failed to create player_data table", e);
        }
    }

    /**
     * Loads player data from cache or database
     */
    public CompletableFuture<PlayerData> loadPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            // Check cache first
            PlayerData cached = playerCache.get(uuid);
            if (cached != null) {
                return cached;
            }

            // Load from database
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT * FROM player_data WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                PlayerData data;
                if (rs.next()) {
                    data = PlayerData.fromResultSet(rs);
                } else {
                    data = new PlayerData(uuid);
                }

                playerCache.put(uuid, data);
                return data;
            } catch (Exception e) {
                logger.error("Failed to load player data for " + uuid, e);
                return new PlayerData(uuid);
            }
        });
    }

    /**
     * Saves player data to database
     */
    public CompletableFuture<Void> savePlayerData(PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO player_data (uuid, username, first_join, last_seen, playtime, " +
                                 "last_server, settings, statistics) VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE username = ?, last_seen = ?, playtime = ?, " +
                                 "last_server = ?, settings = ?, statistics = ?")) {

                stmt.setString(1, data.getUuid().toString());
                stmt.setString(2, data.getUsername());
                stmt.setTimestamp(3, java.sql.Timestamp.from(data.getFirstJoin()));
                stmt.setTimestamp(4, java.sql.Timestamp.from(data.getLastSeen()));
                stmt.setLong(5, data.getPlaytime());
                stmt.setString(6, data.getLastServer());
                stmt.setString(7, data.getSettingsJson());
                stmt.setString(8, data.getStatisticsJson());

                // Update values
                stmt.setString(9, data.getUsername());
                stmt.setTimestamp(10, java.sql.Timestamp.from(data.getLastSeen()));
                stmt.setLong(11, data.getPlaytime());
                stmt.setString(12, data.getLastServer());
                stmt.setString(13, data.getSettingsJson());
                stmt.setString(14, data.getStatisticsJson());

                stmt.executeUpdate();
            } catch (Exception e) {
                logger.error("Failed to save player data for " + data.getUuid(), e);
            }
        });
    }

    /**
     * Updates a player's last seen time and server
     */
    public void updatePlayerStatus(UUID uuid, String server) {
        PlayerData data = playerCache.get(uuid);
        if (data != null) {
            data.setLastSeen(Instant.now());
            data.setLastServer(server);
        }
    }

    /**
     * Gets cached player data if available
     */
    public Optional<PlayerData> getCachedData(UUID uuid) {
        return Optional.ofNullable(playerCache.get(uuid));
    }

    /**
     * Removes player data from cache
     */
    public void invalidateCache(UUID uuid) {
        playerCache.remove(uuid);
    }

    /**
     * Saves all cached player data
     */
    public void saveAllData() {
        playerCache.values().forEach(data ->
                savePlayerData(data).join()
        );
    }
}
