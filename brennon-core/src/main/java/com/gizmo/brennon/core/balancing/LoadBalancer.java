package com.gizmo.brennon.core.balancing;

import com.gizmo.brennon.core.BrennonCore;
import com.gizmo.brennon.core.server.ServerManager;
import com.gizmo.brennon.core.server.ServerInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Advanced load balancer for server distribution
 *
 * @author Gizmo0320
 * @since 2025-03-04 00:30:27
 */
public class LoadBalancer {
    private final BrennonCore core;
    private final ServerManager serverManager;
    private final Logger logger;
    private final Map<String, ServerLoadStats> serverStats;

    // Configuration
    private double tpsWeight = 0.4;
    private double playerCountWeight = 0.3;
    private double memoryWeight = 0.2;
    private double responseTimeWeight = 0.1;
    private int targetPlayersPerServer = 50;
    private double highLoadThreshold = 0.75;
    private double criticalLoadThreshold = 0.90;

    public LoadBalancer(BrennonCore core) {
        this.core = core;
        this.serverManager = core.getServerManager();
        this.logger = core.getLogger();
        this.serverStats = new ConcurrentHashMap<>();
    }

    /**
     * Represents detailed server load statistics
     */
    public static class ServerLoadStats {
        private double tps;
        private int playerCount;
        private double memoryUsage;
        private long responseTime;
        private double loadFactor;
        private long lastUpdate;
        private final Queue<Double> loadHistory;
        private static final int HISTORY_SIZE = 10;

        public ServerLoadStats() {
            this.tps = 20.0;
            this.playerCount = 0;
            this.memoryUsage = 0.0;
            this.responseTime = 0;
            this.loadFactor = 0.0;
            this.lastUpdate = System.currentTimeMillis();
            this.loadHistory = new LinkedList<>();
        }

        public void update(double tps, int playerCount, double memoryUsage, long responseTime) {
            this.tps = tps;
            this.playerCount = playerCount;
            this.memoryUsage = memoryUsage;
            this.responseTime = responseTime;
            this.lastUpdate = System.currentTimeMillis();
        }

        public void updateLoadFactor(double loadFactor) {
            this.loadFactor = loadFactor;
            loadHistory.offer(loadFactor);
            while (loadHistory.size() > HISTORY_SIZE) {
                loadHistory.poll();
            }
        }

        public double getAverageLoad() {
            return loadHistory.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(loadFactor);
        }

        // Getters
        public double getTps() { return tps; }
        public int getPlayerCount() { return playerCount; }
        public double getMemoryUsage() { return memoryUsage; }
        public long getResponseTime() { return responseTime; }
        public double getLoadFactor() { return loadFactor; }
        public long getLastUpdate() { return lastUpdate; }
    }

    /**
     * Finds the best server in a group based on current load and specific requirements
     */
    public Optional<ServerInfo> findBestServer(String groupId, ServerRequirements requirements) {
        try {
            List<ServerInfo> groupServers = new ArrayList<>(serverManager.getServersByGroup(groupId));
            if (groupServers.isEmpty()) {
                return Optional.empty();
            }

            // Filter servers based on requirements
            if (requirements != null) {
                groupServers.removeIf(server -> !meetsRequirements(server, requirements));
            }

            // Filter out overloaded or unhealthy servers
            groupServers.removeIf(server -> {
                ServerLoadStats stats = serverStats.get(server.id());
                return stats != null &&
                        (stats.getLoadFactor() > criticalLoadThreshold ||
                                !isServerHealthy(server));
            });

            if (groupServers.isEmpty()) {
                logger.warning("No suitable servers found in group: " + groupId);
                return Optional.empty();
            }

            // Find server with lowest load
            return groupServers.stream()
                    .min(this::compareServerLoad);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding best server in group: " + groupId, e);
            return Optional.empty();
        }
    }

    /**
     * Calculates the current load factor for a server
     */
    public double calculateServerLoad(String serverId) {
        ServerInfo server = serverManager.getServer(serverId).orElse(null);
        if (server == null) return 1.0;

        ServerLoadStats stats = serverStats.computeIfAbsent(serverId, k -> new ServerLoadStats());

        // Calculate individual factors
        double tpsFactor = Math.max(0, (20.0 - stats.getTps()) / 20.0);
        double playerFactor = (double) stats.getPlayerCount() / targetPlayersPerServer;
        double memoryFactor = stats.getMemoryUsage();
        double responseFactor = Math.min(1.0, stats.getResponseTime() / 1000.0); // Scale to 1 second

        // Calculate weighted load
        double loadFactor = (tpsFactor * tpsWeight) +
                (playerFactor * playerCountWeight) +
                (memoryFactor * memoryWeight) +
                (responseFactor * responseTimeWeight);

        // Update stats
        stats.updateLoadFactor(loadFactor);

        return loadFactor;
    }

    /**
     * Updates server statistics
     */
    public void updateServerStats(String serverId, double tps, int playerCount,
                                  double memoryUsage, long responseTime) {
        serverStats.computeIfAbsent(serverId, k -> new ServerLoadStats())
                .update(tps, playerCount, memoryUsage, responseTime);

        // Check for high load
        double load = calculateServerLoad(serverId);
        if (load > highLoadThreshold) {
            logger.warning("Server " + serverId + " is under high load: " +
                    String.format("%.2f", load * 100) + "%");
        }
    }

    private boolean meetsRequirements(ServerInfo server, ServerRequirements requirements) {
        ServerLoadStats stats = serverStats.get(server.id());
        if (stats == null) return false;

        return (!requirements.hasMinTps() || stats.getTps() >= requirements.getMinTps()) &&
                (!requirements.hasMaxPlayers() || stats.getPlayerCount() < requirements.getMaxPlayers()) &&
                (!requirements.hasMaxMemory() || stats.getMemoryUsage() < requirements.getMaxMemory()) &&
                (!requirements.hasMaxResponseTime() || stats.getResponseTime() < requirements.getMaxResponseTime());
    }

    private boolean isServerHealthy(ServerInfo server) {
        ServerLoadStats stats = serverStats.get(server.id());
        if (stats == null) return false;

        return stats.getTps() >= 15.0 && // Minimum acceptable TPS
                stats.getResponseTime() < 5000 && // Max 5 second response time
                System.currentTimeMillis() - stats.getLastUpdate() < 60000; // Last update within 60 seconds
    }

    private int compareServerLoad(ServerInfo server1, ServerInfo server2) {
        double load1 = serverStats.get(server1.id()).getAverageLoad();
        double load2 = serverStats.get(server2.id()).getAverageLoad();
        return Double.compare(load1, load2);
    }

    // Configuration methods
    public void setWeights(double tps, double players, double memory, double response) {
        double total = tps + players + memory + response;
        this.tpsWeight = tps / total;
        this.playerCountWeight = players / total;
        this.memoryWeight = memory / total;
        this.responseTimeWeight = response / total;
    }

    public void setTargetPlayersPerServer(int count) {
        this.targetPlayersPerServer = count;
    }

    public void setLoadThresholds(double high, double critical) {
        this.highLoadThreshold = high;
        this.criticalLoadThreshold = critical;
    }

    /**
     * Gets current load statistics for a server
     */
    public Optional<ServerLoadStats> getServerStats(String serverId) {
        return Optional.ofNullable(serverStats.get(serverId));
    }

    /**
     * Gets all server statistics
     */
    public Map<String, ServerLoadStats> getAllServerStats() {
        return Collections.unmodifiableMap(serverStats);
    }
}