package com.gizmo.brennon.core.punishment;

import com.google.inject.Inject;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.redis.RedisManager;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PunishmentService implements Service {
    private final Logger logger;
    private final DatabaseManager databaseManager;
    private final RedisManager redisManager;

    @Inject
    public PunishmentService(Logger logger, DatabaseManager databaseManager, RedisManager redisManager) {
        this.logger = logger;
        this.databaseManager = databaseManager;
        this.redisManager = redisManager;
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

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS punishments (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    target_id CHAR(36) NOT NULL,
                    target_name VARCHAR(16) NOT NULL,
                    issuer_id CHAR(36) NOT NULL,
                    issuer_name VARCHAR(16) NOT NULL,
                    type VARCHAR(16) NOT NULL,
                    reason TEXT NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    expires_at TIMESTAMP NULL,
                    active BOOLEAN NOT NULL DEFAULT TRUE,
                    INDEX idx_target_id (target_id),
                    INDEX idx_active (active)
                )
            """);
        }
    }

    public Punishment createPunishment(UUID targetId, String targetName, UUID issuerId,
                                       String issuerName, PunishmentType type, String reason, Instant expiresAt) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     """
                     INSERT INTO punishments (target_id, target_name, issuer_id, issuer_name, type, reason, expires_at)
                     VALUES (?, ?, ?, ?, ?, ?, ?)
                     """, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, targetId.toString());
            stmt.setString(2, targetName);
            stmt.setString(3, issuerId.toString());
            stmt.setString(4, issuerName);
            stmt.setString(5, type.name());
            stmt.setString(6, reason);
            stmt.setObject(7, expiresAt);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Punishment punishment = new Punishment(
                            rs.getLong(1),
                            targetId,
                            targetName,
                            issuerId,
                            issuerName,
                            type,
                            reason,
                            Instant.now(),
                            expiresAt,
                            true
                    );

                    // Notify other servers via Redis
                    notifyPunishmentCreated(punishment);

                    return punishment;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to create punishment", e);
        }
        return null;
    }

    private void notifyPunishmentCreated(Punishment punishment) {
        try {
            String channel = "brennon:punishments";
            String message = String.format("CREATE:%d", punishment.id());
            redisManager.publish(channel, message);;
        } catch (Exception e) {
            logger.error("Failed to publish punishment creation notification", e);
        }
    }

    public boolean revokePunishment(long punishmentId, UUID revokedBy) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE punishments SET active = FALSE WHERE id = ?")) {

            stmt.setLong(1, punishmentId);
            int updated = stmt.executeUpdate();

            if (updated > 0) {
                // Notify other servers via Redis
                String channel = "brennon:punishments";
                String message = String.format("REVOKE:%d:%s", punishmentId, revokedBy);
                redisManager.publish(channel, message);
                return true;
            }
        } catch (Exception e) {
            logger.error("Failed to revoke punishment", e);
        }
        return false;
    }

    public List<Punishment> getActivePunishments(UUID targetId) {
        List<Punishment> punishments = new ArrayList<>();
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     """
                     SELECT * FROM punishments 
                     WHERE target_id = ? AND active = TRUE 
                     AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)
                     """)) {

            stmt.setString(1, targetId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    punishments.add(mapResultSetToPunishment(rs));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get active punishments", e);
        }
        return punishments;
    }

    private Punishment mapResultSetToPunishment(ResultSet rs) throws Exception {
        return new Punishment(
                rs.getLong("id"),
                UUID.fromString(rs.getString("target_id")),
                rs.getString("target_name"),
                UUID.fromString(rs.getString("issuer_id")),
                rs.getString("issuer_name"),
                PunishmentType.valueOf(rs.getString("type")),
                rs.getString("reason"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("expires_at") != null ? rs.getTimestamp("expires_at").toInstant() : null,
                rs.getBoolean("active")
        );
    }
}
