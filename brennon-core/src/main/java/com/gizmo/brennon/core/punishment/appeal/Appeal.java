package com.gizmo.brennon.core.punishment.appeal;

import java.time.Instant;
import java.util.UUID;

public record Appeal(
        long id,
        long punishmentId,
        UUID appealerId,
        String reason,
        AppealStatus status,
        UUID handlerId,
        String handlerName,
        String response,
        Instant createdAt,
        Instant handledAt
) {
    public static Appeal create(long punishmentId, UUID appealerId, String reason) {
        return new Appeal(
                0,
                punishmentId,
                appealerId,
                reason,
                AppealStatus.PENDING,
                null,
                null,
                null,
                Instant.now(),
                null
        );
    }

    public Appeal withHandled(UUID handlerId, String handlerName, String response, AppealStatus status) {
        return new Appeal(
                id,
                punishmentId,
                appealerId,
                reason,
                status,
                handlerId,
                handlerName,
                response,
                createdAt,
                Instant.now()
        );
    }
}
