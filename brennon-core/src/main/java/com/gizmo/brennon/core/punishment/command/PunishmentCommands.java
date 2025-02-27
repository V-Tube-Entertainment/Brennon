package com.gizmo.brennon.core.punishment.command;

import com.gizmo.brennon.core.command.Command;
import com.gizmo.brennon.core.command.CommandContext;
import com.gizmo.brennon.core.punishment.Punishment;
import com.gizmo.brennon.core.punishment.PunishmentService;
import com.gizmo.brennon.core.punishment.PunishmentType;
import com.gizmo.brennon.core.server.KickService;
import com.gizmo.brennon.core.user.PlayerLookupService;
import com.gizmo.brennon.core.user.UserInfo;
import com.gizmo.brennon.core.util.TimeFormatter;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PunishmentCommands {
    private final PunishmentService punishmentService;
    private final PlayerLookupService playerLookupService;
    private final KickService kickService;

    @Inject
    public PunishmentCommands(PunishmentService punishmentService, PlayerLookupService playerLookupService, KickService kickService) {
        this.punishmentService = punishmentService;
        this.playerLookupService = playerLookupService;
        this.kickService = kickService;
    }

    @Command(name = "ban", permission = "brennon.punish.ban")
    public void ban(CommandContext context) {
        if (context.getArgs().size() < 2) {
            context.replyError(Component.text("/ban <player> <reason>"));
            return;
        }

        String targetName = context.getArg(0);
        String reason = String.join(" ", context.getArgs().subList(1, context.getArgs().size()));

        UUID targetId = playerLookupService.getUUID(targetName);
        if (targetId == null) {
            context.replyError(Component.text("Player not found."));
            return;
        }

        Optional<UserInfo> senderInfo = context.getSenderInfo();
        if (senderInfo.isEmpty()) {
            context.replyError(Component.text("Invalid sender."));
            return;
        }

        Punishment punishment = punishmentService.createPunishment(
                targetId,
                targetName,
                context.getSender(),
                senderInfo.get().username(),
                PunishmentType.BAN,
                reason,
                null // permanent ban
        );

        if (punishment != null) {
            context.reply(Component.text("Successfully banned ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(targetName)
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(" for: ")
                            .color(NamedTextColor.GREEN))
                    .append(Component.text(reason)
                            .color(NamedTextColor.WHITE)));
        } else {
            context.replyError(Component.text("Failed to ban player."));
        }
    }

    @Command(name = "tempban", permission = "brennon.punish.tempban")
    public void tempban(CommandContext context) {
        if (context.getArgs().size() < 3) {
            context.replyError(Component.text("/tempban <player> <duration> <reason>"));
            return;
        }

        String targetName = context.getArg(0);
        Duration duration = parseDuration(context.getArg(1));
        if (duration == null) {
            context.replyError(Component.text("Invalid duration format. Use 1d, 2h, 30m, etc."));
            return;
        }

        String reason = String.join(" ", context.getArgs().subList(2, context.getArgs().size()));

        UUID targetId = playerLookupService.getUUID(targetName);
        if (targetId == null) {
            context.replyError(Component.text("Player not found."));
            return;
        }

        Optional<UserInfo> senderInfo = context.getSenderInfo();
        if (senderInfo.isEmpty()) {
            context.replyError(Component.text("Invalid sender."));
            return;
        }

        Punishment punishment = punishmentService.createPunishment(
                targetId,
                targetName,
                context.getSender(),
                senderInfo.get().username(),
                PunishmentType.TEMP_BAN,
                reason,
                Instant.now().plus(duration)
        );

        if (punishment != null) {
            context.reply(Component.text("Successfully banned ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(targetName)
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(" for ")
                            .color(NamedTextColor.GREEN))
                    .append(formatDuration(duration))
                    .append(Component.text(" Reason: ")
                            .color(NamedTextColor.GREEN))
                    .append(Component.text(reason)
                            .color(NamedTextColor.WHITE)));
        } else {
            context.replyError(Component.text("Failed to ban player."));
        }
    }

    @Command(name = "unban", permission = "brennon.punish.unban")
    public void unban(CommandContext context) {
        if (context.getArgs().isEmpty()) {
            context.replyError(Component.text("/unban <player>"));
            return;
        }

        String targetName = context.getArg(0);
        UUID targetId = playerLookupService.getUUID(targetName);
        if (targetId == null) {
            context.replyError(Component.text("Player not found."));
            return;
        }

        List<Punishment> punishments = punishmentService.getActivePunishments(targetId);
        punishments.stream()
                .filter(p -> p.type() == PunishmentType.BAN || p.type() == PunishmentType.TEMP_BAN)
                .forEach(p -> punishmentService.revokePunishment(p.id(), context.getSender()));

        context.reply(Component.text("Successfully unbanned ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(targetName)
                        .color(NamedTextColor.WHITE)));
    }

    @Command(name = "mute", permission = "brennon.punish.mute")
    public void mute(CommandContext context) {
        if (context.getArgs().size() < 2) {
            context.replyError(Component.text("/mute <player> <reason>"));
            return;
        }

        String targetName = context.getArg(0);
        String reason = String.join(" ", context.getArgs().subList(1, context.getArgs().size()));

        UUID targetId = playerLookupService.getUUID(targetName);
        if (targetId == null) {
            context.replyError(Component.text("Player not found."));
            return;
        }

        Optional<UserInfo> senderInfo = context.getSenderInfo();
        if (senderInfo.isEmpty()) {
            context.replyError(Component.text("Invalid sender."));
            return;
        }

        Punishment punishment = punishmentService.createPunishment(
                targetId,
                targetName,
                context.getSender(),
                senderInfo.get().username(),
                PunishmentType.MUTE,
                reason,
                null // permanent mute
        );

        if (punishment != null) {
            context.reply(Component.text("Successfully muted ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(targetName)
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(" for: ")
                            .color(NamedTextColor.GREEN))
                    .append(Component.text(reason)
                            .color(NamedTextColor.WHITE)));
        } else {
            context.replyError(Component.text("Failed to mute player."));
        }
    }

    @Command(name = "tempmute", permission = "brennon.punish.tempmute")
    public void tempmute(CommandContext context) {
        if (context.getArgs().size() < 3) {
            context.replyError(Component.text("/tempmute <player> <duration> <reason>"));
            return;
        }

        String targetName = context.getArg(0);
        Duration duration = parseDuration(context.getArg(1));
        if (duration == null) {
            context.replyError(Component.text("Invalid duration format. Use 1d, 2h, 30m, etc."));
            return;
        }

        String reason = String.join(" ", context.getArgs().subList(2, context.getArgs().size()));

        UUID targetId = playerLookupService.getUUID(targetName);
        if (targetId == null) {
            context.replyError(Component.text("Player not found."));
            return;
        }

        Optional<UserInfo> senderInfo = context.getSenderInfo();
        if (senderInfo.isEmpty()) {
            context.replyError(Component.text("Invalid sender."));
            return;
        }

        Punishment punishment = punishmentService.createPunishment(
                targetId,
                targetName,
                context.getSender(),
                senderInfo.get().username(),
                PunishmentType.TEMP_MUTE,
                reason,
                Instant.now().plus(duration)
        );

        if (punishment != null) {
            context.reply(Component.text("Successfully muted ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(targetName)
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(" for ")
                            .color(NamedTextColor.GREEN))
                    .append(formatDuration(duration))
                    .append(Component.text(" Reason: ")
                            .color(NamedTextColor.GREEN))
                    .append(Component.text(reason)
                            .color(NamedTextColor.WHITE)));
        } else {
            context.replyError(Component.text("Failed to mute player."));
        }
    }

    @Command(name = "unmute", permission = "brennon.punish.unmute")
    public void unmute(CommandContext context) {
        if (context.getArgs().isEmpty()) {
            context.replyError(Component.text("/unmute <player>"));
            return;
        }

        String targetName = context.getArg(0);
        UUID targetId = playerLookupService.getUUID(targetName);
        if (targetId == null) {
            context.replyError(Component.text("Player not found."));
            return;
        }

        List<Punishment> punishments = punishmentService.getActivePunishments(targetId);
        punishments.stream()
                .filter(p -> p.type() == PunishmentType.MUTE || p.type() == PunishmentType.TEMP_MUTE)
                .forEach(p -> punishmentService.revokePunishment(p.id(), context.getSender()));

        context.reply(Component.text("Successfully unmuted ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(targetName)
                        .color(NamedTextColor.WHITE)));
    }

    @Command(name = "kick", permission = "brennon.punish.kick")
    public void kick(CommandContext context) {
        if (context.getArgs().size() < 2) {
            context.replyError(Component.text("/kick <player> <reason>"));
            return;
        }

        String targetName = context.getArg(0);
        String reason = String.join(" ", context.getArgs().subList(1, context.getArgs().size()));

        UUID targetId = playerLookupService.getUUID(targetName);
        if (targetId == null) {
            context.replyError(Component.text("Player not found."));
            return;
        }

        Optional<UserInfo> senderInfo = context.getSenderInfo();
        if (senderInfo.isEmpty()) {
            context.replyError(Component.text("Invalid sender."));
            return;
        }

        Punishment punishment = punishmentService.createPunishment(
                targetId,
                targetName,
                context.getSender(),
                senderInfo.get().username(),
                PunishmentType.KICK,
                reason,
                Instant.now()
        );

        if (punishment != null) {
            // Perform the actual kick
            boolean kicked = kickService.kickPlayer(targetId, reason, senderInfo.get().username());

            if (kicked) {
                context.reply(Component.text("Successfully kicked ")
                        .color(NamedTextColor.GREEN)
                        .append(Component.text(targetName)
                                .color(NamedTextColor.WHITE))
                        .append(Component.text(" for: ")
                                .color(NamedTextColor.GREEN))
                        .append(Component.text(reason)
                                .color(NamedTextColor.WHITE)));
            } else {
                context.reply(Component.text("Created kick record but failed to kick player. They may have already left.")
                        .color(NamedTextColor.YELLOW));
            }
        } else {
            context.replyError(Component.text("Failed to kick player."));
        }
    }

    @Command(name = "warn", permission = "brennon.punish.warn")
    public void warn(CommandContext context) {
        if (context.getArgs().size() < 2) {
            context.replyError(Component.text("/warn <player> <reason>"));
            return;
        }

        String targetName = context.getArg(0);
        String reason = String.join(" ", context.getArgs().subList(1, context.getArgs().size()));

        UUID targetId = playerLookupService.getUUID(targetName);
        if (targetId == null) {
            context.replyError(Component.text("Player not found."));
            return;
        }

        Optional<UserInfo> senderInfo = context.getSenderInfo();
        if (senderInfo.isEmpty()) {
            context.replyError(Component.text("Invalid sender."));
            return;
        }

        Punishment punishment = punishmentService.createPunishment(
                targetId,
                targetName,
                context.getSender(),
                senderInfo.get().username(),
                PunishmentType.WARN,
                reason,
                Instant.now()
        );

        if (punishment != null) {
            // TODO: Implement warning message delivery
            context.reply(Component.text("Successfully warned ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(targetName)
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(" for: ")
                            .color(NamedTextColor.GREEN))
                    .append(Component.text(reason)
                            .color(NamedTextColor.WHITE)));
        } else {
            context.replyError(Component.text("Failed to warn player."));
        }
    }

    @Command(name = "history", permission = "brennon.punish.history")
    public void history(CommandContext context) {
        if (context.getArgs().isEmpty()) {
            context.replyError(Component.text("/history <player> [page]"));
            return;
        }

        String targetName = context.getArg(0);
        int page;
        try {
            page = context.getArgs().size() > 1 ? Integer.parseInt(context.getArg(1)) : 1;
        } catch (NumberFormatException e) {
            context.replyError(Component.text("Invalid page number."));
            return;
        }

        if (page < 1) {
            context.replyError(Component.text("Page number must be greater than 0."));
            return;
        }

        UUID targetId = playerLookupService.getUUID(targetName);
        if (targetId == null) {
            context.replyError(Component.text("Player not found."));
            return;
        }

        int limit = 10;
        int offset = (page - 1) * limit;

        List<Punishment> history = punishmentService.getPunishmentHistory(targetId, limit, offset);
        if (history.isEmpty()) {
            context.reply(Component.text("No punishment history found for ")
                    .color(NamedTextColor.YELLOW)
                    .append(Component.text(targetName)
                            .color(NamedTextColor.WHITE)));
            return;
        }

        context.reply(Component.text("Punishment history for ")
                .color(NamedTextColor.GOLD)
                .append(Component.text(targetName)
                        .color(NamedTextColor.WHITE))
                .append(Component.text(" (Page " + page + "):")
                        .color(NamedTextColor.GOLD)));

        for (Punishment p : history) {
            Component status = p.active() ?
                    Component.text("Active").color(NamedTextColor.RED) :
                    Component.text("Inactive").color(NamedTextColor.GRAY);

            Component expires;
            if (p.expiresAt() == null) {
                expires = Component.text("permanent").color(NamedTextColor.WHITE);
            } else if (p.active()) {
                expires = Component.text("expires in ")
                        .color(NamedTextColor.GRAY)
                        .append(formatTimeLeft(p.expiresAt()));
            } else {
                expires = Component.text("expired").color(NamedTextColor.GRAY);
            }

            context.reply(Component.text("#" + p.id())
                    .color(NamedTextColor.GRAY)
                    .append(Component.text(" " + p.type().getDisplayName())
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(" by ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(p.issuerName())
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(" on ")
                            .color(NamedTextColor.GRAY))
                    .append(formatInstant(p.createdAt()))
                    .append(Component.text(" - ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(p.reason())
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(" (")
                            .color(NamedTextColor.GRAY))
                    .append(expires)
                    .append(Component.text(") ")
                            .color(NamedTextColor.GRAY))
                    .append(status));
        }
    }

    @Command(name = "checkmute", permission = "brennon.punish.checkmute")
    public void checkMute(CommandContext context) {
        if (context.getArgs().isEmpty()) {
            context.replyError(Component.text("/checkmute <player>"));
            return;
        }

        String targetName = context.getArg(0);
        UUID targetId = playerLookupService.getUUID(targetName);
        if (targetId == null) {
            context.replyError(Component.text("Player not found."));
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

            Component expires = mute.expiresAt() == null ?
                    Component.text("permanently").color(NamedTextColor.WHITE) :
                    Component.text("for ")
                            .color(NamedTextColor.WHITE)
                            .append(formatTimeLeft(mute.expiresAt()))
                            .append(Component.text(" (until ")
                                    .color(NamedTextColor.GRAY))
                            .append(formatInstant(mute.expiresAt()))
                            .append(Component.text(")")
                                    .color(NamedTextColor.GRAY));

            context.reply(Component.text(targetName)
                    .color(NamedTextColor.YELLOW)
                    .append(Component.text(" is muted ")
                            .color(NamedTextColor.RED))
                    .append(expires)
                    .append(Component.newline())
                    .append(Component.text("Reason: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(mute.reason())
                            .color(NamedTextColor.WHITE))
                    .append(Component.newline())
                    .append(Component.text("Muted by: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(mute.issuerName())
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(" on ")
                            .color(NamedTextColor.GRAY))
                    .append(formatInstant(mute.createdAt())));
        } else {
            context.reply(Component.text(targetName)
                    .color(NamedTextColor.YELLOW)
                    .append(Component.text(" is not muted.")
                            .color(NamedTextColor.GREEN)));
        }
    }

    @Command(name = "checkban", permission = "brennon.punish.checkban")
    public void checkBan(CommandContext context) {
        if (context.getArgs().isEmpty()) {
            context.replyError(Component.text("/checkban <player>"));
            return;
        }

        String targetName = context.getArg(0);
        UUID targetId = playerLookupService.getUUID(targetName);
        if (targetId == null) {
            context.replyError(Component.text("Player not found."));
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

            Component expires = ban.expiresAt() == null ?
                    Component.text("permanently").color(NamedTextColor.WHITE) :
                    Component.text("for ")
                            .color(NamedTextColor.WHITE)
                            .append(formatTimeLeft(ban.expiresAt()))
                            .append(Component.text(" (until ")
                                    .color(NamedTextColor.GRAY))
                            .append(formatInstant(ban.expiresAt()))
                            .append(Component.text(")")
                                    .color(NamedTextColor.GRAY));

            context.reply(Component.text(targetName)
                    .color(NamedTextColor.YELLOW)
                    .append(Component.text(" is banned ")
                            .color(NamedTextColor.RED))
                    .append(expires)
                    .append(Component.newline())
                    .append(Component.text("Reason: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(ban.reason())
                            .color(NamedTextColor.WHITE))
                    .append(Component.newline())
                    .append(Component.text("Banned by: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(ban.issuerName())
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(" on ")
                            .color(NamedTextColor.GRAY))
                    .append(formatInstant(ban.createdAt())));
        } else {
            context.reply(Component.text(targetName)
                    .color(NamedTextColor.YELLOW)
                    .append(Component.text(" is not banned.")
                            .color(NamedTextColor.GREEN)));
        }
    }

    @Command(name = "punish", permission = "brennon.punish.info")
    public void punish(CommandContext context) {
        context.reply(Component.text("Punishment Commands:")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD));

        sendHelpLine(context, "/ban <player> <reason>", "Permanently ban a player");
        sendHelpLine(context, "/tempban <player> <duration> <reason>", "Temporarily ban a player");
        sendHelpLine(context, "/unban <player>", "Unban a player");
        sendHelpLine(context, "/mute <player> <reason>", "Permanently mute a player");
        sendHelpLine(context, "/tempmute <player> <duration> <reason>", "Temporarily mute a player");
        sendHelpLine(context, "/unmute <player>", "Unmute a player");
        sendHelpLine(context, "/warn <player> <reason>", "Warn a player");
        sendHelpLine(context, "/kick <player> <reason>", "Kick a player");
        sendHelpLine(context, "/history <player> [page]", "View punishment history");
        sendHelpLine(context, "/checkban <player>", "Check if a player is banned");
        sendHelpLine(context, "/checkmute <player>", "Check if a player is muted");
    }

    private void sendHelpLine(CommandContext context, String command, String description) {
        context.reply(Component.text(command)
                .color(NamedTextColor.YELLOW)
                .append(Component.text(" - ")
                        .color(NamedTextColor.GRAY))
                .append(Component.text(description)
                        .color(NamedTextColor.GRAY)));
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

    private Component formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();

        Component result = Component.empty().color(NamedTextColor.WHITE);
        if (days > 0) {
            result = result.append(Component.text(days + "d "));
        }
        if (hours > 0) {
            result = result.append(Component.text(hours + "h "));
        }
        if (minutes > 0) {
            result = result.append(Component.text(minutes + "m"));
        }
        return result;
    }

    private Component formatTimeLeft(Instant expires) {
        Duration duration = Duration.between(Instant.now(), expires);
        return formatDuration(duration);
    }

    private Component formatInstant(Instant instant) {
        return Component.text(TimeFormatter.format(instant))
                .color(NamedTextColor.WHITE);
    }
}