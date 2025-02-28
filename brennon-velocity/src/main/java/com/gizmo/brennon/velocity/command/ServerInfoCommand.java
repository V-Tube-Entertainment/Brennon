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

public class ServerInfoCommand implements SimpleCommand {
    private final BrennonVelocity plugin;

    public ServerInfoCommand(BrennonVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) {
            invocation.source().sendMessage(Component.text("Usage: /serverinfo <server>", NamedTextColor.RED));
            return;
        }

        String serverName = args[0];
        Optional<RegisteredServer> targetServer = plugin.getProxyManager().getServer(serverName);

        if (targetServer.isEmpty()) {
            invocation.source().sendMessage(Component.text("Server not found!", NamedTextColor.RED));
            return;
        }

        RegisteredServer server = targetServer.get();
        List<Component> components = new ArrayList<>();

        // Add server information
        components.add(Component.text("Server Information: ", NamedTextColor.GOLD));
        components.add(Component.text(serverName, NamedTextColor.YELLOW));
        components.add(Component.newline());
        components.add(Component.text("Players: ", NamedTextColor.GOLD));
        components.add(Component.text(server.getPlayersConnected().size(), NamedTextColor.WHITE));
        components.add(Component.newline());
        components.add(Component.text("Address: ", NamedTextColor.GOLD));
        components.add(Component.text(server.getServerInfo().getAddress().toString(), NamedTextColor.WHITE));

        // Combine and send server info
        Component serverInfo = Component.empty();
        for (Component component : components) {
            serverInfo = serverInfo.append(component);
        }
        invocation.source().sendMessage(serverInfo);

        // Show player list if permitted
        if (invocation.source().hasPermission("brennon.command.serverinfo.players")) {
            List<Component> playerComponents = new ArrayList<>();

            playerComponents.add(Component.newline());
            playerComponents.add(Component.text("Online Players:", NamedTextColor.GOLD));
            playerComponents.add(Component.newline());

            server.getPlayersConnected().forEach(player -> {
                playerComponents.add(Component.text("- ", NamedTextColor.GRAY));
                playerComponents.add(Component.text(player.getUsername(), NamedTextColor.WHITE));
                playerComponents.add(Component.newline());
            });

            // Combine and send player list
            Component playerList = Component.empty();
            for (Component component : playerComponents) {
                playerList = playerList.append(component);
            }
            invocation.source().sendMessage(playerList);
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> suggestions = new ArrayList<>();

            if (invocation.arguments().length == 1) {
                plugin.getProxyManager().getServers().keySet().forEach(suggestions::add);
            }

            return suggestions;
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("brennon.command.serverinfo");
    }
}