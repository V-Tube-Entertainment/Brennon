package com.gizmo.brennon.core.punishment;

import com.gizmo.brennon.core.punishment.appeal.AppealStatus;

import java.time.Instant;
import java.util.UUID;

public record PunishmentAppeal(
        long id,
        long punishmentId,
        UUID appealerId,
        String appealer,
        String reason,
        AppealStatus status,
        Instant createdAt,
        Instant resolvedAt,
        String resolvedBy,
        String resolution
) {}
