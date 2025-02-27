package com.gizmo.brennon.core.announcement;

import net.kyori.adventure.text.Component;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record Announcement(
        long id,
        UUID creatorId,
        String creatorName,
        Component message,
        Set<String> targetServers,
        Set<String> targetGroups,
        AnnouncementType type,
        Instant createdAt,
        Instant expiresAt,
        boolean active
) {
    public boolean hasExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
