package com.gizmo.brennon.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NetworkCommand implements SimpleCommand {
    private final BrennonVelocity plugin;

    public NetworkCommand(BrennonVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) {
            showNetworkInfo(invocation);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "status" -> showDetailedStatus(invocation);
            case "reload" -> {
                if (!invocation.source().hasPermission("brennon.command.network.reload")) {
                    invocation.source().sendMessage(Component.text("You don't have permission to reload the network!", NamedTextColor.RED));
                    return;
                }
                reloadNetwork(invocation);
            }
            default -> invocation.source().sendMessage(Component.text("Usage: /network [status|reload]", NamedTextColor.RED));
        }
    }

    private void showNetworkInfo(Invocation invocation) {
        int totalPlayers = plugin.getServer().getAllPlayers().size();
        int totalServers = plugin.getProxyManager().getServers().size();

        Component info = Component.text()
                .append(Component.text("Network Information", NamedTextColor.GOLD))
                .append(Component.newline())
                .append(Component.text("Total Players: ", NamedTextColor.YELLOW))
                .append(Component.text(totalPlayers, NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("Total Servers: ", NamedTextColor.YELLOW))
                .append(Component.text(totalServers, NamedTextColor.WHITE))
                .build();

        invocation.source().sendMessage(info);
    }

    private void showDetailedStatus(Invocation invocation) {
        if (!invocation.source().hasPermission("brennon.command.network.status")) {
            invocation.source().sendMessage(Component.text("You don't have permission to view detailed status!", NamedTextColor.RED));
            return;
        }

        Component.Builder status = Component.text()
                .append(Component.text("Network Status", NamedTextColor.GOLD))
                .append(Component.newline());

        plugin.getProxyManager().getServers().forEach((name, server) -> {
            status.append(Component.text(name + ": ", NamedTextColor.YELLOW))
                    .append(Component.text(server.getPlayersConnected().size() + " players", NamedTextColor.WHITE))
                    .append(Component.newline());
        });

        invocation.source().sendMessage(status.build());
    }

    private void reloadNetwork(Invocation invocation) {
        try {
            // Implement network reload logic here
            plugin.getCore().reloadConfig();
            invocation.source().sendMessage(Component.text("Network configuration reloaded successfully!", NamedTextColor.GREEN));
        } catch (Exception e) {
            invocation.source().sendMessage(Component.text("Failed to reload network configuration!", NamedTextColor.RED));
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> suggestions = new ArrayList<>();

            if (invocation.arguments().length == 1) {
                if (invocation.source().hasPermission("brennon.command.network.status")) {
                    suggestions.add("status");
                }
                if (invocation.source().hasPermission("brennon.command.network.reload")) {
                    suggestions.add("reload");
                }
            }

            return suggestions;
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("brennon.command.network");
    }
}
