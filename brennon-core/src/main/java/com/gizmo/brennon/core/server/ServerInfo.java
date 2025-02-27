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
        double memoryUsage,
        double cpuUsage,
        long lastHeartbeat,
        long uptime,
        Instant createdAt
) {
    public boolean isOnline() {
        return status == ServerStatus.ONLINE;
    }

    public boolean isFull() {
        return onlinePlayers >= maxPlayers;
    }

    public double getMemoryUsagePercent() {
        return memoryUsage;
    }

    public static ServerInfo empty(String id) {
        return new ServerInfo(
                id,
                "Unknown",
                "unknown",
                "none",
                "localhost",
                25565,
                false,
                ServerStatus.OFFLINE,
                Map.of(),
                100,
                0,
                20.0,
                0.0,
                0.0,
                0L,
                0L,
                Instant.now()
        );
    }
}
