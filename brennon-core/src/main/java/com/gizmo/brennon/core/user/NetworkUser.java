package com.gizmo.brennon.core.user;

import java.time.Instant;
import java.util.UUID;

public record NetworkUser(
        UUID uuid,
        String username,
        String currentServer,
        Instant firstJoin,
        Instant lastJoin,
        Instant lastSeen,
        String lastIpAddress,
        UserSettings settings
) {
    public boolean isOnline() {
        return currentServer != null;
    }

    public NetworkUser withServer(String server) {
        return new NetworkUser(
                uuid,
                username,
                server,
                firstJoin,
                lastJoin,
                lastSeen,
                lastIpAddress,
                settings
        );
    }
}
