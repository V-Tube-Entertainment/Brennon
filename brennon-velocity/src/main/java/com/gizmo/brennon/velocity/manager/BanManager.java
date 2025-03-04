package com.gizmo.brennon.velocity.manager;

import com.gizmo.brennon.core.punishment.PunishmentService;
import com.gizmo.brennon.core.punishment.PunishmentType;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.time.Instant;
import java.util.UUID;

/**
 * Manages player bans and punishments
 *
 * @author Gizmo0320
 * @since 2025-03-04 01:26:53
 */
public class BanManager {
    private final BrennonVelocity plugin;
    private final PunishmentService punishmentService;

    public BanManager(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.punishmentService = plugin.getCore().getPunishmentService();
    }

    public boolean isBanned(UUID uuid) {
        return punishmentService.getActivePunishments(uuid).stream()
                .anyMatch(p -> p.type() == PunishmentType.BAN);
    }

    public Component getBanMessage(UUID uuid) {
        return punishmentService.getActivePunishments(uuid).stream()
                .filter(p -> p.type() == PunishmentType.BAN)
                .findFirst()
                .map(ban -> Component.text()
                        .append(Component.text("You are banned from this server!\n", NamedTextColor.RED))
                        .append(Component.text("Reason: ", NamedTextColor.GRAY))
                        .append(Component.text(ban.reason(), NamedTextColor.WHITE))
                        .build())
                .orElse(Component.text("You are banned from this server!", NamedTextColor.RED));
    }

    public void ban(UUID uuid, String reason, UUID banner) {
        String bannerName = plugin.getServer().getPlayer(banner)
                .map(p -> p.getUsername())
                .orElse("Console");

        punishmentService.createPunishment(
                uuid,
                plugin.getServer().getPlayer(uuid).map(p -> p.getUsername()).orElse("Unknown"),
                banner,
                bannerName,
                PunishmentType.BAN,
                reason,
                null // Permanent ban
        );
    }

    public void tempBan(UUID uuid, String reason, UUID banner, Instant expiry) {
        String bannerName = plugin.getServer().getPlayer(banner)
                .map(p -> p.getUsername())
                .orElse("Console");

        punishmentService.createPunishment(
                uuid,
                plugin.getServer().getPlayer(uuid).map(p -> p.getUsername()).orElse("Unknown"),
                banner,
                bannerName,
                PunishmentType.BAN,
                reason,
                expiry
        );
    }

    public void unban(UUID uuid, UUID unbanner) {
        punishmentService.getActivePunishments(uuid).stream()
                .filter(p -> p.type() == PunishmentType.BAN)
                .forEach(ban -> punishmentService.revokePunishment(ban.id(), unbanner));
    }
}