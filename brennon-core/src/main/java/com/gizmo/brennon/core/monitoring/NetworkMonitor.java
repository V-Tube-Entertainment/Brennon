package com.gizmo.brennon.core.monitoring;

import com.google.inject.Inject;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.messaging.MessageBroker;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NetworkMonitor implements Service {
    private final Logger logger;
    private final DatabaseManager databaseManager;
    private final MessageBroker messageBroker;
    private ScheduledExecutorService executor;

    @Inject
    public NetworkMonitor(Logger logger, DatabaseManager databaseManager, MessageBroker messageBroker) {
        this.logger = logger;
        this.databaseManager = databaseManager;
        this.messageBroker = messageBroker;
    }

    @Override
    public void enable() throws Exception {
        initializeDatabase();
        startMonitoring();
    }

    @Override
    public void disable() throws Exception {
        if (executor != null) {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void initializeDatabase() throws SQLException {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {

            // Create metrics table for storing server performance data
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS server_metrics (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    server_id VARCHAR(64) NOT NULL,
                    timestamp TIMESTAMP NOT NULL,
                    tps DOUBLE NOT NULL,
                    cpu_usage DOUBLE NOT NULL,
                    memory_used BIGINT NOT NULL,
                    memory_max BIGINT NOT NULL,
                    online_players INT NOT NULL,
                    INDEX idx_server_time (server_id, timestamp)
                )
            """);

            // Create alerts table for storing monitoring alerts
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS monitoring_alerts (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    server_id VARCHAR(64) NOT NULL,
                    alert_type VARCHAR(32) NOT NULL,
                    severity VARCHAR(16) NOT NULL,
                    message TEXT NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    resolved_at TIMESTAMP,
                    INDEX idx_server (server_id),
                    INDEX idx_unresolved (resolved_at)
                )
            """);
        }
    }

    private void startMonitoring() {
        executor = Executors.newScheduledThreadPool(2);

        // Schedule metrics cleanup task
        executor.scheduleAtFixedRate(this::cleanupOldMetrics, 1, 24, TimeUnit.HOURS);

        // Schedule alert check task
        executor.scheduleAtFixedRate(this::checkAlerts, 1, 1, TimeUnit.MINUTES);
    }

    public void recordMetrics(ServerMetrics metrics) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO server_metrics 
                (server_id, timestamp, tps, cpu_usage, memory_used, memory_max, online_players)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """)) {

            stmt.setString(1, metrics.serverId());
            stmt.setTimestamp(2, Timestamp.from(metrics.timestamp()));
            stmt.setDouble(3, metrics.tps());
            stmt.setDouble(4, metrics.cpuUsage());
            stmt.setLong(5, metrics.memoryUsed());
            stmt.setLong(6, metrics.memoryMax());
            stmt.setInt(7, metrics.onlinePlayers());

            stmt.executeUpdate();
            checkMetricsThresholds(metrics);
        } catch (SQLException e) {
            logger.error("Failed to record server metrics", e);
        }
    }

    private void checkMetricsThresholds(ServerMetrics metrics) {
        // Check TPS
        if (metrics.tps() < 15.0) {
            createAlert(metrics.serverId(), AlertType.LOW_TPS, AlertSeverity.HIGH,
                    String.format("Server TPS dropped to %.2f", metrics.tps()));
        }

        // Check memory usage
        double memoryUsagePercent = (double) metrics.memoryUsed() / metrics.memoryMax() * 100;
        if (memoryUsagePercent > 90) {
            createAlert(metrics.serverId(), AlertType.HIGH_MEMORY, AlertSeverity.WARNING,
                    String.format("High memory usage: %.1f%%", memoryUsagePercent));
        }

        // Check CPU usage
        if (metrics.cpuUsage() > 80) {
            createAlert(metrics.serverId(), AlertType.HIGH_CPU, AlertSeverity.WARNING,
                    String.format("High CPU usage: %.1f%%", metrics.cpuUsage()));
        }
    }

    private void createAlert(String serverId, AlertType type, AlertSeverity severity, String message) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO monitoring_alerts 
                (server_id, alert_type, severity, message, created_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """)) {

            stmt.setString(1, serverId);
            stmt.setString(2, type.name());
            stmt.setString(3, severity.name());
            stmt.setString(4, message);

            stmt.executeUpdate();

            // Notify about new alert
            messageBroker.publish("brennon:alerts", new MonitoringAlert(
                    serverId,
                    type,
                    severity,
                    message,
                    Instant.now()
            ).toJson());
        } catch (SQLException e) {
            logger.error("Failed to create monitoring alert", e);
        }
    }

    private void cleanupOldMetrics() {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM server_metrics WHERE timestamp < ?")) {

            // Keep metrics for 30 days
            stmt.setTimestamp(1, Timestamp.from(Instant.now().minus(30, TimeUnit.DAYS.toChronoUnit())));
            int deleted = stmt.executeUpdate();
            logger.debug("Cleaned up {} old metric records", deleted);
        } catch (SQLException e) {
            logger.error("Failed to cleanup old metrics", e);
        }
    }

    public List<ServerMetrics> getMetrics(String serverId, Instant from, Instant to) {
        List<ServerMetrics> metrics = new ArrayList<>();
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT * FROM server_metrics 
                WHERE server_id = ? AND timestamp BETWEEN ? AND ?
                ORDER BY timestamp DESC
                """)) {

            stmt.setString(1, serverId);
            stmt.setTimestamp(2, Timestamp.from(from));
            stmt.setTimestamp(3, Timestamp.from(to));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    metrics.add(new ServerMetrics(
                            rs.getString("server_id"),
                            rs.getTimestamp("timestamp").toInstant(),
                            rs.getDouble("tps"),
                            rs.getDouble("cpu_usage"),
                            rs.getLong("memory_used"),
                            rs.getLong("memory_max"),
                            rs.getInt("online_players")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve server metrics", e);
        }
        return metrics;
    }
}
