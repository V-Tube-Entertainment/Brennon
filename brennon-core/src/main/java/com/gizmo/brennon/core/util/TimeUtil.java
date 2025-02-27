package com.gizmo.brennon.core.util;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([wdhms])");

    public static Duration parseDuration(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        Duration duration = Duration.ZERO;
        Matcher matcher = TIME_PATTERN.matcher(input.toLowerCase());

        while (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            duration = duration.plus(switch (unit) {
                case "w" -> Duration.of(amount * 7, ChronoUnit.DAYS);
                case "d" -> Duration.of(amount, ChronoUnit.DAYS);
                case "h" -> Duration.of(amount, ChronoUnit.HOURS);
                case "m" -> Duration.of(amount, ChronoUnit.MINUTES);
                case "s" -> Duration.of(amount, ChronoUnit.SECONDS);
                default -> Duration.ZERO;
            });
        }

        return duration.isZero() ? null : duration;
    }

    public static String formatDuration(Duration duration) {
        if (duration == null) {
            return "permanent";
        }

        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder builder = new StringBuilder();

        if (days > 0) {
            builder.append(days).append("d ");
        }
        if (hours > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0) {
            builder.append(minutes).append("m ");
        }
        if (seconds > 0 || builder.isEmpty()) {
            builder.append(seconds).append("s");
        }

        return builder.toString().trim();
    }
}
