package com.gizmo.brennon.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SendCommand implements SimpleCommand {
    private final BrennonVelocity plugin;

    public SendCommand(BrennonVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length < 2) {
            invocation.source().sendMessage(Component.text("Usage: /send <player> <server>", NamedTextColor.RED));
            return;
        }

        String playerName = args[0];
        String serverName = args[1];

        Optional<Player> targetPlayer = plugin.getServer().getPlayer(playerName);
        Optional<RegisteredServer> targetServer = plugin.getProxyManager().getServer(serverName);

        if (targetPlayer.isEmpty()) {
            invocation.source().sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return;
        }

        if (targetServer.isEmpty()) {
            invocation.source().sendMessage(Component.text("Server not found!", NamedTextColor.RED));
            return;
        }

        Player player = targetPlayer.get();
        RegisteredServer server = targetServer.get();

        // Send the player to the target server
        player.createConnectionRequest(server).connect().thenAccept(result -> {
            if (result.isSuccessful()) {
                invocation.source().sendMessage(Component.text()
                        .append(Component.text("Sent ", NamedTextColor.GREEN))
                        .append(Component.text(player.getUsername(), NamedTextColor.YELLOW))
                        .append(Component.text(" to ", NamedTextColor.GREEN))
                        .append(Component.text(serverName, NamedTextColor.YELLOW))
                        .build());

                player.sendMessage(Component.text()
                        .append(Component.text("You were sent to ", NamedTextColor.GREEN))
                        .append(Component.text(serverName, NamedTextColor.YELLOW))
                        .build());
            } else {
                invocation.source().sendMessage(Component.text()
                        .append(Component.text("Failed to send ", NamedTextColor.RED))
                        .append(Component.text(player.getUsername(), NamedTextColor.YELLOW))
                        .append(Component.text(" to ", NamedTextColor.RED))
                        .append(Component.text(serverName, NamedTextColor.YELLOW))
                        .build());
            }
        });
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> suggestions = new ArrayList<>();

            if (invocation.arguments().length == 1) {
                // Suggest online players
                plugin.getServer().getAllPlayers().forEach(player ->
                        suggestions.add(player.getUsername()));
            } else if (invocation.arguments().length == 2) {
                // Suggest servers
                plugin.getProxyManager().getServers().keySet().forEach(suggestions::add);
            }

            return suggestions;
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("brennon.command.send");
    }
}
