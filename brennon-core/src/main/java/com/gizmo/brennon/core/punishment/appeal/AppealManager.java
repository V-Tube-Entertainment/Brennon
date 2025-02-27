package com.gizmo.brennon.core.punishment.appeal;

import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.punishment.AppealStatus;
import com.google.inject.Inject;
import org.slf4j.Logger;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppealManager {
    private final DatabaseManager databaseManager;
    private final Logger logger;

    @Inject
    public AppealManager(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    public void initializeDatabase() throws SQLException {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS appeals (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    punishment_id BIGINT NOT NULL,
                    appealer_id CHAR(36) NOT NULL,
                    appealer_name VARCHAR(16) NOT NULL,
                    reason TEXT NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
                    handler_id CHAR(36),
                    handler_name VARCHAR(16),
                    response TEXT,
                    handled_at TIMESTAMP,
                    FOREIGN KEY (punishment_id) REFERENCES punishments(id) ON DELETE CASCADE,
                    INDEX idx_punishment_id (punishment_id),
                    INDEX idx_status (status)
                )
            """);
        }
    }

    public Appeal createAppeal(long punishmentId, UUID appealerId, String appealerName, String reason) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     """
                     INSERT INTO appeals (punishment_id, appealer_id, appealer_name, reason)
                     VALUES (?, ?, ?, ?)
                     """, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, punishmentId);
            stmt.setString(2, appealerId.toString());
            stmt.setString(3, appealerName);
            stmt.setString(4, reason);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Appeal(
                            rs.getLong(1),
                            punishmentId,
                            appealerId,
                            appealerName,
                            reason,
                            Instant.now(),
                            AppealStatus.PENDING,
                            null,
                            null,
                            null,
                            null
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to create appeal", e);
        }
        return null;
    }

    public List<Appeal> getPendingAppeals() {
        List<Appeal> appeals = new ArrayList<>();
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     """
                     SELECT * FROM appeals 
                     WHERE status = 'PENDING'
                     ORDER BY created_at ASC
                     """)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appeals.add(mapResultSetToAppeal(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get pending appeals", e);
        }
        return appeals;
    }

    private Appeal mapResultSetToAppeal(ResultSet rs) throws SQLException {
        return new Appeal(
                rs.getLong("id"),
                rs.getLong("punishment_id"),
                UUID.fromString(rs.getString("appealer_id")),
                rs.getString("appealer_name"),
                rs.getString("reason"),
                rs.getTimestamp("created_at").toInstant(),
                AppealStatus.valueOf(rs.getString("status")),
                rs.getString("handler_id") != null ? UUID.fromString(rs.getString("handler_id")) : null,
                rs.getString("handler_name"),
                rs.getString("response"),
                rs.getTimestamp("handled_at") != null ? rs.getTimestamp("handled_at").toInstant() : null
        );
    }
}
