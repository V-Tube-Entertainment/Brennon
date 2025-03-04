package com.gizmo.brennon.core.balancing;

import com.google.inject.Inject;
import com.gizmo.brennon.core.server.ServerGroup;
import com.gizmo.brennon.core.server.ServerGroupManager;
import com.gizmo.brennon.core.server.ServerInfo;
import com.gizmo.brennon.core.server.ServerManager;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Enhanced load balancer with advanced server monitoring and balancing capabilities
 *
 * @author Gizmo0320
 * @since 2025-03-04 00:41:50
 */
public class LoadBalancer implements Service {
    private final Logger logger;
    private final ServerManager serverManager;
    private final ServerGroupManager groupManager;
    private final AtomicBoolean isRunning;
    private final AtomicReference<Thread> balancerThread;
    private final Map<String, ServerHealthStats> serverHealth;

    // Balancing configuration
    private double tpsWeight = 0.35;
    private double playerWeight = 0.25;
    private double memoryWeight = 0.20;
    private double cpuWeight = 0.20;
    private double highLoadThreshold = 0.80;
    private double criticalLoadThreshold = 0.90;

    @Inject
    public LoadBalancer(Logger logger, ServerManager serverManager, ServerGroupManager groupManager) {
        this.logger = logger;
        this.serverManager = serverManager;
        this.groupManager = groupManager;
        this.isRunning = new AtomicBoolean(false);
        this.balancerThread = new AtomicReference<>();
        this.serverHealth = new ConcurrentHashMap<>();
    }

    @Override
    public void enable() throws Exception {
        if (isRunning.compareAndSet(false, true)) {
            Thread thread = new Thread(this::balancingLoop, "LoadBalancer-Thread");
            balancerThread.set(thread);
            thread.start();
            logger.info("LoadBalancer enabled and started balancing loop");
        }
    }

    @Override
    public void disable() throws Exception {
        if (isRunning.compareAndSet(true, false)) {
            Thread thread = balancerThread.get();
            if (thread != null) {
                thread.interrupt();
                thread.join(5000); // Wait up to 5 seconds for clean shutdown
            }
            serverHealth.clear();
            logger.info("LoadBalancer disabled");
        }
    }

    public Optional<ServerInfo> findBestServer(String groupId, UUID playerId) {
        if (!isRunning.get()) {
            return Optional.empty();
        }

        Optional<ServerGroup> groupOpt = groupManager.getGroup(groupId);
        if (groupOpt.isEmpty()) {
            return Optional.empty();
        }

        ServerGroup group = groupOpt.get();
        List<ServerInfo> availableServers = new ArrayList<>();

        for (String serverId : group.serverIds()) {
            serverManager.getServer(serverId).ifPresent(server -> {
                if (isServerHealthy(server)) {
                    availableServers.add(server);
                }
            });
        }

        if (availableServers.isEmpty()) {
            return Optional.empty();
        }

        // Sort servers by comprehensive score
        availableServers.sort((s1, s2) ->
                Double.compare(calculateServerScore(s2), calculateServerScore(s1)));

        return Optional.of(availableServers.get(0));
    }

    private boolean isServerHealthy(ServerInfo server) {
        ServerHealthStats health = serverHealth.get(server.id());
        if (health == null) {
            return server.isOnline() && !server.isFull();
        }

        return server.isOnline() &&
                !server.isFull() &&
                server.tps() >= 17.0 &&
                health.getLoadFactor() < criticalLoadThreshold;
    }

    private double calculateServerScore(ServerInfo server) {
        double score = 100.0;
        ServerHealthStats health = serverHealth.computeIfAbsent(
                server.id(),
                k -> new ServerHealthStats()
        );

        // TPS factor (0-20)
        double tpsFactor = server.tps() / 20.0;

        // Player distribution factor (prefer servers with some players but not full)
        double playerRatio = (double) server.onlinePlayers() / server.maxPlayers();
        double playerFactor = 1.0 - Math.abs(0.5 - playerRatio);

        // Resource usage factors
        double memoryFactor = 1.0 - server.memoryUsage();
        double cpuFactor = 1.0 - server.cpuUsage();

        // Calculate weighted score
        score *= (tpsFactor * tpsWeight) +
                (playerFactor * playerWeight) +
                (memoryFactor * memoryWeight) +
                (cpuFactor * cpuWeight);

        // Update health stats
        health.updateStats(
                server.tps(),
                server.onlinePlayers(),
                server.memoryUsage(),
                server.cpuUsage()
        );

        return score;
    }

    private void balancingLoop() {
        while (isRunning.get() && !Thread.currentThread().isInterrupted()) {
            try {
                for (ServerGroup group : groupManager.getAllGroups()) {
                    if (group.properties().autoScaling()) {
                        balanceGroup(group);
                    }
                }
                TimeUnit.SECONDS.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error in balancing loop", e);
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public Map<String, ServerInfo> balanceGroup(ServerGroup group) {
        Map<String, ServerInfo> serverAssignments = new HashMap<>();
        List<ServerInfo> groupServers = new ArrayList<>();

        // Collect online servers in group
        for (String serverId : group.serverIds()) {
            serverManager.getServer(serverId).ifPresent(server -> {
                if (server.isOnline()) {
                    groupServers.add(server);
                }
            });
        }

        if (groupServers.isEmpty()) {
            return serverAssignments;
        }

        // Calculate metrics for scaling decisions
        int totalPlayers = groupServers.stream()
                .mapToInt(ServerInfo::onlinePlayers)
                .sum();
        int totalCapacity = groupServers.stream()
                .mapToInt(ServerInfo::maxPlayers)
                .sum();
        double averageLoad = groupServers.stream()
                .mapToDouble(this::calculateServerScore)
                .average()
                .orElse(0.0);

        // Handle scaling based on load and player count
        if (totalPlayers > totalCapacity * highLoadThreshold &&
                groupServers.size() < group.properties().maxServers()) {
            handleScaleUp(group);
        } else if (totalPlayers < totalCapacity * 0.3 &&
                groupServers.size() > group.properties().minServers() &&
                averageLoad < 0.4) {
            handleScaleDown(group, groupServers);
        }

        return serverAssignments;
    }

    private void handleScaleUp(ServerGroup group) {
        logger.info("Initiating scale up for group: {}", group.id());
        // Implementation would be provided by the platform-specific code
    }

    private void handleScaleDown(ServerGroup group, List<ServerInfo> groupServers) {
        logger.info("Initiating scale down for group: {}", group.id());
        // Implementation would be provided by the platform-specific code
    }

    // Health tracking inner class
    private static class ServerHealthStats {
        private double tps;
        private int playerCount;
        private double memoryUsage;
        private double cpuUsage;
        private double loadFactor;
        private long lastUpdate;

        public ServerHealthStats() {
            this.tps = 20.0;
            this.playerCount = 0;
            this.memoryUsage = 0.0;
            this.cpuUsage = 0.0;
            this.loadFactor = 0.0;
            this.lastUpdate = System.currentTimeMillis();
        }

        public void updateStats(double tps, int playerCount, double memoryUsage, double cpuUsage) {
            this.tps = tps;
            this.playerCount = playerCount;
            this.memoryUsage = memoryUsage;
            this.cpuUsage = cpuUsage;
            this.loadFactor = (cpuUsage * 0.4) + (memoryUsage * 0.3) +
                    ((20.0 - tps) / 20.0 * 0.3);
            this.lastUpdate = System.currentTimeMillis();
        }

        public double getLoadFactor() {
            return loadFactor;
        }
    }
}