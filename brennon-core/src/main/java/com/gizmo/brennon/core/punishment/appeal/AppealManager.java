package com.gizmo.brennon.core.punishment.appeal;

import com.gizmo.brennon.core.punishment.appeal.search.AppealSearchOptions;
import com.gizmo.brennon.core.punishment.appeal.search.AppealSearchResult;
import com.google.inject.Inject;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.messaging.MessageBroker;
import com.gizmo.brennon.core.punishment.PunishmentService;
import com.gizmo.brennon.core.service.Service;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class AppealManager implements Service {
    private static final Duration APPEAL_COOLDOWN = Duration.ofDays(7);
    private static final int MAX_ACTIVE_APPEALS = 3;

    private final Logger logger;
    private final DatabaseManager databaseManager;
    private final MessageBroker messageBroker;
    private final PunishmentService punishmentService;
    private final Gson gson;

    @Inject
    public AppealManager(Logger logger, DatabaseManager databaseManager,
                         MessageBroker messageBroker, PunishmentService punishmentService) {
        this.logger = logger;
        this.databaseManager = databaseManager;
        this.messageBroker = messageBroker;
        this.punishmentService = punishmentService;
        this.gson = new Gson();
    }

    @Override
    public void enable() throws Exception {
        initialize();
        setupMessageBroker();
    }

    @Override
    public void disable() throws Exception {
        // No specific cleanup needed
    }

    private void initialize() throws SQLException {
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
                    cooldown_until TIMESTAMP,
                    attempt_count INT NOT NULL DEFAULT 1,
                    FOREIGN KEY (punishment_id) REFERENCES punishments(id),
                    INDEX idx_status (status),
                    INDEX idx_appealer_id (appealer_id),
                    INDEX idx_created_at (created_at)
                )""")) {
                stmt.executeUpdate();
            }
        }
    }

    private void setupMessageBroker() {
        messageBroker.subscribe("brennon:appeals", message -> {
            try {
                JsonObject data = gson.fromJson(message, JsonObject.class);
                String type = data.get("type").getAsString();

                switch (type) {
                    case "APPEAL_ESCALATE" -> {
                        long appealId = data.get("appealId").getAsLong();
                        UUID handlerId = UUID.fromString(data.get("handlerId").getAsString());
                        String handlerName = data.get("handlerName").getAsString();
                        escalateAppeal(appealId, handlerId, handlerName);
                    }
                }
            } catch (Exception e) {
                logger.error("Error handling appeal message", e);
            }
        });
    }

    public Optional<Appeal> createAppeal(long punishmentId, UUID appealerId, String reason) {
        if (!canUserAppeal(appealerId)) {
            return Optional.empty();
        }

        if (hasPendingAppeal(punishmentId)) {
            return Optional.empty();
        }

        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try {
                Appeal appeal;
                try (PreparedStatement stmt = conn.prepareStatement("""
                    INSERT INTO appeals (punishment_id, appealer_id, reason, created_at, attempt_count)
                    SELECT ?, ?, ?, ?, COALESCE(
                        (SELECT MAX(attempt_count) + 1 
                         FROM appeals 
                         WHERE punishment_id = ? AND appealer_id = ?),
                        1)
                    """, PreparedStatement.RETURN_GENERATED_KEYS)) {

                    Instant now = Instant.now();
                    stmt.setLong(1, punishmentId);
                    stmt.setString(2, appealerId.toString());
                    stmt.setString(3, reason);
                    stmt.setObject(4, now);
                    stmt.setLong(5, punishmentId);
                    stmt.setString(6, appealerId.toString());

                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("Failed to get generated appeal ID");
                        }
                        appeal = Appeal.create(punishmentId, appealerId, reason);
                    }
                }

                // Set cooldown for the user
                try (PreparedStatement stmt = conn.prepareStatement("""
                    UPDATE appeals 
                    SET cooldown_until = ? 
                    WHERE id = ?
                    """)) {
                    stmt.setObject(1, Instant.now().plus(APPEAL_COOLDOWN));
                    stmt.setLong(2, appeal.id());
                    stmt.executeUpdate();
                }

                conn.commit();
                notifyAppealCreated(appeal);
                return Optional.of(appeal);

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Failed to create appeal for punishment {}", punishmentId, e);
            return Optional.empty();
        }
    }

    public boolean handleAppeal(long appealId, UUID handlerId, String handlerName,
                                AppealStatus status, String response) {
        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try {
                Appeal appeal;
                try (PreparedStatement stmt = conn.prepareStatement("""
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

                    if (stmt.executeUpdate() == 0) {
                        return false;
                    }

                    appeal = getAppeal(appealId).orElseThrow();
                }

                if (status == AppealStatus.APPROVED) {
                    punishmentService.revokePunishment(appeal.punishmentId(), handlerId);
                }

                conn.commit();
                notifyAppealHandled(appeal);
                return true;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Failed to handle appeal {}", appealId, e);
            return false;
        }
    }

    public boolean escalateAppeal(long appealId, UUID handlerId, String handlerName) {
        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try {
                Appeal appeal;
                try (PreparedStatement stmt = conn.prepareStatement("""
                    UPDATE appeals
                    SET status = ?, handler_id = ?, handler_name = ?, handled_at = ?
                    WHERE id = ? AND status = 'PENDING'
                    """)) {

                    stmt.setString(1, AppealStatus.ESCALATED.name());
                    stmt.setString(2, handlerId.toString());
                    stmt.setString(3, handlerName);
                    stmt.setObject(4, Instant.now());
                    stmt.setLong(5, appealId);

                    if (stmt.executeUpdate() == 0) {
                        return false;
                    }

                    appeal = getAppeal(appealId).orElseThrow();
                }

                conn.commit();
                notifyAppealEscalated(appeal);
                return true;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Failed to escalate appeal {}", appealId, e);
            return false;
        }
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

    private boolean canUserAppeal(UUID appealerId) {
        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            // Check active appeals count
            try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT COUNT(*)
                FROM appeals
                WHERE appealer_id = ?
                AND status = 'PENDING'
                """)) {

                stmt.setString(1, appealerId.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) >= MAX_ACTIVE_APPEALS) {
                        return false;
                    }
                }
            }

            // Check cooldown
            try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT cooldown_until
                FROM appeals
                WHERE appealer_id = ?
                AND cooldown_until > ?
                LIMIT 1
                """)) {

                stmt.setString(1, appealerId.toString());
                stmt.setObject(2, Instant.now());
                try (ResultSet rs = stmt.executeQuery()) {
                    return !rs.next();
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to check appeal eligibility for user {}", appealerId, e);
            return false;
        }
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
                rs.getTimestamp("handled_at") != null ? rs.getTimestamp("handled_at").toInstant() : null,
                rs.getInt("attempt_count")
        );
    }

    private void notifyAppealCreated(Appeal appeal) {
        JsonObject data = new JsonObject();
        data.addProperty("type", "APPEAL_CREATED");
        data.addProperty("appealId", appeal.id());
        data.addProperty("punishmentId", appeal.punishmentId());
        data.addProperty("appealerId", appeal.appealerId().toString());
        data.addProperty("attemptCount", appeal.attemptCount());
        messageBroker.publish("brennon:appeals", gson.toJson(data));
    }

    private void notifyAppealHandled(Appeal appeal) {
        JsonObject data = new JsonObject();
        data.addProperty("type", "APPEAL_HANDLED");
        data.addProperty("appealId", appeal.id());
        data.addProperty("status", appeal.status().name());
        data.addProperty("handlerId", appeal.handlerId().toString());
        data.addProperty("handlerName", appeal.handlerName());
        data.addProperty("response", appeal.response());
        messageBroker.publish("brennon:appeals", gson.toJson(data));
    }

    private void notifyAppealEscalated(Appeal appeal) {
        JsonObject data = new JsonObject();
        data.addProperty("type", "APPEAL_ESCALATED");
        data.addProperty("appealId", appeal.id());
        data.addProperty("punishmentId", appeal.punishmentId());
        data.addProperty("handlerId", appeal.handlerId().toString());
        data.addProperty("handlerName", appeal.handlerName());
        messageBroker.publish("brennon:appeals", gson.toJson(data));
    }

    public List<Appeal> getAppealHistory(UUID appealerId, int page, int pageSize) {
        List<Appeal> appeals = new ArrayList<>();
        int offset = (page - 1) * pageSize;

        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT *
                FROM appeals
                WHERE appealer_id = ?
                ORDER BY created_at DESC
                LIMIT ? OFFSET ?
                """)) {

            stmt.setString(1, appealerId.toString());
            stmt.setInt(2, pageSize);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appeals.add(mapResultSetToAppeal(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get appeal history for user {}", appealerId, e);
        }

        return appeals;
    }

    public int getTotalAppeals(UUID appealerId) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT COUNT(*)
                FROM appeals
                WHERE appealer_id = ?
                """)) {

            stmt.setString(1, appealerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get total appeals count for user {}", appealerId, e);
        }
        return 0;
    }

    public List<Appeal> getHandlerHistory(UUID handlerId, int page, int pageSize) {
        List<Appeal> appeals = new ArrayList<>();
        int offset = (page - 1) * pageSize;

        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT *
                FROM appeals
                WHERE handler_id = ?
                ORDER BY handled_at DESC
                LIMIT ? OFFSET ?
                """)) {

            stmt.setString(1, handlerId.toString());
            stmt.setInt(2, pageSize);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appeals.add(mapResultSetToAppeal(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get handler history for user {}", handlerId, e);
        }

        return appeals;
    }

    public Map<AppealStatus, Integer> getAppealStatistics(UUID appealerId) {
        Map<AppealStatus, Integer> stats = new EnumMap<>(AppealStatus.class);
        for (AppealStatus status : AppealStatus.values()) {
            stats.put(status, 0);
        }

        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT status, COUNT(*) as count
                FROM appeals
                WHERE appealer_id = ?
                GROUP BY status
                """)) {

            stmt.setString(1, appealerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AppealStatus status = AppealStatus.valueOf(rs.getString("status"));
                    stats.put(status, rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get appeal statistics for user {}", appealerId, e);
        }

        return stats;
    }

    public AppealSearchResult searchAppeals(AppealSearchOptions options) {
        List<Appeal> appeals = new ArrayList<>();
        int totalResults = 0;

        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            // Build the search query
            StringBuilder queryBuilder = new StringBuilder();
            List<Object> params = new ArrayList<>();

            queryBuilder.append("""
            SELECT SQL_CALC_FOUND_ROWS *
            FROM appeals
            WHERE 1=1
        """);

            // Add search conditions
            if (!options.query().isEmpty()) {
                queryBuilder.append("""
                AND (
                    LOWER(reason) LIKE LOWER(?)
                    OR LOWER(response) LIKE LOWER(?)
                    OR LOWER(handler_name) LIKE LOWER(?)
                )
            """);
                String searchPattern = "%" + options.query() + "%";
                params.add(searchPattern);
                params.add(searchPattern);
                params.add(searchPattern);
            }

            if (!options.statuses().isEmpty()) {
                queryBuilder.append("AND status IN (");
                queryBuilder.append(String.join(",", Collections.nCopies(options.statuses().size(), "?")));
                queryBuilder.append(") ");
                params.addAll(options.statuses().stream().map(Enum::name).toList());
            }

            if (options.startDate() != null) {
                queryBuilder.append("AND created_at >= ? ");
                params.add(options.startDate());
            }

            if (options.endDate() != null) {
                queryBuilder.append("AND created_at <= ? ");
                params.add(options.endDate());
            }

            if (options.handlerId() != null) {
                queryBuilder.append("AND handler_id = ? ");
                params.add(options.handlerId().toString());
            }

            if (options.appealerId() != null) {
                queryBuilder.append("AND appealer_id = ? ");
                params.add(options.appealerId().toString());
            }

            // Add ordering and pagination
            queryBuilder.append("ORDER BY created_at DESC ");
            queryBuilder.append("LIMIT ? OFFSET ?");

            int offset = (options.page() - 1) * options.pageSize();
            params.add(options.pageSize());
            params.add(offset);

            // Execute search query
            try (PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    setParameter(stmt, i + 1, params.get(i));
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        appeals.add(mapResultSetToAppeal(rs));
                    }
                }
            }

            // Get total results count
            try (PreparedStatement countStmt = conn.prepareStatement("SELECT FOUND_ROWS()");
                 ResultSet countRs = countStmt.executeQuery()) {
                if (countRs.next()) {
                    totalResults = countRs.getInt(1);
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to search appeals", e);
            return new AppealSearchResult(
                    Collections.emptyList(),
                    0,
                    options.page(),
                    0
            );
        }

        int totalPages = (totalResults + options.pageSize() - 1) / options.pageSize();

        return new AppealSearchResult(
                appeals,
                totalResults,
                options.page(),
                totalPages
        );
    }

    private void setParameter(PreparedStatement stmt, int index, Object value) throws SQLException {
        if (value instanceof Instant instant) {
            stmt.setTimestamp(index, Timestamp.from(instant));
        } else {
            stmt.setObject(index, value);
        }
    }
}