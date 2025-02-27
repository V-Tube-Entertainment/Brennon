package com.gizmo.brennon.core.punishment.command;

import com.gizmo.brennon.core.command.Command;
import com.gizmo.brennon.core.command.CommandContext;
import com.gizmo.brennon.core.punishment.PunishmentService;
import com.gizmo.brennon.core.punishment.PunishmentType;
import com.google.inject.Inject;

import java.time.Duration;
import java.time.Instant;
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
            context.sender().sendMessage("Usage: /ban <player> <reason>");
            return;
        }

        String targetName = context.args()[0];
        String reason = String.join(" ", context.args(), 1, context.args().length);

        // TODO: Implement player lookup
        UUID targetId = null; // Get from player lookup

        punishmentService.createPunishment(
                targetId,
                targetName,
                context.sender().getUUID(),
                context.sender().getName(),
                PunishmentType.BAN,
                reason,
                null // permanent ban
        );

        context.sender().sendMessage("Player " + targetName + " has been banned.");
    }

    @Command(name = "tempban", permission = "brennon.punish.tempban")
    public void tempban(CommandContext context) {
        if (context.args().length < 3) {
            context.sender().sendMessage("Usage: /tempban <player> <duration> <reason>");
            return;
        }

        String targetName = context.args()[0];
        Duration duration = parseDuration(context.args()[1]);
        String reason = String.join(" ", context.args(), 2, context.args().length);

        if (duration == null) {
            context.sender().sendMessage("Invalid duration format. Use 1d, 2h, etc.");
            return;
        }

        // TODO: Implement player lookup
        UUID targetId = null; // Get from player lookup

        punishmentService.createPunishment(
                targetId,
                targetName,
                context.sender().getUUID(),
                context.sender().getName(),
                PunishmentType.TEMP_BAN,
                reason,
                Instant.now().plus(duration)
        );

        context.sender().sendMessage("Player " + targetName + " has been temporarily banned.");
    }

    private Duration parseDuration(String input) {
        try {
            char unit = input.charAt(input.length() - 1);
            int amount = Integer.parseInt(input.substring(0, input.length() - 1));

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
}
