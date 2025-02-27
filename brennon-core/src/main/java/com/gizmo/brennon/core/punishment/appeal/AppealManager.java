package com.gizmo.brennon.core.punishment.appeal;

import com.google.inject.Inject;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.messaging.MessageBroker;
import com.gizmo.brennon.core.punishment.PunishmentManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

public class AppealManager {
    private final Logger logger;
    private final DatabaseManager databaseManager;
    private final MessageBroker messageBroker;
    private final PunishmentManager punishmentManager;
    private final Gson gson;

    @Inject
    public AppealManager(Logger logger, DatabaseManager databaseManager,
                         MessageBroker messageBroker, PunishmentManager punishmentManager) {
        this.logger = logger;
        this.databaseManager = databaseManager;
        this.messageBroker = messageBroker;
        this.punishmentManager = punishmentManager;
        this.gson = new Gson();
    }

    public void initialize() throws SQLException {
        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS appeals (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    punishment_id BIGINT NOT NULL,
                    appealer_id CHAR(36) NOT NULL,
                    reason TEXT NOT NULL,
                    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
                    handler_id CHAR(36),
                    handler_name VARCHAR(16),
                    response TEXT,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    handled_at TIMESTAMP,
                    FOREIGN KEY (punishment_id) REFERENCES punishments(id),
                    INDEX (status),
                    INDEX (appealer_id),
                    INDEX (created_at)
                )""")) {
                stmt.executeUpdate();
            }
        }
    }

    public Optional<Appeal> createAppeal(long punishmentId, UUID appealerId, String reason) {
        // Check if there's already a pending appeal
        if (hasPendingAppeal(punishmentId)) {
            return Optional.empty();
        }

        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO appeals (punishment_id, appealer_id, reason, created_at)
                VALUES (?, ?, ?, ?)
                """, PreparedStatement.RETURN_GENERATED_KEYS)) {

            Instant now = Instant.now();
            stmt.setLong(1, punishmentId);
            stmt.setString(2, appealerId.toString());
            stmt.setString(3, reason);
            stmt.setObject(4, now);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Appeal appeal = Appeal.create(punishmentId, appealerId, reason);
                    notifyAppealCreated(appeal);
                    return Optional.of(appeal);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to create appeal for punishment {}", punishmentId, e);
        }

        return Optional.empty();
    }

    public boolean handleAppeal(long appealId, UUID handlerId, String handlerName,
                                AppealStatus status, String response) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                UPDATE appeals
                SET status = ?, handler_id = ?, handler_name = ?, 
                    response = ?, handled_at = ?
                WHERE id = ? AND status = 'PENDING'
                """)) {

            Instant now = Instant.now();
            stmt.setString(1, status.name());
            stmt.setString(2, handlerId.toString());
            stmt.setString(3, handlerName);
            stmt.setString(4, response);
            stmt.setObject(5, now);
            stmt.setLong(6, appealId);

            if (stmt.executeUpdate() > 0) {
                getAppeal(appealId).ifPresent(appeal -> {
                    // If approved, remove the punishment
                    if (status == AppealStatus.APPROVED) {
                        punishmentManager.revokePunishment(appeal.punishmentId(), handlerId, handlerName,
                                "Appeal approved: " + response);
                    }
                    notifyAppealHandled(appeal);
                });
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to handle appeal {}", appealId, e);
        }

        return false;
    }

    public List<Appeal> getPendingAppeals() {
        List<Appeal> appeals = new ArrayList<>();

        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT *
                FROM appeals
                WHERE status = 'PENDING'
                ORDER BY created_at ASC
                """);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                appeals.add(mapResultSetToAppeal(rs));
            }
        } catch (SQLException e) {
            logger.error("Failed to get pending appeals", e);
        }

        return appeals;
    }

    public Optional<Appeal> getAppeal(long appealId) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT *
                FROM appeals
                WHERE id = ?
                """)) {

            stmt.setLong(1, appealId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAppeal(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get appeal {}", appealId, e);
        }

        return Optional.empty();
    }

    private boolean hasPendingAppeal(long punishmentId) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT COUNT(*) 
                FROM appeals
                WHERE punishment_id = ? AND status = 'PENDING'
                """)) {

            stmt.setLong(1, punishmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Failed to check pending appeals for punishment {}", punishmentId, e);
            return true; // Fail safe - prevent creating new appeal if we can't check
        }
    }

    private Appeal mapResultSetToAppeal(ResultSet rs) throws SQLException {
        return new Appeal(
                rs.getLong("id"),
                rs.getLong("punishment_id"),
                UUID.fromString(rs.getString("appealer_id")),
                rs.getString("reason"),
                AppealStatus.valueOf(rs.getString("status")),
                rs.getString("handler_id") != null ? UUID.fromString(rs.getString("handler_id")) : null,
                rs.getString("handler_name"),
                rs.getString("response"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("handled_at") != null ? rs.getTimestamp("handled_at").toInstant() : null
        );
    }

    private void notifyAppealCreated(Appeal appeal) {
        JsonObject data = new JsonObject();
        data.addProperty("type", "APPEAL_CREATED");
        data.addProperty("appealId", appeal.id());
        data.addProperty("punishmentId", appeal.punishmentId());
        data.addProperty("appealerId", appeal.appealerId().toString());
        messageBroker.publish("brennon:appeals", gson.toJson(data));
    }

    private void notifyAppealHandled(Appeal appeal) {
        JsonObject data = new JsonObject();
        data.addProperty("type", "APPEAL_HANDLED");
        data.addProperty("appealId", appeal.id());
        data.addProperty("status", appeal.status().name());
        data.addProperty("handlerId", appeal.handlerId().toString());
        data.addProperty("handlerName", appeal.handlerName());
        messageBroker.publish("brennon:appeals", gson.toJson(data));
    }
}
