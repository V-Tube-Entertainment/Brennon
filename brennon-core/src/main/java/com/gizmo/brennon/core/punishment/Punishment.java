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
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean isPermanent() {
        return expiresAt == null;
    }
}
