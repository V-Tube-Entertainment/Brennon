package com.gizmo.brennon.core.announcement;

import com.google.inject.Inject;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.redis.RedisManager;
import com.gizmo.brennon.core.service.Service;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.slf4j.Logger;

import java.sql.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AnnouncementService implements Service {
    private final Logger logger;
    private final DatabaseManager databaseManager;
    private final RedisManager redisManager;
    private final GsonComponentSerializer gsonSerializer;

    @Inject
    public AnnouncementService(Logger logger, DatabaseManager databaseManager, RedisManager redisManager) {
        this.logger = logger;
        this.databaseManager = databaseManager;
        this.redisManager = redisManager;
        this.gsonSerializer = GsonComponentSerializer.gson();
    }

    @Override
    public void enable() throws Exception {
        initializeDatabase();
    }

    @Override
    public void disable() throws Exception {
        // Cleanup if needed
    }

    private void initializeDatabase() throws Exception {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {

            // Create announcements table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS announcements (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    creator_id CHAR(36) NOT NULL,
                    creator_name VARCHAR(16) NOT NULL,
                    message TEXT NOT NULL,
                    type VARCHAR(32) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    expires_at TIMESTAMP NULL,
                    active BOOLEAN NOT NULL DEFAULT TRUE
                )
            """);

            // Create announcement targets table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS announcement_targets (
                    announcement_id BIGINT NOT NULL,
                    target_type ENUM('SERVER', 'GROUP') NOT NULL,
                    target_value VARCHAR(255) NOT NULL,
                    PRIMARY KEY (announcement_id, target_type, target_value),
                    FOREIGN KEY (announcement_id) REFERENCES announcements(id) ON DELETE CASCADE
                )
            """);
        }
    }

    public Announcement createAnnouncement(UUID creatorId, String creatorName, Component message,
                                           Set<String> targetServers, Set<String> targetGroups, AnnouncementType type, Instant expiresAt) {
        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(
                    """
                    INSERT INTO announcements 
                    (creator_id, creator_name, message, type, expires_at)
                    VALUES (?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, creatorId.toString());
                stmt.setString(2, creatorName);
                stmt.setString(3, gsonSerializer.serialize(message));
                stmt.setString(4, type.name());
                stmt.setTimestamp(5, expiresAt != null ? Timestamp.from(expiresAt) : null);

                stmt.executeUpdate();

                long announcementId;
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        announcementId = rs.getLong(1);
                    } else {
                        throw new SQLException("Failed to get generated announcement ID");
                    }
                }

                // Insert target servers
                if (!targetServers.isEmpty()) {
                    try (PreparedStatement targetStmt = conn.prepareStatement(
                            """
                            INSERT INTO announcement_targets 
                            (announcement_id, target_type, target_value)
                                                    VALUES (?, ?, ?)
                        """)) {
                        for (String server : targetServers) {
                            targetStmt.setLong(1, announcementId);
                            targetStmt.setString(2, "SERVER");
                            targetStmt.setString(3, server);
                            targetStmt.addBatch();
                        }
                        targetStmt.executeBatch();
                    }
                }

                // Insert target groups
                if (!targetGroups.isEmpty()) {
                    try (PreparedStatement targetStmt = conn.prepareStatement(
                            """
                            INSERT INTO announcement_targets 
                            (announcement_id, target_type, target_value)
                            VALUES (?, ?, ?)
                            """)) {
                        for (String group : targetGroups) {
                            targetStmt.setLong(1, announcementId);
                            targetStmt.setString(2, "GROUP");
                            targetStmt.setString(3, group);
                            targetStmt.addBatch();
                        }
                        targetStmt.executeBatch();
                    }
                }

                conn.commit();

                Announcement announcement = new Announcement(
                        announcementId,
                        creatorId,
                        creatorName,
                        message,
                        targetServers,
                        targetGroups,
                        type,
                        Instant.now(),
                        expiresAt,
                        true
                );

                notifyAnnouncementCreated(announcement);
                return announcement;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            logger.error("Failed to create announcement", e);
            return null;
        }
    }

    public boolean deactivateAnnouncement(long announcementId) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE announcements SET active = FALSE WHERE id = ?")) {

            stmt.setLong(1, announcementId);
            int updated = stmt.executeUpdate();

            if (updated > 0) {
                notifyAnnouncementDeactivated(announcementId);
                return true;
            }
        } catch (Exception e) {
            logger.error("Failed to deactivate announcement", e);
        }
        return false;
    }

    public Set<Announcement> getActiveAnnouncements() {
        Set<Announcement> announcements = new HashSet<>();
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT a.*, 
                    GROUP_CONCAT(CASE WHEN at.target_type = 'SERVER' THEN at.target_value END) as servers,
                    GROUP_CONCAT(CASE WHEN at.target_type = 'GROUP' THEN at.target_value END) as groups
                FROM announcements a
                LEFT JOIN announcement_targets at ON a.id = at.announcement_id
                WHERE a.active = TRUE 
                AND (a.expires_at IS NULL OR a.expires_at > CURRENT_TIMESTAMP)
                GROUP BY a.id
                """)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Set<String> servers = new HashSet<>();
                    Set<String> groups = new HashSet<>();

                    String serversList = rs.getString("servers");
                    if (serversList != null) {
                        servers.addAll(Set.of(serversList.split(",")));
                    }

                    String groupsList = rs.getString("groups");
                    if (groupsList != null) {
                        groups.addAll(Set.of(groupsList.split(",")));
                    }

                    announcements.add(new Announcement(
                            rs.getLong("id"),
                            UUID.fromString(rs.getString("creator_id")),
                            rs.getString("creator_name"),
                            gsonSerializer.deserialize(rs.getString("message")),
                            servers,
                            groups,
                            AnnouncementType.valueOf(rs.getString("type")),
                            rs.getTimestamp("created_at").toInstant(),
                            rs.getTimestamp("expires_at") != null ? rs.getTimestamp("expires_at").toInstant() : null,
                            rs.getBoolean("active")
                    ));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get active announcements", e);
        }
        return announcements;
    }

    private void notifyAnnouncementCreated(Announcement announcement) {
        try {
            String channel = "brennon:announcements";
            String message = String.format("CREATE:%d", announcement.id());
            redisManager.publish(channel, message);
        } catch (Exception e) {
            logger.error("Failed to publish announcement creation notification", e);
        }
    }

    private void notifyAnnouncementDeactivated(long announcementId) {
        try {
            String channel = "brennon:announcements";
            String message = String.format("DEACTIVATE:%d", announcementId);
            redisManager.publish(channel, message);
        } catch (Exception e) {
            logger.error("Failed to publish announcement deactivation notification", e);
        }
    }
}