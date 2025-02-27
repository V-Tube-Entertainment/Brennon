package com.gizmo.brennon.core.messaging;

import java.time.Instant;
import java.util.UUID;

public class ChatMessage {
    private final UUID sender;
    private final String message;
    private final String serverName;
    private final Instant timestamp;

    public ChatMessage(UUID sender, String message, String serverName, Instant timestamp) {
        this.sender = sender;
        this.message = message;
        this.serverName = serverName;
        this.timestamp = timestamp;
    }

    public UUID getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getServerName() {
        return serverName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
