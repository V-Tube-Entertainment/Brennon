package com.gizmo.brennon.core.punishment;

import java.time.Instant;
import java.util.UUID;

public record PunishmentNote(
        long id,
        long punishmentId,
        UUID staffId,
        String staffName,
        String note,
        Instant createdAt
) {}
