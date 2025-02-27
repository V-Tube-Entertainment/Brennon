package com.gizmo.brennon.core.punishment.evidence;

import java.time.Instant;
import java.util.UUID;

public record Evidence(
        long id,
        long punishmentId,
        EvidenceType type,
        String content,
        UUID submittedBy,
        Instant submittedAt
) {}
