package com.gizmo.brennon.core.punishment.appeal.command;

import com.gizmo.brennon.core.command.Command;
import com.gizmo.brennon.core.command.CommandContext;
import com.gizmo.brennon.core.punishment.Punishment;
import com.gizmo.brennon.core.punishment.PunishmentService;
import com.gizmo.brennon.core.punishment.appeal.Appeal;
import com.gizmo.brennon.core.punishment.appeal.AppealManager;
import com.gizmo.brennon.core.punishment.appeal.AppealStatus;
import com.gizmo.brennon.core.user.UserInfo;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.EnumMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AppealCommands {
    private static final int APPEALS_PER_PAGE = 5;

    private final AppealManager appealManager;
    private final PunishmentService punishmentService;

    @Inject
    public AppealCommands(AppealManager appealManager, PunishmentService punishmentService) {
        this.appealManager = appealManager;
        this.punishmentService = punishmentService;
    }

    @Command(name = "appeal", permission = "brennon.appeal.create")
    public void appeal(CommandContext context) {
        if (!context.isPlayer()) {
            context.replyError(Component.text("Only players can create appeals."));
            return;
        }

        if (context.getArgs().size() < 2) {
            context.replyError(Component.text("/appeal <punishment-id> <reason>"));
            return;
        }

        long punishmentId;
        try {
            punishmentId = Long.parseLong(context.getArg(0));
        } catch (NumberFormatException e) {
            context.replyError(Component.text("Invalid punishment ID. Must be a number."));
            return;
        }

        String reason = String.join(" ", context.getArgs().subList(1, context.getArgs().size()));

        Optional<Appeal> appeal = appealManager.createAppeal(punishmentId, context.getSender(), reason);
        if (appeal.isPresent()) {
            context.reply(Component.text("Appeal submitted successfully!")
                    .color(NamedTextColor.GREEN)
                    .append(Component.newline())
                    .append(Component.text("Appeal ID: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(appeal.get().id())
                            .color(NamedTextColor.WHITE)));
        } else {
            context.replyError(Component.text("Could not create appeal. You may have an active appeal, be on cooldown, or have reached the maximum number of appeals."));
        }
    }

    @Command(name = "appeals", permission = "brennon.appeal.view")
    public void viewAppeals(CommandContext context) {
        List<Appeal> pending = appealManager.getPendingAppeals();
        if (pending.isEmpty()) {
            context.reply(Component.text("There are no pending appeals.")
                    .color(NamedTextColor.YELLOW));
            return;
        }

        Component message = Component.text("Pending Appeals:")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .append(Component.newline());

        for (Appeal appeal : pending) {
            message = message.append(Component.text("ID: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(appeal.id())
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(" | Punishment: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(appeal.punishmentId())
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(" | Created: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(formatDate(appeal.createdAt()))
                            .color(NamedTextColor.WHITE))
                    .append(Component.newline())
                    .append(Component.text("Reason: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(appeal.reason())
                            .color(NamedTextColor.WHITE))
                    .append(Component.newline())
                    .append(Component.newline());
        }

        context.reply(message);
    }

    @Command(name = "appealhandle", permission = "brennon.appeal.handle")
    public void handleAppeal(CommandContext context) {
        if (context.getArgs().size() < 3) {
            context.replyError(Component.text("/appealhandle <appeal-id> <approve|deny|escalate> <reason>"));
            return;
        }

        long appealId;
        try {
            appealId = Long.parseLong(context.getArg(0));
        } catch (NumberFormatException e) {
            context.replyError(Component.text("Invalid appeal ID. Must be a number."));
            return;
        }

        String action = context.getArg(1).toLowerCase();
        AppealStatus status;
        switch (action) {
            case "approve" -> status = AppealStatus.APPROVED;
            case "deny" -> status = AppealStatus.DENIED;
            case "escalate" -> status = AppealStatus.ESCALATED;
            default -> {
                context.replyError(Component.text("Invalid action. Must be approve, deny, or escalate."));
                return;
            }
        }

        String response = String.join(" ", context.getArgs().subList(2, context.getArgs().size()));
        Optional<UserInfo> senderInfo = context.getSenderInfo();

        if (senderInfo.isEmpty()) {
            context.replyError(Component.text("Could not determine handler information."));
            return;
        }

        boolean success;
        if (status == AppealStatus.ESCALATED) {
            success = appealManager.escalateAppeal(appealId, context.getSender(), senderInfo.get().username());
        } else {
            success = appealManager.handleAppeal(appealId, context.getSender(), senderInfo.get().username(), status, response);
        }

        if (success) {
            String actionText = switch (status) {
                case APPROVED -> "approved";
                case DENIED -> "denied";
                case ESCALATED -> "escalated";
                default -> "handled";
            };

            context.reply(Component.text("Appeal " + actionText + " successfully!")
                    .color(NamedTextColor.GREEN));
        } else {
            context.replyError(Component.text("Could not handle appeal. It may not exist or may have already been handled."));
        }
    }

    @Command(name = "appealinfo", permission = "brennon.appeal.view")
    public void appealInfo(CommandContext context) {
        if (context.getArgs().isEmpty()) {
            context.replyError(Component.text("/appealinfo <appeal-id>"));
            return;
        }

        long appealId;
        try {
            appealId = Long.parseLong(context.getArg(0));
        } catch (NumberFormatException e) {
            context.replyError(Component.text("Invalid appeal ID. Must be a number."));
            return;
        }

        Optional<Appeal> appeal = appealManager.getAppeal(appealId);
        if (appeal.isEmpty()) {
            context.replyError(Component.text("Appeal not found."));
            return;
        }

        Appeal a = appeal.get();
        Component message = Component.text("Appeal Information:")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .append(Component.newline())
                .append(Component.text("ID: ")
                        .color(NamedTextColor.GRAY))
                .append(Component.text(a.id())
                        .color(NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("Punishment ID: ")
                        .color(NamedTextColor.GRAY))
                .append(Component.text(a.punishmentId())
                        .color(NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("Status: ")
                        .color(NamedTextColor.GRAY))
                .append(Component.text(a.status().name())
                        .color(getStatusColor(a.status())))
                .append(Component.newline())
                .append(Component.text("Created: ")
                        .color(NamedTextColor.GRAY))
                .append(Component.text(formatDate(a.createdAt()))
                        .color(NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("Reason: ")
                        .color(NamedTextColor.GRAY))
                .append(Component.text(a.reason())
                        .color(NamedTextColor.WHITE));

        if (a.handlerId() != null) {
            message = message.append(Component.newline())
                    .append(Component.text("Handled By: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(a.handlerName())
                            .color(NamedTextColor.WHITE))
                    .append(Component.newline())
                    .append(Component.text("Handled At: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(formatDate(a.handledAt()))
                            .color(NamedTextColor.WHITE))
                    .append(Component.newline())
                    .append(Component.text("Response: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(a.response())
                            .color(NamedTextColor.WHITE));
        }

        context.reply(message);
    }

    @Command(name = "appealhistory", permission = "brennon.appeal.history")
    public void appealHistory(CommandContext context) {
        int page = 1;
        UUID targetId = context.getSender();
        String targetName = context.getSenderInfo()
                .map(UserInfo::username)
                .orElse("Unknown");

        if (context.getArgs().size() >= 1) {
            try {
                page = Integer.parseInt(context.getArgs().get(0));
                if (page < 1) {
                    context.replyError(Component.text("Page number must be positive."));
                    return;
                }
            } catch (NumberFormatException e) {
                // Check if the sender has permission to view others' history
                if (context.getSenderInfo().map(info -> info.hasPermission("brennon.appeal.history.others")).orElse(false)) {
                    String playerName = context.getArg(0);
                    Optional<UUID> playerId = punishmentService.lookupPlayer(playerName);
                    if (playerId.isEmpty()) {
                        context.replyError(Component.text("Player not found."));
                        return;
                    }
                    targetId = playerId.get();
                    targetName = playerName;

                    // Check for page number as second argument
                    if (context.getArgs().size() >= 2) {
                        try {
                            page = Integer.parseInt(context.getArgs().get(1));
                            if (page < 1) {
                                context.replyError(Component.text("Page number must be positive."));
                                return;
                            }
                        } catch (NumberFormatException ex) {
                            context.replyError(Component.text("Invalid page number."));
                            return;
                        }
                    }
                } else {
                    context.replyError(Component.text("Invalid page number."));
                    return;
                }
            }
        }

        List<Appeal> appeals = appealManager.getAppealHistory(targetId, page, APPEALS_PER_PAGE);
        int totalAppeals = appealManager.getTotalAppeals(targetId);
        int totalPages = (totalAppeals + APPEALS_PER_PAGE - 1) / APPEALS_PER_PAGE;

        if (appeals.isEmpty()) {
            context.reply(Component.text("No appeals found for " + targetName)
                    .color(NamedTextColor.YELLOW));
            return;
        }

        Map<AppealStatus, Integer> stats = appealManager.getAppealStatistics(targetId);

        Component message = Component.text("Appeal History - " + targetName)
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .append(Component.newline())
                .append(Component.text("Page " + page + "/" + totalPages)
                        .color(NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.newline());

        // Add statistics
        message = message.append(Component.text("Statistics:")
                        .color(NamedTextColor.GRAY))
                .append(Component.newline());

        for (Map.Entry<AppealStatus, Integer> stat : stats.entrySet()) {
            message = message.append(Component.text("  " + stat.getKey().name() + ": ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(stat.getValue())
                            .color(getStatusColor(stat.getKey())))
                    .append(Component.newline());
        }

        message = message.append(Component.newline());

        // Add appeals
        for (Appeal appeal : appeals) {
            message = message.append(Component.text("ID: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(appeal.id())
                            .color(NamedTextColor.WHITE))
                    .append(Component.text(" | Status: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(appeal.status().name())
                            .color(getStatusColor(appeal.status())))
                    .append(Component.text(" | Created: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(formatDate(appeal.createdAt()))
                            .color(NamedTextColor.WHITE))
                    .append(Component.newline())
                    .append(Component.text("  Reason: ")
                            .color(NamedTextColor.GRAY))
                    .append(Component.text(appeal.reason())
                            .color(NamedTextColor.WHITE))
                    .append(Component.newline());

            if (appeal.handlerId() != null) {
                message = message.append(Component.text("  Handled by: ")
                                .color(NamedTextColor.GRAY))
                        .append(Component.text(appeal.handlerName())
                                .color(NamedTextColor.WHITE))
                        .append(Component.text(" | Response: ")
                                .color(NamedTextColor.GRAY))
                        .append(Component.text(appeal.response())
                                .color(NamedTextColor.WHITE))
                        .append(Component.newline());
            }

            message = message.append(Component.newline());
        }

        // Add navigation instructions
        if (totalPages > 1) {
            message = message.append(Component.text("Use /appealhistory " +
                            (context.getArgs().size() >= 2 ? context.getArg(0) + " " : "") +
                            "<page> to view more pages")
                    .color(NamedTextColor.GRAY));
        }

        context.reply(message);
    }

    private NamedTextColor getStatusColor(AppealStatus status) {
        return switch (status) {
            case PENDING -> NamedTextColor.YELLOW;
            case APPROVED -> NamedTextColor.GREEN;
            case DENIED -> NamedTextColor.RED;
            case ESCALATED -> NamedTextColor.GOLD;
        };
    }

    private String formatDate(Instant instant) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneOffset.UTC)
                .format(instant);
    }
}