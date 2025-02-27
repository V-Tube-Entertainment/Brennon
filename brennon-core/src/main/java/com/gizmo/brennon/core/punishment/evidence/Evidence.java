package com.gizmo.brennon.core.punishment.evidence;

import java.time.Instant;
import java.util.UUID;

public record Evidence(
        long id,
        long punishmentId,
        String type,
        String content,
        UUID submitterId,
        String submitterName,
        Instant submittedAt
) {}
