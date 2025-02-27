package com.gizmo.brennon.core.player;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.redis.RedisManager;
import net.luckperms.api.LuckPerms;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class PlayerManager {
    private final Logger logger;
    private final DatabaseManager databaseManager;
    private final RedisManager redisManager;
    private final LuckPerms luckPerms;
    private final Map<UUID, BrennonPlayer> playerCache;

    @Inject
    public PlayerManager(Logger logger, DatabaseManager databaseManager,
                         RedisManager redisManager, LuckPerms luckPerms) {
        this.logger = logger;
        this.databaseManager = databaseManager;
        this.redisManager = redisManager;
        this.luckPerms = luckPerms;
        this.playerCache = new ConcurrentHashMap<>();
    }

    public Optional<BrennonPlayer> getPlayer(UUID uuid) {
        return Optional.ofNullable(playerCache.computeIfAbsent(uuid, this::loadPlayer));
    }

    private BrennonPlayer loadPlayer(UUID uuid) {
        try (Connection conn = databaseManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM players WHERE uuid = ?"
            );
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String username = rs.getString("username");
                BrennonPlayer player = new BrennonPlayer(uuid, username);

                player.setDisplayName(rs.getString("display_name"));
                player.setFirstJoin(rs.getTimestamp("first_join").toInstant());
                player.setLastSeen(rs.getTimestamp("last_seen").toInstant());
                player.setCurrentServer(rs.getString("current_server"));

                // Load LuckPerms user
                luckPerms.getUserManager().loadUser(uuid).thenAcceptAsync(player::setLuckPermsUser);

                return player;
            }
        } catch (SQLException e) {
            logger.error("Failed to load player data for " + uuid, e);
        }
        return null;
    }

    public void savePlayer(BrennonPlayer player) {
        try (Connection conn = databaseManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO players (
                    uuid, username, display_name, first_join, last_seen, 
                    current_server, stats, settings, metadata
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    username = VALUES(username),
                    display_name = VALUES(display_name),
                    last_seen = VALUES(last_seen),
                    current_server = VALUES(current_server),
                    stats = VALUES(stats),
                    settings = VALUES(settings),
                    metadata = VALUES(metadata)
            """);

            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getUsername());
            stmt.setString(3, player.getDisplayName());
            stmt.setObject(4, player.getFirstJoin());
            stmt.setObject(5, player.getLastSeen());
            stmt.setString(6, player.getCurrentServer());
            stmt.setString(7, new Gson().toJson(player.getStats()));
            stmt.setString(8, new Gson().toJson(player.getSettings()));
            stmt.setString(9, new Gson().toJson(player.getAllMetadata()));

            stmt.executeUpdate();

            // Notify other servers about the update
            notifyUpdate(player);
        } catch (SQLException e) {
            logger.error("Failed to save player data for " + player.getUniqueId(), e);
        }
    }

    public void updatePlayerServer(UUID uuid, String server) {
        getPlayer(uuid).ifPresent(player -> {
            player.setCurrentServer(server);
            player.setLastSeen(Instant.now());
            savePlayer(player);
        });
    }

    private void notifyUpdate(BrennonPlayer player) {
        PlayerUpdate update = new PlayerUpdate(
                player.getUniqueId(),
                player.getUsername(),
                player.getCurrentServer(),
                player.isOnline()
        );
        redisManager.publish("brennon:player:update", new Gson().toJson(update));
    }

    public void invalidateCache(UUID uuid) {
        playerCache.remove(uuid);
    }

    private record PlayerUpdate(UUID uuid, String username, String server, boolean online) {}
}