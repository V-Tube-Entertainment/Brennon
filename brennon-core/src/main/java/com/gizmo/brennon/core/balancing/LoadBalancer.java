package com.gizmo.brennon.core.balancing;

import com.google.inject.Inject;
import com.gizmo.brennon.core.server.ServerGroup;
import com.gizmo.brennon.core.server.ServerGroupManager;
import com.gizmo.brennon.core.server.ServerInfo;
import com.gizmo.brennon.core.server.ServerManager;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;

import java.util.*;

public class LoadBalancer implements Service {
    private final Logger logger;
    private final ServerManager serverManager;
    private final ServerGroupManager groupManager;

    @Inject
    public LoadBalancer(Logger logger, ServerManager serverManager, ServerGroupManager groupManager) {
        this.logger = logger;
        this.serverManager = serverManager;
        this.groupManager = groupManager;
    }

    public Optional<ServerInfo> findBestServer(String groupId, UUID playerId) {
        Optional<ServerGroup> groupOpt = groupManager.getGroup(groupId);
        if (groupOpt.isEmpty()) {
            return Optional.empty();
        }

        ServerGroup group = groupOpt.get();
        List<ServerInfo> availableServers = new ArrayList<>();

        for (String serverId : group.serverIds()) {
            ServerInfo server = serverManager.getServer(serverId);
            if (server != null && server.isOnline() && !server.isFull()) {
                availableServers.add(server);
            }
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
        score -= (server.getMemoryUsagePercent() * 0.3);

        return Math.max(0, score);
    }

    public Map<String, ServerInfo> balanceGroup(ServerGroup group) {
        Map<String, ServerInfo> serverAssignments = new HashMap<>();
        List<ServerInfo> groupServers = new ArrayList<>();

        for (String serverId : group.serverIds()) {
            ServerInfo server = serverManager.getServer(serverId);
            if (server != null && server.isOnline()) {
                groupServers.add(server);
            }
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
                // Trigger scale up logic here
            } else if (totalPlayers < totalCapacity * 0.3 &&
                    groupServers.size() > group.properties().minServers()) {
                // Can scale down
                logger.info("Group {} can scale down", group.id());
                // Trigger scale down logic here
            }
        }

        return serverAssignments;
    }
    @Override
    public void disable() throws Exception {
        // Cleanup if needed
    }
}
