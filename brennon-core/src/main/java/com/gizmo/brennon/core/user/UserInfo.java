package com.gizmo.brennon.core.user;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UserInfo(
        UUID uuid,
        String username,
        String displayName,
        String currentServer,
        UserStatus status,
        String ipAddress,
        String locale,
        Map<String, String> metadata,
        Map<String, String> properties,
        Instant firstJoin,
        Instant lastSeen
) {
    public static UserInfo createBasic(UUID uuid, String username) {
        Instant now = Instant.now();
        return new UserInfo(
                uuid,
                username,
                username,
                "",
                UserStatus.OFFLINE,
                "",
                "en_US",
                Map.of(),
                Map.of(),
                now,
                now
        );
    }

    public static UserInfo createOnline(
            UUID uuid,
            String username,
            String currentServer,
            String ipAddress,
            Instant firstJoin,
            Instant lastSeen
    ) {
        return new UserInfo(
                uuid,
                username,
                username,
                currentServer,
                UserStatus.ONLINE,
                ipAddress,
                "en_US",
                Map.of(),
                Map.of(),
                firstJoin,
                lastSeen
        );
    }
}
