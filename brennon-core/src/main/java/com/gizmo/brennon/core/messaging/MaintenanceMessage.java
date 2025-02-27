package com.gizmo.brennon.core.messaging;

import java.time.Instant;

public class MaintenanceMessage {
    private final String message;
    private final boolean enabling;
    private final String serverName;
    private final Instant timestamp;

    public MaintenanceMessage(String message, boolean enabling, String serverName, Instant timestamp) {
        this.message = message;
        this.enabling = enabling;
        this.serverName = serverName;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public boolean isEnabling() {
        return enabling;
    }

    public String getServerName() {
        return serverName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
