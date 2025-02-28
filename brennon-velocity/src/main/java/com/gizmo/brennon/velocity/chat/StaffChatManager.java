package com.gizmo.brennon.velocity.chat;

import com.velocitypowered.api.proxy.Player;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StaffChatManager {
    private final BrennonVelocity plugin;
    private final Set<UUID> staffChatEnabled = new HashSet<>();

    public StaffChatManager(BrennonVelocity plugin) {
        this.plugin = plugin;
    }

    public void toggleStaffChat(Player player) {
        UUID uuid = player.getUniqueId();
        if (staffChatEnabled.contains(uuid)) {
            staffChatEnabled.remove(uuid);
            player.sendMessage(Component.text("Staff chat disabled", NamedTextColor.RED));
        } else {
            staffChatEnabled.add(uuid);
            player.sendMessage(Component.text("Staff chat enabled", NamedTextColor.GREEN));
        }
    }

    public boolean isInStaffChat(Player player) {
        return staffChatEnabled.contains(player.getUniqueId());
    }

    public void broadcastStaffMessage(Player sender, String message) {
        Component staffMessage = Component.text()
                .append(Component.text("[STAFF] ", NamedTextColor.RED))
                .append(Component.text(sender.getUsername(), NamedTextColor.YELLOW))
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(message, NamedTextColor.WHITE))
                .build();

        plugin.getServer().getAllPlayers().stream()
                .filter(player -> player.hasPermission("brennon.staff.chat"))
                .forEach(player -> player.sendMessage(staffMessage));
    }
}
