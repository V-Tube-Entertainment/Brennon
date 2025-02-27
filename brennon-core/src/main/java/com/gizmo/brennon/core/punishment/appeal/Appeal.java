package com.gizmo.brennon.core.punishment.appeal;

import com.gizmo.brennon.core.punishment.AppealStatus;
import java.time.Instant;
import java.util.UUID;

public record Appeal(
        long id,
        long punishmentId,
        UUID appealerId,
        String appealerName,
        String reason,
        Instant createdAt,
        AppealStatus status,
        UUID handlerId,
        String handlerName,
        String response,
        Instant handledAt
) {}
