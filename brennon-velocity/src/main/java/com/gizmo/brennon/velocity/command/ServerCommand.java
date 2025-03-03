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

public class ServerCommand implements SimpleCommand {
    private final BrennonVelocity plugin;

    public ServerCommand(BrennonVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return;
        }

        String[] args = invocation.arguments();

        if (args.length == 0) {
            showServerList(player);
            return;
        }

        String serverName = args[0];
        Optional<RegisteredServer> targetServer = plugin.getProxyManager().getServer(serverName);

        if (targetServer.isEmpty()) {
            player.sendMessage(Component.text("Server not found!", NamedTextColor.RED));
            return;
        }

        if (!player.hasPermission("brennon.server." + serverName)) {
            player.sendMessage(Component.text("You don't have permission to join this server!", NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("Connecting to " + serverName + "...", NamedTextColor.GREEN));
        player.createConnectionRequest(targetServer.get()).fireAndForget();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> suggestions = new ArrayList<>();

            if (invocation.source() instanceof Player player) {
                plugin.getProxyManager().getServers().forEach((id, server) -> {
                    if (player.hasPermission("brennon.server." + id)) {
                        suggestions.add(id);
                    }
                });
            }

            return suggestions;
        });
    }

    private void showServerList(Player player) {
        List<Component> components = new ArrayList<>();

        // Add header
        components.add(Component.text("Available servers:", NamedTextColor.GOLD));
        components.add(Component.newline());

        // Add server entries
        plugin.getProxyManager().getServers().forEach((id, server) -> {
            if (player.hasPermission("brennon.server." + id)) {
                int playerCount = server.getPlayersConnected().size();
                components.add(Component.text("- " + id + " ", NamedTextColor.YELLOW));
                components.add(Component.text("(" + playerCount + " players)", NamedTextColor.GRAY));
                components.add(Component.newline());
            }
        });

        // Combine all components
        Component finalMessage = Component.empty();
        for (Component component : components) {
            finalMessage = finalMessage.append(component);
        }

        player.sendMessage(finalMessage);
    }
}