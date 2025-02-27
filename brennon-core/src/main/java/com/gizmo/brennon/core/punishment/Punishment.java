package com.gizmo.brennon.core.punishment;

import java.time.Instant;
import java.util.UUID;

public record Punishment(
        long id,
        UUID targetId,
        String targetName,
        UUID issuerId,
        String issuerName,
        PunishmentType type,
        String reason,
        Instant createdAt,
        Instant expiresAt,
        boolean active
) {
    public boolean isPermanent() {
        return expiresAt == null;
    }

    public boolean hasExpired() {
        return !isPermanent() && Instant.now().isAfter(expiresAt);
    }
}
