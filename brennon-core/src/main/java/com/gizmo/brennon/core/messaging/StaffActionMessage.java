package com.gizmo.brennon.core.messaging;

import java.time.Instant;
import java.util.UUID;

public class StaffActionMessage {
    private final UUID staffId;
    private final String staffName;
    private final UUID targetId;
    private final String targetName;
    private final StaffActionType actionType;
    private final String serverName;
    private final String reason;
    private final Instant timestamp;

    public StaffActionMessage(UUID staffId, String staffName, UUID targetId, String targetName,
                              StaffActionType actionType, String serverName, String reason, Instant timestamp) {
        this.staffId = staffId;
        this.staffName = staffName;
        this.targetId = targetId;
        this.targetName = targetName;
        this.actionType = actionType;
        this.serverName = serverName;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public UUID getStaffId() {
        return staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public StaffActionType getActionType() {
        return actionType;
    }

    public String getServerName() {
        return serverName;
    }

    public String getReason() {
        return reason;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
