package com.gizmo.brennon.core.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.redis.RedisManager;
import com.gizmo.brennon.core.service.Service;
import com.google.gson.Gson;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class PlayerDataManager implements Service {
    private final Logger logger;
    private final DatabaseManager databaseManager;
    private final RedisManager redisManager;
    private final LuckPerms luckPerms;
    private final Gson gson;
    private final Map<UUID, BrennonPlayer> playerCache;

    @Inject
    public PlayerDataManager(Logger logger, DatabaseManager databaseManager,
                             RedisManager redisManager, LuckPerms luckPerms) {
        this.logger = logger;
        this.databaseManager = databaseManager;
        this.redisManager = redisManager;
        this.luckPerms = luckPerms;
        this.gson = new Gson();
        this.playerCache = new ConcurrentHashMap<>();
    }

    @Override
    public void enable() throws Exception {
        // Initialize database tables
        createTables();

        // Subscribe to Redis channels for player data updates
        subscribeToRedisChannels();

        logger.info("PlayerDataManager enabled successfully");
    }

    @Override
    public void disable() throws Exception {
        // Save all cached player data
        saveAllPlayers();

        // Clear cache
        playerCache.clear();

        logger.info("PlayerDataManager disabled successfully");
    }

    private void createTables() throws Exception {
        try (Connection conn = databaseManager.getConnection()) {
            // Players table
            conn.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    username VARCHAR(16) NOT NULL,
                    display_name VARCHAR(48),
                    first_join TIMESTAMP,
                    last_seen TIMESTAMP,
                    stats TEXT,
                    settings TEXT,
                    metadata TEXT
                )
            """);
        }
    }

    private void subscribeToRedisChannels() {
        redisManager.subscribe("brennon:player:data", message -> {
            try {
                PlayerDataUpdate update = gson.fromJson(message, PlayerDataUpdate.class);
                handlePlayerUpdate(update);
            } catch (Exception e) {
                logger.error("Error handling player data update", e);
            }
        });
    }

    public BrennonPlayer getPlayer(UUID uuid) {
        return playerCache.computeIfAbsent(uuid, this::loadPlayer);
    }

    private BrennonPlayer loadPlayer(UUID uuid) {
        try (Connection conn = databaseManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM players WHERE uuid = ?"
            );
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                BrennonPlayer player = new BrennonPlayer(
                        uuid,
                        rs.getString("username")
                );
                player.setDisplayName(rs.getString("display_name"));
                player.setFirstJoin(rs.getTimestamp("first_join").toInstant());
                player.setLastSeen(rs.getTimestamp("last_seen").toInstant());

                // Load LuckPerms data
                User lpUser = luckPerms.getUserManager().loadUser(uuid).join();
                player.setLuckPermsUser(lpUser);
                player.setPrimaryGroup(luckPerms.getGroupManager().getGroup(lpUser.getPrimaryGroup()));

                // Load JSON data
                PlayerStats stats = gson.fromJson(rs.getString("stats"), PlayerStats.class);
                PlayerSettings settings = gson.fromJson(rs.getString("settings"), PlayerSettings.class);
                if (stats != null) player.getStats().copyFrom(stats);
                if (settings != null) player.getSettings().copyFrom(settings);

                return player;
            }
        } catch (Exception e) {
            logger.error("Error loading player data for " + uuid, e);
        }
        return null;
    }

    public void savePlayer(BrennonPlayer player) {
        try (Connection conn = databaseManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO players (uuid, username, display_name, first_join, last_seen, stats, settings, metadata)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                username = VALUES(username),
                display_name = VALUES(display_name),
                last_seen = VALUES(last_seen),
                stats = VALUES(stats),
                settings = VALUES(settings),
                metadata = VALUES(metadata)
            """);

            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getUsername());
            stmt.setString(3, player.getDisplayName());
            stmt.setObject(4, player.getFirstJoin());
            stmt.setObject(5, player.getLastSeen());
            stmt.setString(6, gson.toJson(player.getStats()));
            stmt.setString(7, gson.toJson(player.getSettings()));
            stmt.setString(8, gson.toJson(player.getMetadata()));

            stmt.executeUpdate();

            // Notify other servers about the update
            notifyUpdate(player);
        } catch (Exception e) {
            logger.error("Error saving player data for " + player.getUniqueId(), e);
        }
    }

    private void notifyUpdate(BrennonPlayer player) {
        PlayerDataUpdate update = new PlayerDataUpdate(
                player.getUniqueId(),
                player.getUsername(),
                player.getCurrentServer(),
                player.isOnline()
        );
        redisManager.publish("brennon:player:data", gson.toJson(update));
    }

    private void handlePlayerUpdate(PlayerDataUpdate update) {
        BrennonPlayer player = playerCache.get(update.uuid());
        if (player != null) {
            player.setCurrentServer(update.server());
            player.setOnline(update.online());
            player.setLastSeen(Instant.now());
        }
    }

    private void saveAllPlayers() {
        playerCache.values().forEach(this::savePlayer);
    }

    public void invalidateCache(UUID uuid) {
        playerCache.remove(uuid);
    }

    private record PlayerDataUpdate(UUID uuid, String username, String server, boolean online) {}
}
