package com.gizmo.brennon.core.messaging;

import java.time.Instant;
import java.util.UUID;

public class PlayerUpdateMessage {
    private final UUID playerId;
    private final String username;
    private final PlayerUpdateType type;
    private final String sourceServer;
    private final String targetServer;
    private final Instant timestamp;

    public PlayerUpdateMessage(UUID playerId, String username, PlayerUpdateType type,
                               String sourceServer, String targetServer, Instant timestamp) {
        this.playerId = playerId;
        this.username = username;
        this.type = type;
        this.sourceServer = sourceServer;
        this.targetServer = targetServer;
        this.timestamp = timestamp;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getUsername() {
        return username;
    }

    public PlayerUpdateType getType() {
        return type;
    }

    public String getSourceServer() {
        return sourceServer;
    }

    public String getTargetServer() {
        return targetServer;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
