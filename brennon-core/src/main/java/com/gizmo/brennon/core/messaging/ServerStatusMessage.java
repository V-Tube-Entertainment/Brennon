package com.gizmo.brennon.core.messaging;

import java.time.Instant;

public class ServerStatusMessage {
    private final String serverName;
    private final ServerStatus status;
    private final int playerCount;
    private final int maxPlayers;
    private final double tps;
    private final double ramUsage;
    private final Instant timestamp;

    public ServerStatusMessage(String serverName, ServerStatus status, int playerCount,
                               int maxPlayers, double tps, double ramUsage, Instant timestamp) {
        this.serverName = serverName;
        this.status = status;
        this.playerCount = playerCount;
        this.maxPlayers = maxPlayers;
        this.tps = tps;
        this.ramUsage = ramUsage;
        this.timestamp = timestamp;
    }

    public String getServerName() {
        return serverName;
    }

    public ServerStatus getStatus() {
        return status;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public double getTps() {
        return tps;
    }

    public double getRamUsage() {
        return ramUsage;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
