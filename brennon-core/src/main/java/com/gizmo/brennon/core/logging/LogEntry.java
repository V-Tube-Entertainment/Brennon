package com.gizmo.brennon.core.logging;

import java.time.Instant;
import java.util.UUID;

public record LogEntry(
        Instant timestamp,
        String serverId,
        LogLevel level,
        String category,
        String message,
        UUID userId,
        String userName,
        String metadata
) {}
