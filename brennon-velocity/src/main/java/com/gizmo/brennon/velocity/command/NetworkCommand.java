package com.gizmo.brennon.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

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
            case "maintenance" -> {
                if (!invocation.source().hasPermission("brennon.command.network.maintenance")) {
                    invocation.source().sendMessage(Component.text("You don't have permission to toggle maintenance mode!", NamedTextColor.RED));
                    return;
                }
                toggleMaintenance(invocation);
            }
            default -> showUsage(invocation);
        }
    }

    private void showUsage(Invocation invocation) {
        Component usage = Component.text()
                .append(Component.text("Network Command Usage:", NamedTextColor.GOLD))
                .append(Component.newline())
                .append(Component.text("/network", NamedTextColor.YELLOW))
                .append(Component.text(" - Show network information", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("/network status", NamedTextColor.YELLOW))
                .append(Component.text(" - Show detailed network status", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("/network reload", NamedTextColor.YELLOW))
                .append(Component.text(" - Reload network configuration", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("/network maintenance", NamedTextColor.YELLOW))
                .append(Component.text(" - Toggle maintenance mode", NamedTextColor.GRAY))
                .build();

        invocation.source().sendMessage(usage);
    }

    private void showNetworkInfo(Invocation invocation) {
        int totalPlayers = plugin.getServer().getAllPlayers().size();
        int totalServers = plugin.getProxyManager().getServers().size();
        boolean maintenance = plugin.getConfigManager().getConfig().isMaintenance();

        Component info = Component.text()
                .append(Component.text("Network Information", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("Status: ", NamedTextColor.YELLOW))
                .append(maintenance ?
                        Component.text("MAINTENANCE", NamedTextColor.RED) :
                        Component.text("ONLINE", NamedTextColor.GREEN))
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
        components.add(Component.text("Network Status", NamedTextColor.GOLD, TextDecoration.BOLD));
        components.add(Component.newline());

        plugin.getProxyManager().getServers().forEach((name, server) -> {
            boolean isOnline = plugin.getCore().getServerManager()
                    .getServer(name)
                    .map(info -> info.isOnline())
                    .orElse(false);

            Component serverStatus = Component.text()
                    .append(Component.text("➤ ", NamedTextColor.GRAY))
                    .append(Component.text(name, NamedTextColor.YELLOW))
                    .append(Component.text(" [", NamedTextColor.DARK_GRAY))
                    .append(isOnline ?
                            Component.text("ONLINE", NamedTextColor.GREEN) :
                            Component.text("OFFLINE", NamedTextColor.RED))
                    .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                    .append(Component.text("Players: ", NamedTextColor.YELLOW))
                    .append(Component.text(server.getPlayersConnected().size(), NamedTextColor.WHITE))
                    .build();

            components.add(serverStatus);
            components.add(Component.newline());
        });

        Component finalMessage = Component.empty();
        for (Component component : components) {
            finalMessage = finalMessage.append(component);
        }

        invocation.source().sendMessage(finalMessage);
    }

    private void reloadNetwork(Invocation invocation) {
        invocation.source().sendMessage(Component.text("Reloading network configuration...", NamedTextColor.YELLOW));

        try {
            // Reload configuration
            plugin.getConfigManager().reloadConfig();

            // Reconnect to message broker
            plugin.getCore().getMessageBroker().reconnect();

            // Refresh server statuses
            plugin.getProxyManager().getServers().forEach((name, server) ->
                    plugin.getCore().getServerManager().refreshServerStatus(name));

            invocation.source().sendMessage(Component.text("Network configuration reloaded successfully!", NamedTextColor.GREEN));
        } catch (Exception e) {
            invocation.source().sendMessage(Component.text("Failed to reload network configuration: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().severe("Failed to reload network configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void toggleMaintenance(Invocation invocation) {
        boolean newState = !plugin.getConfigManager().getConfig().isMaintenance();
        plugin.getConfigManager().getConfig().setMaintenance(newState);
        plugin.getConfigManager().saveConfig();

        // Broadcast maintenance status
        Component message = Component.text()
                .append(Component.text("NETWORK", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Maintenance mode has been ", NamedTextColor.YELLOW))
                .append(newState ?
                        Component.text("ENABLED", NamedTextColor.RED) :
                        Component.text("DISABLED", NamedTextColor.GREEN))
                .build();

        plugin.getServer().getAllPlayers().forEach(player -> {
            if (!newState || plugin.getConfigManager().getConfig().getMaintenanceWhitelist().contains(player.getUsername())) {
                player.sendMessage(message);
            } else {
                player.disconnect(Component.text(plugin.getConfigManager().getConfig().getMaintenanceMotd(), NamedTextColor.RED));
            }
        });

        invocation.source().sendMessage(message);
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
                if (invocation.source().hasPermission("brennon.command.network.maintenance")) {
                    suggestions.add("maintenance");
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