package com.gizmo.brennon.core.monitoring;

import com.google.gson.JsonObject;
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
    private void checkAlerts() {
        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            // Check and potentially resolve existing alerts
            checkExistingAlerts(conn);

            // Check for new system-wide alerts
            checkSystemAlerts(conn);

            // Check server-specific alerts
            checkServerAlerts(conn);
        } catch (SQLException e) {
            logger.error("Failed to check alerts", e);
        }
    }

    private void checkExistingAlerts(Connection conn) throws SQLException {
        // Get unresolved alerts
        try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT * FROM monitoring_alerts 
                WHERE resolved_at IS NULL
                ORDER BY created_at DESC
                """)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String serverId = rs.getString("server_id");
                    AlertType type = AlertType.valueOf(rs.getString("alert_type"));
                    long alertId = rs.getLong("id");

                    // Check if the alert condition is still valid
                    if (!isAlertStillValid(conn, serverId, type)) {
                        resolveAlert(conn, alertId);

                        // Notify about resolved alert
                        messageBroker.publish("brennon:alerts", createResolvedAlertMessage(
                                serverId,
                                type,
                                "Alert condition no longer present"
                        ));
                    }
                }
            }
        }
    }

    private boolean isAlertStillValid(Connection conn, String serverId, AlertType type) throws SQLException {
        // Get the most recent metrics for the server
        try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT * FROM server_metrics 
                WHERE server_id = ? 
                ORDER BY timestamp DESC 
                LIMIT 1
                """)) {

            stmt.setString(1, serverId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ServerMetrics metrics = new ServerMetrics(
                            rs.getString("server_id"),
                            rs.getTimestamp("timestamp").toInstant(),
                            rs.getDouble("tps"),
                            rs.getDouble("cpu_usage"),
                            rs.getLong("memory_used"),
                            rs.getLong("memory_max"),
                            rs.getInt("online_players")
                    );

                    // Check if the alert condition is still true
                    return switch (type) {
                        case LOW_TPS -> metrics.tps() < 15.0;
                        case HIGH_MEMORY -> metrics.getMemoryUsagePercent() > 90;
                        case HIGH_CPU -> metrics.cpuUsage() > 80;
                        case PLAYER_SPIKE -> isPlayerSpike(conn, serverId, metrics.onlinePlayers());
                        case CONNECTION_LOST -> isServerOffline(serverId);
                        default -> false;
                    };
                }
            }
        }
        return false;
    }

    private void checkSystemAlerts(Connection conn) throws SQLException {
        // Check for network-wide issues
        int totalOfflineServers = countOfflineServers();
        int totalServers = countTotalServers();

        if (totalOfflineServers > 0 && ((double) totalOfflineServers / totalServers) > 0.25) {
            createAlert("NETWORK", AlertType.CONNECTION_LOST, AlertSeverity.CRITICAL,
                    String.format("%.0f%% of servers are offline (%d/%d)",
                            (double) totalOfflineServers / totalServers * 100,
                            totalOfflineServers, totalServers));
        }

        // Check for database health
        try (PreparedStatement stmt = conn.prepareStatement("SELECT 1")) {
            long start = System.currentTimeMillis();
            stmt.executeQuery();
            long queryTime = System.currentTimeMillis() - start;

            if (queryTime > 1000) { // Alert if query takes more than 1 second
                createAlert("NETWORK", AlertType.CUSTOM, AlertSeverity.WARNING,
                        "Database response time is high: " + queryTime + "ms");
            }
        }
    }

    private void checkServerAlerts(Connection conn) throws SQLException {
        // Get recent metrics for all servers
        try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT m1.* 
                FROM server_metrics m1
                INNER JOIN (
                    SELECT server_id, MAX(timestamp) as max_ts 
                    FROM server_metrics 
                    GROUP BY server_id
                ) m2 ON m1.server_id = m2.server_id AND m1.timestamp = m2.max_ts
                """)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String serverId = rs.getString("server_id");
                    ServerMetrics metrics = new ServerMetrics(
                            serverId,
                            rs.getTimestamp("timestamp").toInstant(),
                            rs.getDouble("tps"),
                            rs.getDouble("cpu_usage"),
                            rs.getLong("memory_used"),
                            rs.getLong("memory_max"),
                            rs.getInt("online_players")
                    );

                    // Check for sustained performance issues
                    checkSustainedPerformanceIssues(conn, serverId, metrics);

                    // Check for unusual player count changes
                    checkPlayerCountAnomaly(conn, serverId, metrics.onlinePlayers());
                }
            }
        }
    }

    private void checkSustainedPerformanceIssues(Connection conn, String serverId, ServerMetrics current) throws SQLException {
        // Check for sustained low TPS (last 5 minutes)
        try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT COUNT(*) as low_tps_count
                FROM server_metrics
                WHERE server_id = ? 
                AND timestamp >= ?
                AND tps < 15.0
                """)) {

            stmt.setString(1, serverId);
            stmt.setTimestamp(2, Timestamp.from(Instant.now().minusSeconds(300)));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt("low_tps_count") >= 4) { // 4 out of 5 minutes
                    createAlert(serverId, AlertType.LOW_TPS, AlertSeverity.HIGH,
                            "Server experiencing sustained low TPS");
                }
            }
        }
    }

    private void checkPlayerCountAnomaly(Connection conn, String serverId, int currentPlayers) throws SQLException {
        // Get average player count for this time of day
        try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT AVG(online_players) as avg_players
                FROM server_metrics
                WHERE server_id = ?
                AND HOUR(timestamp) = HOUR(NOW())
                AND timestamp >= DATE_SUB(NOW(), INTERVAL 7 DAY)
                """)) {

            stmt.setString(1, serverId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double avgPlayers = rs.getDouble("avg_players");
                    if (currentPlayers > avgPlayers * 2) { // 100% increase
                        createAlert(serverId, AlertType.PLAYER_SPIKE, AlertSeverity.WARNING,
                                String.format("Unusual player count increase (Current: %d, Avg: %.1f)",
                                        currentPlayers, avgPlayers));
                    }
                }
            }
        }
    }

    private void resolveAlert(Connection conn, long alertId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("""
                UPDATE monitoring_alerts
                SET resolved_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """)) {
            stmt.setLong(1, alertId);
            stmt.executeUpdate();
        }
    }

    private String createResolvedAlertMessage(String serverId, AlertType type, String message) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "ALERT_RESOLVED");
        json.addProperty("serverId", serverId);
        json.addProperty("alertType", type.name());
        json.addProperty("message", message);
        json.addProperty("timestamp", Instant.now().toString());
        return json.toString();
    }

    private boolean isPlayerSpike(Connection conn, String serverId, int currentPlayers) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT AVG(online_players) as avg_players
                FROM server_metrics
                WHERE server_id = ?
                AND timestamp >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
                """)) {
            stmt.setString(1, serverId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double avgPlayers = rs.getDouble("avg_players");
                    return currentPlayers > avgPlayers * 2;
                }
            }
        }
        return false;
    }

    private boolean isServerOffline(String serverId) {
        // Check if we haven't received metrics from this server in the last minute
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT COUNT(*) as recent_metrics
                FROM server_metrics
                WHERE server_id = ?
                AND timestamp >= DATE_SUB(NOW(), INTERVAL 1 MINUTE)
                """)) {
            stmt.setString(1, serverId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt("recent_metrics") == 0;
            }
        } catch (SQLException e) {
            logger.error("Failed to check server status", e);
            return false;
        }
    }

    private int countOfflineServers() throws SQLException {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT COUNT(DISTINCT server_id) as offline_count
                FROM server_metrics m1
                WHERE NOT EXISTS (
                    SELECT 1 FROM server_metrics m2
                    WHERE m2.server_id = m1.server_id
                    AND m2.timestamp >= DATE_SUB(NOW(), INTERVAL 1 MINUTE)
                )
                """)) {
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("offline_count") : 0;
            }
        }
    }

    private int countTotalServers() throws SQLException {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(DISTINCT server_id) as total FROM server_metrics")) {
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }
}
