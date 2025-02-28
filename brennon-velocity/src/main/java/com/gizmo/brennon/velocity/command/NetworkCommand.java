package com.gizmo.brennon.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
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

        List<Component> components = new ArrayList<>();
        components.add(Component.text("Network Status", NamedTextColor.GOLD));
        components.add(Component.newline());

        plugin.getProxyManager().getServers().forEach((name, server) -> {
            components.add(Component.text(name + ": ", NamedTextColor.YELLOW));
            components.add(Component.text(server.getPlayersConnected().size() + " players", NamedTextColor.WHITE));
            components.add(Component.newline());
        });

        Component finalMessage = Component.empty();
        for (Component component : components) {
            finalMessage = finalMessage.append(component);
        }

        invocation.source().sendMessage(finalMessage);
    }

    private void reloadNetwork(Invocation invocation) {
        try {
            invocation.source().sendMessage(Component.text("Network reload not implemented yet!", NamedTextColor.YELLOW));
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