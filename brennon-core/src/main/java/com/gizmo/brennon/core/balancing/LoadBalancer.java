package com.gizmo.brennon.core.balancing;

import com.google.inject.Inject;
import com.gizmo.brennon.core.server.ServerGroup;
import com.gizmo.brennon.core.server.ServerGroupManager;
import com.gizmo.brennon.core.server.ServerInfo;
import com.gizmo.brennon.core.server.ServerManager;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class LoadBalancer implements Service {
    private final Logger logger;
    private final ServerManager serverManager;
    private final ServerGroupManager groupManager;
    private final AtomicBoolean isRunning;
    private final AtomicReference<Thread> balancerThread;

    @Inject
    public LoadBalancer(Logger logger, ServerManager serverManager, ServerGroupManager groupManager) {
        this.logger = logger;
        this.serverManager = serverManager;
        this.groupManager = groupManager;
        this.isRunning = new AtomicBoolean(false);
        this.balancerThread = new AtomicReference<>();
    }

    @Override
    public void enable() throws Exception {
        isRunning.set(true);
        Thread thread = new Thread(this::balancingLoop, "LoadBalancer-Thread");
        balancerThread.set(thread);
        thread.start();
        logger.info("LoadBalancer enabled and started balancing loop");
    }

    @Override
    public void disable() throws Exception {
        isRunning.set(false);
        Thread thread = balancerThread.get();
        if (thread != null) {
            thread.interrupt();
            thread.join(5000); // Wait up to 5 seconds for clean shutdown
        }
        logger.info("LoadBalancer disabled");
    }

    public Optional<ServerInfo> findBestServer(String groupId, UUID playerId) {
        Optional<ServerGroup> groupOpt = groupManager.getGroup(groupId);
        if (groupOpt.isEmpty()) {
            return Optional.empty();
        }

        ServerGroup group = groupOpt.get();
        List<ServerInfo> availableServers = new ArrayList<>();

        for (String serverId : group.serverIds()) {
            serverManager.getServer(serverId).ifPresent(server -> {
                if (server.isOnline() && !server.isFull()) {
                    availableServers.add(server);
                }
            });
        }

        if (availableServers.isEmpty()) {
            return Optional.empty();
        }

        // Sort servers by a score based on various factors
        availableServers.sort((s1, s2) -> {
            double score1 = calculateServerScore(s1);
            double score2 = calculateServerScore(s2);
            return Double.compare(score2, score1); // Higher score is better
        });

        return Optional.of(availableServers.get(0));
    }

    private double calculateServerScore(ServerInfo server) {
        double score = 100.0;

        // Player count factor (prefer servers with more players but not full)
        double playerRatio = (double) server.onlinePlayers() / server.maxPlayers();
        if (playerRatio < 0.2) {
            score -= 20; // Penalize empty servers
        } else if (playerRatio > 0.8) {
            score -= 30; // Penalize nearly full servers
        }

        // Performance factors
        if (server.tps() < 19.0) {
            score -= (19.0 - server.tps()) * 10;
        }

        score -= server.cpuUsage() * 0.5;
        score -= server.memoryUsage() * 0.3;

        return Math.max(0, score);
    }

    private void balancingLoop() {
        while (isRunning.get() && !Thread.currentThread().isInterrupted()) {
            try {
                for (ServerGroup group : groupManager.getAllGroups()) {
                    balanceGroup(group);
                }
                TimeUnit.SECONDS.sleep(30); // Balance every 30 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error in balancing loop", e);
                try {
                    TimeUnit.SECONDS.sleep(5); // Wait before retrying on error
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

        // Calculate total players and capacity
        int totalPlayers = groupServers.stream()
                .mapToInt(ServerInfo::onlinePlayers)
                .sum();
        int totalCapacity = groupServers.stream()
                .mapToInt(ServerInfo::maxPlayers)
                .sum();

        // Check if we need to scale
        if (group.properties().autoScaling()) {
            if (totalPlayers > totalCapacity * 0.8 &&
                    groupServers.size() < group.properties().maxServers()) {
                // Need to scale up
                logger.info("Group {} needs scaling up", group.id());
                handleScaleUp(group);
            } else if (totalPlayers < totalCapacity * 0.3 &&
                    groupServers.size() > group.properties().minServers()) {
                // Can scale down
                logger.info("Group {} can scale down", group.id());
                handleScaleDown(group, groupServers);
            }
        }

        return serverAssignments;
    }

    private void handleScaleUp(ServerGroup group) {
        // Implementation for scaling up servers
        // This would typically involve:
        // 1. Creating a new server instance
        // 2. Registering it with the server manager
        // 3. Adding it to the group
        logger.info("Scaling up group: {}", group.id());
    }

    private void handleScaleDown(ServerGroup group, List<ServerInfo> groupServers) {
        // Implementation for scaling down servers
        // This would typically involve:
        // 1. Identifying the least utilized server
        // 2. Gracefully shutting it down
        // 3. Removing it from the group
        logger.info("Scaling down group: {}", group.id());
    }
}
