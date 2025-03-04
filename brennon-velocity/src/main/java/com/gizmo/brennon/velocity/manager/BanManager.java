package com.gizmo.brennon.velocity.manager;

import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.UUID;

public class BanManager {
    private final BrennonVelocity plugin;

    public BanManager(BrennonVelocity plugin) {
        this.plugin = plugin;
    }

    public boolean isBanned(UUID uuid) {
        return plugin.getCore().getPunishmentService().isBanned(uuid);
    }

    public Component getBanMessage(UUID uuid) {
        return plugin.getCore().getPunishmentService()
                .getBanReason(uuid)
                .map(reason -> Component.text()
                        .append(Component.text("You are banned from this server!\n", NamedTextColor.RED))
                        .append(Component.text("Reason: ", NamedTextColor.GRAY))
                        .append(Component.text(reason, NamedTextColor.WHITE))
                        .build())
                .orElse(Component.text("You are banned from this server!", NamedTextColor.RED));
    }
}
