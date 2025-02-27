package com.gizmo.brennon.core.punishment.command;

import com.gizmo.brennon.core.command.Command;
import com.gizmo.brennon.core.command.CommandContext;
import com.gizmo.brennon.core.punishment.Punishment;
import com.gizmo.brennon.core.punishment.PunishmentService;
import com.gizmo.brennon.core.punishment.PunishmentType;
import com.gizmo.brennon.core.util.TimeFormatter;
import com.google.inject.Inject;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PunishmentCommands {
    private final PunishmentService punishmentService;

    @Inject
    public PunishmentCommands(PunishmentService punishmentService) {
        this.punishmentService = punishmentService;
    }

    @Command(name = "ban", permission = "brennon.punish.ban")
    public void ban(CommandContext context) {
        if (context.args().length < 2) {
            context.sender().sendMessage("§c/ban <player> <reason>");
            return;
        }

        String targetName = context.args()[0];
        String reason = String.join(" ", context.args(), 1, context.args().length);

        // TODO: Replace with player lookup service
        UUID targetId = context.getUUIDFromName(targetName);
        if (targetId == null) {
            context.sender().sendMessage("§cPlayer not found.");
            return;
        }

        Punishment punishment = punishmentService.createPunishment(
                targetId,
                targetName,
                context.sender().getUUID(),
                context.sender().getName(),
                PunishmentType.BAN,
                reason,
                null // permanent ban
        );

        if (punishment != null) {
            context.sender().sendMessage("§aSuccessfully banned " + targetName + " for: " + reason);
        } else {
            context.sender().sendMessage("§cFailed to ban player.");
        }
    }

    @Command(name = "tempban", permission = "brennon.punish.tempban")
    public void tempban(CommandContext context) {
        if (context.args().length < 3) {
            context.sender().sendMessage("§c/tempban <player> <duration> <reason>");
            return;
        }

        String targetName = context.args()[0];
        Duration duration = parseDuration(context.args()[1]);
        if (duration == null) {
            context.sender().sendMessage("§cInvalid duration format. Use 1d, 2h, 30m, etc.");
            return;
        }

        String reason = String.join(" ", context.args(), 2, context.args().length);

        // TODO: Replace with player lookup service
        UUID targetId = context.getUUIDFromName(targetName);
        if (targetId == null) {
            context.sender().sendMessage("§cPlayer not found.");
            return;
        }

        Punishment punishment = punishmentService.createPunishment(
                targetId,
                targetName,
                context.sender().getUUID(),
                context.sender().getName(),
                PunishmentType.TEMP_BAN,
                reason,
                Instant.now().plus(duration)
        );

        if (punishment != null) {
            context.sender().sendMessage("§aSuccessfully temp-banned " + targetName + " for " + formatDuration(duration));
        } else {
            context.sender().sendMessage("§cFailed to ban player.");
        }
    }

    @Command(name = "unban", permission = "brennon.punish.unban")
    public void unban(CommandContext context) {
        if (context.args().length < 1) {
            context.sender().sendMessage("§c/unban <player>");
            return;
        }

        String targetName = context.args()[0];
        UUID targetId = context.getUUIDFromName(targetName);
        if (targetId == null) {
            context.sender().sendMessage("§cPlayer not found.");
            return;
        }

        List<Punishment> punishments = punishmentService.getActivePunishments(targetId);
        punishments.stream()
                .filter(p -> p.type() == PunishmentType.BAN || p.type() == PunishmentType.TEMP_BAN)
                .forEach(p -> punishmentService.revokePunishment(p.id(), context.sender().getUUID()));

        context.sender().sendMessage("§aUnbanned " + targetName);
    }

    @Command(name = "history", permission = "brennon.punish.history")
    public void history(CommandContext context) {
        if (context.args().length < 1) {
            context.sender().sendMessage("§c/history <player> [page]");
            return;
        }

        String targetName = context.args()[0];
        int page = context.args().length > 1 ? Integer.parseInt(context.args()[1]) : 1;
        int limit = 10;
        int offset = (page - 1) * limit;

        UUID targetId = context.getUUIDFromName(targetName);
        if (targetId == null) {
            context.sender().sendMessage("§cPlayer not found.");
            return;
        }

        List<Punishment> history = punishmentService.getPunishmentHistory(targetId, limit, offset);
        if (history.isEmpty()) {
            context.sender().sendMessage("§eNo punishment history found for " + targetName);
            return;
        }

        context.sender().sendMessage("§6Punishment history for " + targetName + " (Page " + page + "):");
        for (Punishment p : history) {
            String status = p.active() ? "§cActive" : "§7Inactive";
            String expires = p.expiresAt() != null ?
                    "expires " + formatInstant(p.expiresAt()) :
                    "permanent";
            context.sender().sendMessage(String.format(
                    "§7#%d §f%s §7by §f%s §7- §f%s §7(%s) %s",
                    p.id(), p.type().getDisplayName(), p.issuerName(),
                    p.reason(), expires, status
            ));
        }
    }

    private Duration parseDuration(String input) {
        try {
            char unit = input.charAt(input.length() - 1);
            long amount = Long.parseLong(input.substring(0, input.length() - 1));

            return switch (unit) {
                case 'd' -> Duration.ofDays(amount);
                case 'h' -> Duration.ofHours(amount);
                case 'm' -> Duration.ofMinutes(amount);
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m");

        return sb.toString().trim();
    }

    private String formatInstant(Instant instant) {
        return TimeFormatter.format(instant);
    }
    @Command(name = "mute", permission = "brennon.punish.mute")
    public void mute(CommandContext context) {
        if (context.args().length < 2) {
            context.sender().sendMessage("§c/mute <player> <reason>");
            return;
        }

        String targetName = context.args()[0];
        String reason = String.join(" ", context.args(), 1, context.args().length);

        UUID targetId = context.getUUIDFromName(targetName);
        if (targetId == null) {
            context.sender().sendMessage("§cPlayer not found.");
            return;
        }

        Punishment punishment = punishmentService.createPunishment(
                targetId,
                targetName,
                context.sender().getUUID(),
                context.sender().getName(),
                PunishmentType.MUTE,
                reason,
                null // permanent mute
        );

        if (punishment != null) {
            context.sender().sendMessage("§aSuccessfully muted " + targetName + " for: " + reason);
        } else {
            context.sender().sendMessage("§cFailed to mute player.");
        }
    }

    @Command(name = "tempmute", permission = "brennon.punish.tempmute")
    public void tempmute(CommandContext context) {
        if (context.args().length < 3) {
            context.sender().sendMessage("§c/tempmute <player> <duration> <reason>");
            return;
        }

        String targetName = context.args()[0];
        Duration duration = parseDuration(context.args()[1]);
        if (duration == null) {
            context.sender().sendMessage("§cInvalid duration format. Use 1d, 2h, 30m, etc.");
            return;
        }

        String reason = String.join(" ", context.args(), 2, context.args().length);

        UUID targetId = context.getUUIDFromName(targetName);
        if (targetId == null) {
            context.sender().sendMessage("§cPlayer not found.");
            return;
        }

        Punishment punishment = punishmentService.createPunishment(
                targetId,
                targetName,
                context.sender().getUUID(),
                context.sender().getName(),
                PunishmentType.TEMP_MUTE,
                reason,
                Instant.now().plus(duration)
        );

        if (punishment != null) {
            context.sender().sendMessage("§aSuccessfully temp-muted " + targetName + " for " + formatDuration(duration));
        } else {
            context.sender().sendMessage("§cFailed to mute player.");
        }
    }

    @Command(name = "unmute", permission = "brennon.punish.unmute")
    public void unmute(CommandContext context) {
        if (context.args().length < 1) {
            context.sender().sendMessage("§c/unmute <player>");
            return;
        }

        String targetName = context.args()[0];
        UUID targetId = context.getUUIDFromName(targetName);
        if (targetId == null) {
            context.sender().sendMessage("§cPlayer not found.");
            return;
        }

        List<Punishment> punishments = punishmentService.getActivePunishments(targetId);
        punishments.stream()
                .filter(p -> p.type() == PunishmentType.MUTE || p.type() == PunishmentType.TEMP_MUTE)
                .forEach(p -> punishmentService.revokePunishment(p.id(), context.sender().getUUID()));

        context.sender().sendMessage("§aUnmuted " + targetName);
    }

    @Command(name = "warn", permission = "brennon.punish.warn")
    public void warn(CommandContext context) {
        if (context.args().length < 2) {
            context.sender().sendMessage("§c/warn <player> <reason>");
            return;
        }

        String targetName = context.args()[0];
        String reason = String.join(" ", context.args(), 1, context.args().length);

        UUID targetId = context.getUUIDFromName(targetName);
        if (targetId == null) {
            context.sender().sendMessage("§cPlayer not found.");
            return;
        }

        Punishment punishment = punishmentService.createPunishment(
                targetId,
                targetName,
                context.sender().getUUID(),
                context.sender().getName(),
                PunishmentType.WARN,
                reason,
                Instant.now() // Warning expires immediately as it's just a record
        );

        if (punishment != null) {
            context.sender().sendMessage("§aSuccessfully warned " + targetName + " for: " + reason);
            // TODO: Send warning message to target player
        } else {
            context.sender().sendMessage("§cFailed to warn player.");
        }
    }

    @Command(name = "kick", permission = "brennon.punish.kick")
    public void kick(CommandContext context) {
        if (context.args().length < 2) {
            context.sender().sendMessage("§c/kick <player> <reason>");
            return;
        }

        String targetName = context.args()[0];
        String reason = String.join(" ", context.args(), 1, context.args().length);

        UUID targetId = context.getUUIDFromName(targetName);
        if (targetId == null) {
            context.sender().sendMessage("§cPlayer not found.");
            return;
        }

        Punishment punishment = punishmentService.createPunishment(
                targetId,
                targetName,
                context.sender().getUUID(),
                context.sender().getName(),
                PunishmentType.KICK,
                reason,
                Instant.now() // Kick expires immediately as it's just a record
        );

        if (punishment != null) {
            context.sender().sendMessage("§aSuccessfully kicked " + targetName + " for: " + reason);
            // TODO: Kick the target player with reason
        } else {
            context.sender().sendMessage("§cFailed to kick player.");
        }
    }

    @Command(name = "punish", permission = "brennon.punish.info")
    public void punish(CommandContext context) {
        context.sender().sendMessage("§6Punishment Commands:");
        context.sender().sendMessage("§e/ban <player> <reason> §7- Permanently ban a player");
        context.sender().sendMessage("§e/tempban <player> <duration> <reason> §7- Temporarily ban a player");
        context.sender().sendMessage("§e/unban <player> §7- Unban a player");
        context.sender().sendMessage("§e/mute <player> <reason> §7- Permanently mute a player");
        context.sender().sendMessage("§e/tempmute <player> <duration> <reason> §7- Temporarily mute a player");
        context.sender().sendMessage("§e/unmute <player> §7- Unmute a player");
        context.sender().sendMessage("§e/warn <player> <reason> §7- Warn a player");
        context.sender().sendMessage("§e/kick <player> <reason> §7- Kick a player");
        context.sender().sendMessage("§e/history <player> [page] §7- View punishment history");
    }

    @Command(name = "checkmute", permission = "brennon.punish.checkmute")
    public void checkMute(CommandContext context) {
        if (context.args().length < 1) {
            context.sender().sendMessage("§c/checkmute <player>");
            return;
        }

        String targetName = context.args()[0];
        UUID targetId = context.getUUIDFromName(targetName);
        if (targetId == null) {
            context.sender().sendMessage("§cPlayer not found.");
            return;
        }

        List<Punishment> punishments = punishmentService.getActivePunishments(targetId);
        boolean isMuted = punishments.stream()
                .anyMatch(p -> p.type() == PunishmentType.MUTE || p.type() == PunishmentType.TEMP_MUTE);

        if (isMuted) {
            Punishment mute = punishments.stream()
                    .filter(p -> p.type() == PunishmentType.MUTE || p.type() == PunishmentType.TEMP_MUTE)
                    .findFirst()
                    .get();

            String expires = mute.expiresAt() != null ?
                    "until " + formatInstant(mute.expiresAt()) :
                    "permanently";

            context.sender().sendMessage(String.format(
                    "§e%s §cis muted %s for: §f%s",
                    targetName, expires, mute.reason()
            ));
        } else {
            context.sender().sendMessage("§e" + targetName + " §ais not muted.");
        }
    }

    @Command(name = "checkban", permission = "brennon.punish.checkban")
    public void checkBan(CommandContext context) {
        if (context.args().length < 1) {
            context.sender().sendMessage("§c/checkban <player>");
            return;
        }

        String targetName = context.args()[0];
        UUID targetId = context.getUUIDFromName(targetName);
        if (targetId == null) {
            context.sender().sendMessage("§cPlayer not found.");
            return;
        }

        List<Punishment> punishments = punishmentService.getActivePunishments(targetId);
        boolean isBanned = punishments.stream()
                .anyMatch(p -> p.type() == PunishmentType.BAN || p.type() == PunishmentType.TEMP_BAN);

        if (isBanned) {
            Punishment ban = punishments.stream()
                    .filter(p -> p.type() == PunishmentType.BAN || p.type() == PunishmentType.TEMP_BAN)
                    .findFirst()
                    .get();

            String expires = ban.expiresAt() != null ?
                    "until " + formatInstant(ban.expiresAt()) :
                    "permanently";

            context.sender().sendMessage(String.format(
                    "§e%s §cis banned %s for: §f%s",
                    targetName, expires, ban.reason()
            ));
        } else {
            context.sender().sendMessage("§e" + targetName + " §ais not banned.");
        }
    }
}

