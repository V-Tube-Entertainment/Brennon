package com.gizmo.brennon.core.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeFormatter {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.of("UTC"));

    public static String format(Instant instant) {
        if (instant == null) {
            return "never";
        }
        return DATE_TIME_FORMATTER.format(instant);
    }

    public static String formatRelative(Instant instant) {
        if (instant == null) {
            return "never";
        }

        long secondsUntil = instant.getEpochSecond() - Instant.now().getEpochSecond();

        if (secondsUntil < 0) {
            return "expired";
        }

        long days = secondsUntil / 86400;
        secondsUntil %= 86400;
        long hours = secondsUntil / 3600;
        secondsUntil %= 3600;
        long minutes = secondsUntil / 60;
        long seconds = secondsUntil % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }
}
