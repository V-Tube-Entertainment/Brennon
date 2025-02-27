package com.gizmo.brennon.core.server;

import java.time.Instant;
import java.util.Map;

public record ServerInfo(
        String id,
        String name,
        String type,
        String group,
        String host,
        int port,
        boolean restricted,
        ServerStatus status,
        Map<String, String> properties,
        int maxPlayers,
        int onlinePlayers,
        double tps,
        double cpuUsage,
        long memoryUsed,
        long memoryMax,
        Instant lastUpdate
) {
    public boolean isOnline() {
        return status == ServerStatus.ONLINE;
    }

    public boolean isFull() {
        return onlinePlayers >= maxPlayers;
    }

    public double getMemoryUsagePercent() {
        return (double) memoryUsed / memoryMax * 100;
    }
}
