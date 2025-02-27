package com.gizmo.brennon.core.messaging;

import java.time.Instant;

public class AlertMessage {
    private final String message;
    private final AlertType type;
    private final String serverName;
    private final Instant timestamp;

    public AlertMessage(String message, AlertType type, String serverName, Instant timestamp) {
        this.message = message;
        this.type = type;
        this.serverName = serverName;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public AlertType getType() {
        return type;
    }

    public String getServerName() {
        return serverName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
