package com.gizmo.brennon.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.gizmo.brennon.core.messaging.MessagingChannels;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MaintenanceCommand implements SimpleCommand {
    private final BrennonVelocity plugin;
    private boolean maintenanceMode = false;

    public MaintenanceCommand(BrennonVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) {
            showStatus(invocation);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "on" -> enableMaintenance(invocation);
            case "off" -> disableMaintenance(invocation);
            case "kick" -> kickNonStaff(invocation);
            default -> invocation.source().sendMessage(
                    Component.text("Usage: /maintenance <on|off|kick>", NamedTextColor.RED));
        }
    }

    private void showStatus(Invocation invocation) {
        invocation.source().sendMessage(Component.text()
                .append(Component.text("Maintenance Mode: ", NamedTextColor.GOLD))
                .append(Component.text(maintenanceMode ? "Enabled" : "Disabled",
                        maintenanceMode ? NamedTextColor.GREEN : NamedTextColor.RED))
                .build());
    }

    private void enableMaintenance(Invocation invocation) {
        if (maintenanceMode) {
            invocation.source().sendMessage(
                    Component.text("Maintenance mode is already enabled!", NamedTextColor.RED));
            return;
        }

        maintenanceMode = true;
        broadcastMaintenanceStatus(true);
        invocation.source().sendMessage(
                Component.text("Maintenance mode enabled!", NamedTextColor.GREEN));
    }

    private void disableMaintenance(Invocation invocation) {
        if (!maintenanceMode) {
            invocation.source().sendMessage(
                    Component.text("Maintenance mode is already disabled!", NamedTextColor.RED));
            return;
        }

        maintenanceMode = false;
        broadcastMaintenanceStatus(false);
        invocation.source().sendMessage(
                Component.text("Maintenance mode disabled!", NamedTextColor.GREEN));
    }

    private void kickNonStaff(Invocation invocation) {
        if (!maintenanceMode) {
            invocation.source().sendMessage(
                    Component.text("Maintenance mode must be enabled first!", NamedTextColor.RED));
            return;
        }

        int kicked = 0;
        Component kickMessage = Component.text()
                .append(Component.text("Server is under maintenance!", NamedTextColor.RED))
                .append(Component.newline())
                .append(Component.text("Please try again later.", NamedTextColor.YELLOW))
                .build();

        for (Player player : plugin.getServer().getAllPlayers()) {
            if (!player.hasPermission("brennon.maintenance.bypass")) {
                player.disconnect(kickMessage);
                kicked++;
            }
        }

        invocation.source().sendMessage(Component.text()
                .append(Component.text("Kicked ", NamedTextColor.GREEN))
                .append(Component.text(kicked, NamedTextColor.YELLOW))
                .append(Component.text(" non-staff players.", NamedTextColor.GREEN))
                .build());
    }

    private void broadcastMaintenanceStatus(boolean enabled) {
        Component message = Component.text()
                .append(Component.text("Maintenance mode has been ", NamedTextColor.YELLOW))
                .append(Component.text(enabled ? "enabled" : "disabled",
                        enabled ? NamedTextColor.RED : NamedTextColor.GREEN))
                .append(Component.text("!", NamedTextColor.YELLOW))
                .build();

        // Broadcast to all players
        plugin.getServer().getAllPlayers().forEach(player ->
                player.sendMessage(message));

        // Notify other servers
        plugin.getCore().getMessageBroker().publish(MessagingChannels.MAINTENANCE,
                enabled ? "enabled" : "disabled");
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> suggestions = new ArrayList<>();

            if (invocation.arguments().length == 1) {
                suggestions.add("on");
                suggestions.add("off");
                suggestions.add("kick");
            }

            return suggestions;
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("brennon.command.maintenance");
    }

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }
}
