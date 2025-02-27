package com.gizmo.brennon.core.monitoring;

import java.time.Instant;

public record ServerMetrics(
        String serverId,
        Instant timestamp,
        double tps,
        double cpuUsage,
        long memoryUsed,
        long memoryMax,
        int onlinePlayers
) {
    public double getMemoryUsagePercent() {
        return (double) memoryUsed / memoryMax * 100;
    }
}
