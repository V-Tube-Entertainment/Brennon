package com.gizmo.brennon.velocity.manager;

import com.gizmo.brennon.core.punishment.PunishmentService;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.UUID;

public class BanManager {
    private final BrennonVelocity plugin;
    private final PunishmentService punishmentService;

    public BanManager(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.punishmentService = plugin.getCore().getPunishmentService();
    }

    public boolean isBanned(UUID uuid) {
        return punishmentService.isBanned(uuid);
    }

    public Component getBanMessage(UUID uuid) {
        return punishmentService.getBanReason(uuid)
                .map(reason -> Component.text()
                        .append(Component.text("You are banned from this server!\n", NamedTextColor.RED))
                        .append(Component.text("Reason: ", NamedTextColor.GRAY))
                        .append(Component.text(reason, NamedTextColor.WHITE))
                        .build())
                .orElse(Component.text("You are banned from this server!", NamedTextColor.RED));
    }

    public void ban(UUID uuid, String reason, UUID banner) {
        punishmentService.ban(uuid, reason, banner);
    }

    public void unban(UUID uuid, UUID unbanner) {
        punishmentService.unban(uuid, unbanner);
    }
}
