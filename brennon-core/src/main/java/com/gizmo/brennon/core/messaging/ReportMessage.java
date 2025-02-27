package com.gizmo.brennon.core.messaging;

import java.time.Instant;
import java.util.UUID;

public class ReportMessage {
    private final UUID reporterId;
    private final String reporterName;
    private final UUID targetId;
    private final String targetName;
    private final String reason;
    private final String serverName;
    private final Instant timestamp;

    public ReportMessage(UUID reporterId, String reporterName, UUID targetId,
                         String targetName, String reason, String serverName, Instant timestamp) {
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.targetId = targetId;
        this.targetName = targetName;
        this.reason = reason;
        this.serverName = serverName;
        this.timestamp = timestamp;
    }

    public UUID getReporterId() {
        return reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getReason() {
        return reason;
    }

    public String getServerName() {
        return serverName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}