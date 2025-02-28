package com.gizmo.brennon.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.gizmo.brennon.core.messaging.ChatMessage;
import com.gizmo.brennon.core.messaging.MessagingChannels;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import com.google.gson.Gson;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StaffChatCommand implements SimpleCommand {
    private final BrennonVelocity plugin;
    private final Set<UUID> toggledPlayers;
    private final Gson gson;

    public StaffChatCommand(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.toggledPlayers = new HashSet<>();
        this.gson = new Gson();
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(Component.text("Only players can use staff chat!", NamedTextColor.RED));
            return;
        }

        String[] args = invocation.arguments();

        if (args.length == 0) {
            // Toggle staff chat mode
            if (toggledPlayers.contains(player.getUniqueId())) {
                toggledPlayers.remove(player.getUniqueId());
                player.sendMessage(Component.text("Staff chat disabled", NamedTextColor.RED));
            } else {
                toggledPlayers.add(player.getUniqueId());
                player.sendMessage(Component.text("Staff chat enabled", NamedTextColor.GREEN));
            }
            return;
        }

        // Send the message
        String message = String.join(" ", args);
        sendStaffMessage(player, message);
    }

    public void sendStaffMessage(Player sender, String message) {
        ChatMessage staffMessage = new ChatMessage(
                sender.getUniqueId(),
                message,
                plugin.getServer().getBoundAddress().getHostString(),
                Instant.now()
        );

        // Format the message for local display
        Component formattedMessage = Component.text()
                .append(Component.text("STAFF", NamedTextColor.RED))
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                .append(Component.text(sender.getUsername(), NamedTextColor.YELLOW))
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(message, NamedTextColor.WHITE))
                .build();

        // Send to all staff members on this proxy
        plugin.getServer().getAllPlayers().stream()
                .filter(player -> player.hasPermission("brennon.staff"))
                .forEach(player -> player.sendMessage(formattedMessage));

        // Publish to other servers
        plugin.getCore().getMessageBroker().publish(MessagingChannels.STAFF_CHAT,
                gson.toJson(staffMessage));
    }

    public boolean isToggled(UUID playerId) {
        return toggledPlayers.contains(playerId);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("brennon.command.staffchat");
    }
}
