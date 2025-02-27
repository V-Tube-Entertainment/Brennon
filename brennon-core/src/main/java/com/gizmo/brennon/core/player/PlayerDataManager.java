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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
        createTables();
        setupRedisSubscriptions();
        logger.info("PlayerDataManager enabled successfully");
    }

    @Override
    public void disable() throws Exception {
        saveAllPlayers();
        playerCache.clear();
        logger.info("PlayerDataManager disabled successfully");
    }

    private void createTables() throws Exception {
        try (Connection conn = databaseManager.getConnection()) {
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

    public Optional<BrennonPlayer> getPlayer(UUID uuid) {
        BrennonPlayer player = playerCache.get(uuid);
        if (player != null) {
            return Optional.of(player);
        }
        return loadPlayer(uuid);
    }

    private Optional<BrennonPlayer> loadPlayer(UUID uuid) {
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
                User lpUser = luckPerms.getUserManager().loadUser(uuid)
                        .get(5, TimeUnit.SECONDS);
                player.setLuckPermsUser(lpUser);

                // Load JSON data
                String statsJson = rs.getString("stats");
                String settingsJson = rs.getString("settings");
                String metadataJson = rs.getString("metadata");

                if (statsJson != null) {
                    PlayerStats stats = gson.fromJson(statsJson, PlayerStats.class);
                    player.getStats().copyFrom(stats);
                }
                if (settingsJson != null) {
                    PlayerSettings settings = gson.fromJson(settingsJson, PlayerSettings.class);
                    player.getSettings().copyFrom(settings);
                }
                if (metadataJson != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> metadata = gson.fromJson(metadataJson, Map.class);
                    metadata.forEach(player::setMetadata);
                }

                playerCache.put(uuid, player);
                return Optional.of(player);
            }
        } catch (Exception e) {
            logger.error("Error loading player data for " + uuid, e);
        }
        return Optional.empty();
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
            stmt.setString(8, gson.toJson(player.getAllMetadata()));

            stmt.executeUpdate();
            notifyUpdate(player);
        } catch (Exception e) {
            logger.error("Error saving player data for " + player.getUniqueId(), e);
        }
    }

    private void saveAllPlayers() {
        playerCache.values().forEach(this::savePlayer);
    }

    public void invalidateCache(UUID uuid) {
        playerCache.remove(uuid);
    }

    private void setupRedisSubscriptions() {
        redisManager.subscribe("brennon:player:data", message -> {
            try {
                PlayerDataUpdate update = gson.fromJson(message, PlayerDataUpdate.class);
                handlePlayerUpdate(update);
            } catch (Exception e) {
                logger.error("Error handling player data update", e);
            }
        });
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
        getPlayer(update.uuid()).ifPresent(player -> {
            player.setCurrentServer(update.server());
            player.setOnline(update.online());
            player.setLastSeen(Instant.now());
        });
    }

    private record PlayerDataUpdate(UUID uuid, String username, String server, boolean online) {}
}