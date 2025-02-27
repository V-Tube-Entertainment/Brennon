package com.gizmo.brennon.core.punishment.template;

import com.gizmo.brennon.core.punishment.PunishmentType;

import java.time.Duration;

public record PunishmentTemplate(
        String id,
        String name,
        PunishmentType type,
        String reasonTemplate,
        Duration duration,
        int level,
        boolean requiresEvidence
) {}
