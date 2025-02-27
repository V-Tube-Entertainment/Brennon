package com.gizmo.brennon.core.logging;

import com.google.inject.Inject;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NetworkLogger implements Service {
    private final Logger logger;
    private final DatabaseManager databaseManager;

    @Inject
    public NetworkLogger(Logger logger, DatabaseManager databaseManager) {
        this.logger = logger;
        this.databaseManager = databaseManager;
    }

    @Override
    public void enable() throws Exception {
        initializeDatabase();
    }

    private void initializeDatabase() throws SQLException {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS network_logs (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    timestamp TIMESTAMP NOT NULL,
                    server_id VARCHAR(64),
                    level VARCHAR(16) NOT NULL,
                    category VARCHAR(32) NOT NULL,
                    message TEXT NOT NULL,
                    user_id CHAR(36),
                    user_name VARCHAR(16),
                    metadata JSON,
                    INDEX idx_timestamp (timestamp),
                    INDEX idx_category (category),
                    INDEX idx_level (level)
                )
            """);
        }
    }

    public void log(LogEntry entry) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO network_logs 
                (timestamp, server_id, level, category, message, user_id, user_name, metadata)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """)) {

            stmt.setTimestamp(1, Timestamp.from(entry.timestamp()));
            stmt.setString(2, entry.serverId());
            stmt.setString(3, entry.level().name());
            stmt.setString(4, entry.category());
            stmt.setString(5, entry.message());
            stmt.setString(6, entry.userId() != null ? entry.userId().toString() : null);
            stmt.setString(7, entry.userName());
            stmt.setString(8, entry.metadata() != null ? entry.metadata().toString() : null);

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to save log entry", e);
        }
    }

    public List<LogEntry> getLogs(LogQueryFilter filter) {
        List<LogEntry> entries = new ArrayList<>();
        StringBuilder query = new StringBuilder("""
            SELECT * FROM network_logs WHERE 1=1
            """);
        List<Object> params = new ArrayList<>();

        if (filter.from() != null) {
            query.append(" AND timestamp >= ?");
            params.add(Timestamp.from(filter.from()));
        }
        if (filter.to() != null) {
            query.append(" AND timestamp <= ?");
            params.add(Timestamp.from(filter.to()));
        }
        if (filter.level() != null) {
            query.append(" AND level = ?");
            params.add(filter.level().name());
        }
        if (filter.category() != null) {
            query.append(" AND category = ?");
            params.add(filter.category());
        }
        if (filter.serverId() != null) {
            query.append(" AND server_id = ?");
            params.add(filter.serverId());
        }
        if (filter.userId() != null) {
            query.append(" AND user_id = ?");
            params.add(filter.userId().toString());
        }

        query.append(" ORDER BY timestamp DESC LIMIT ?");
        params.add(filter.limit());

        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(new LogEntry(
                            rs.getTimestamp("timestamp").toInstant(),
                            rs.getString("server_id"),
                            LogLevel.valueOf(rs.getString("level")),
                            rs.getString("category"),
                            rs.getString("message"),
                            rs.getString("user_id") != null ? UUID.fromString(rs.getString("user_id")) : null,
                            rs.getString("user_name"),
                            rs.getString("metadata")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve logs", e);
        }
        return entries;
    }
    @Override
    public void disable() throws Exception {
        // Cleanup if needed
    }
}
