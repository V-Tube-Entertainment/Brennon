package com.gizmo.brennon.core.user;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UserInfo(
        UUID uuid,
        String username,
        String currentServer,
        UserStatus status,
        String ipAddress,
        Map<String, String> properties,
        Instant firstSeen,
        Instant lastSeen
) {}
