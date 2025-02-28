package com.gizmo.brennon.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.gizmo.brennon.core.messaging.MessagingChannels;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AlertCommand implements SimpleCommand {
    private final BrennonVelocity plugin;

    public AlertCommand(BrennonVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) {
            invocation.source().sendMessage(Component.text("Usage: /alert <message>", NamedTextColor.RED));
            return;
        }

        String message = String.join(" ", args);
        Component alertMessage = Component.text()
                .append(Component.text("ALERT", NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                .append(Component.text(message, NamedTextColor.YELLOW))
                .build();

        // Broadcast to all players
        plugin.getServer().getAllPlayers().forEach(player ->
                player.sendMessage(alertMessage));

        // Send alert through message broker for other servers
        plugin.getCore().getMessageBroker().publish(MessagingChannels.ALERTS, message);

        // Confirm to sender
        invocation.source().sendMessage(Component.text("Alert sent!", NamedTextColor.GREEN));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("brennon.command.alert");
    }
}
