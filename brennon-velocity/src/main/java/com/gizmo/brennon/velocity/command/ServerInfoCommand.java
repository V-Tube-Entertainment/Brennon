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
        Component message = Component.text()
                .append(Component.text("Server Information: ", NamedTextColor.GOLD))
                .append(Component.text(serverName, NamedTextColor.YELLOW))
                .append(Component.newline())
                .append(Component.text("Players: ", NamedTextColor.GOLD))
                .append(Component.text(server.getPlayersConnected().size(), NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("Address: ", NamedTextColor.GOLD))
                .append(Component.text(server.getServerInfo().getAddress().toString(), NamedTextColor.WHITE))
                .build();

        invocation.source().sendMessage(message);

        // Add player list if the source has permission
        if (invocation.source().hasPermission("brennon.command.serverinfo.players")) {
            Component playerList = Component.text()
                    .append(Component.text("Online Players:", NamedTextColor.GOLD))
                    .append(Component.newline());

            for (Player player : server.getPlayersConnected()) {
                playerList = playerList.append(Component.text("- ", NamedTextColor.GRAY))
                        .append(Component.text(player.getUsername(), NamedTextColor.WHITE))
                        .append(Component.newline());
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
