package com.gizmo.brennon.core.punishment;

import com.google.inject.Inject;
import com.gizmo.brennon.core.database.DatabaseManager;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class IPPunishmentManager {
    private final Logger logger;
    private final DatabaseManager databaseManager;

    @Inject
    public IPPunishmentManager(Logger logger, DatabaseManager databaseManager) {
        this.logger = logger;
        this.databaseManager = databaseManager;
    }

    public void initialize() throws SQLException {
        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS ip_punishments (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    ip_address VARCHAR(45) NOT NULL,
                    type VARCHAR(32) NOT NULL,
                    reason TEXT NOT NULL,
                    issuer_id UUID NOT NULL,
                    issuer_name VARCHAR(16) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    expires_at TIMESTAMP,
                    active BOOLEAN NOT NULL DEFAULT TRUE,
                    INDEX (ip_address),
                    INDEX (active)
                )""")) {
                stmt.executeUpdate();
            }
        }
    }

    public List<IPPunishment> getActivePunishments(String ipAddress) {
        List<IPPunishment> punishments = new ArrayList<>();

        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT *
                FROM ip_punishments
                WHERE ip_address = ?
                AND active = TRUE
                AND (expires_at IS NULL OR expires_at > ?)
                ORDER BY created_at DESC
                """)) {

            stmt.setString(1, ipAddress);
            stmt.setObject(2, Instant.now());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    punishments.add(mapResultSetToPunishment(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get active punishments for IP {}", ipAddress, e);
        }

        return punishments;
    }

    public Optional<IPPunishment> getPunishment(long id) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT *
                FROM ip_punishments
                WHERE id = ?
                """)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPunishment(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get IP punishment {}", id, e);
        }

        return Optional.empty();
    }

    public IPPunishment addPunishment(String ipAddress, PunishmentType type, String reason,
                                      UUID issuerId, String issuerName, Instant expiresAt) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO ip_punishments 
                (ip_address, type, reason, issuer_id, issuer_name, created_at, expires_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, PreparedStatement.RETURN_GENERATED_KEYS)) {

            Instant now = Instant.now();
            stmt.setString(1, ipAddress);
            stmt.setString(2, type.name());
            stmt.setString(3, reason);
            stmt.setString(4, issuerId.toString());
            stmt.setString(5, issuerName);
            stmt.setObject(6, now);
            stmt.setObject(7, expiresAt);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new IPPunishment(
                            rs.getLong(1),
                            ipAddress,
                            type,
                            reason,
                            issuerId,
                            issuerName,
                            now,
                            expiresAt,
                            true
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to add IP punishment for {}", ipAddress, e);
        }

        return null;
    }

    private IPPunishment mapResultSetToPunishment(ResultSet rs) throws SQLException {
        return new IPPunishment(
                rs.getLong("id"),
                rs.getString("ip_address"),
                PunishmentType.valueOf(rs.getString("type")),
                rs.getString("reason"),
                UUID.fromString(rs.getString("issuer_id")),
                rs.getString("issuer_name"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("expires_at") != null ? rs.getTimestamp("expires_at").toInstant() : null,
                rs.getBoolean("active")
        );
    }

    public boolean revokePunishment(long id) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                UPDATE ip_punishments
                SET active = FALSE
                WHERE id = ?
                """)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error("Failed to revoke IP punishment {}", id, e);
            return false;
        }
    }
}
