package com.gizmo.brennon.core.punishment.analytics;

import com.gizmo.brennon.core.punishment.Punishment;
import com.gizmo.brennon.core.punishment.PunishmentType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PunishmentAnalytics {

    public Map<PunishmentType, Integer> getPunishmentTypeDistribution(List<Punishment> punishments) {
        return punishments.stream()
                .collect(Collectors.groupingBy(
                        Punishment::type,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    public Map<String, Integer> getIssuerDistribution(List<Punishment> punishments) {
        return punishments.stream()
                .collect(Collectors.groupingBy(
                        Punishment::issuerName,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    public Map<UUID, Integer> getTargetDistribution(List<Punishment> punishments) {
        return punishments.stream()
                .collect(Collectors.groupingBy(
                        Punishment::targetId,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    public long getActivePunishmentCount(List<Punishment> punishments) {
        return punishments.stream()
                .filter(Punishment::active)
                .count();
    }

    public Map<PunishmentType, Long> getAverageDuration(List<Punishment> punishments) {
        return punishments.stream()
                .filter(p -> p.expiresAt() != null)
                .collect(Collectors.groupingBy(
                        Punishment::type,
                        Collectors.averagingLong(p ->
                                p.expiresAt().getEpochSecond() - p.createdAt().getEpochSecond())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Math.round(e.getValue())
                ));
    }
}