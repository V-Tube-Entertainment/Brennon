package com.gizmo.brennon.core.punishment;

import java.time.Instant;
import java.util.UUID;

public record IPPunishment(
        long id,
        String ipAddress,
        PunishmentType type,
        String reason,
        UUID issuerId,
        String issuerName,
        Instant createdAt,
        Instant expiresAt,
        boolean active
) {
    public boolean isPermanent() {
        return expiresAt == null;
    }

    public boolean isExpired() {
        return !isPermanent() && Instant.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return active && !isExpired();
    }
}
