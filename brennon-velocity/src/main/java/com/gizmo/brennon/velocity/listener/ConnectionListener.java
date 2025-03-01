package com.gizmo.brennon.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Handles player connection events
 *
 * @author Gizmo0320
 * @since 2025-02-28 20:48:42
 */
public class ConnectionListener {
    private final BrennonVelocity plugin;

    public ConnectionListener(BrennonVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();

        // Check maintenance mode
        if (plugin.getConfigManager().getConfig().isMaintenance() &&
                !plugin.getConfigManager().getConfig().getMaintenanceWhitelist().contains(player.getUsername()) &&
                !player.hasPermission("brennon.maintenance.bypass")) {

            event.setResult(LoginEvent.ComponentResult.denied(
                    Component.text(plugin.getConfigManager().getConfig().getMaintenanceMotd(), NamedTextColor.RED)
            ));
            return;
        }

        // Check if server is at capacity
        if (plugin.getServer().getPlayerCount() >= plugin.getConfigManager().getConfig().getMaxPlayers() &&
                !player.hasPermission("brennon.bypass.maxplayers")) {

            event.setResult(LoginEvent.ComponentResult.denied(
                    Component.text("The server is currently full!", NamedTextColor.RED)
            ));
            return;
        }
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();

        // Initialize player data
        plugin.getPlayerManager().initializePlayer(player);

        // Send welcome message
        player.sendMessage(Component.text()
                .append(Component.text("Welcome to ", NamedTextColor.GREEN))
                .append(Component.text(plugin.getConfigManager().getConfig().getServerName(), NamedTextColor.YELLOW))
                .append(Component.text("!", NamedTextColor.GREEN))
                .build());

        // Broadcast join message to staff
        if (player.hasPermission("brennon.staff")) {
            plugin.getServer().getAllPlayers().stream()
                    .filter(p -> p.hasPermission("brennon.staff"))
                    .forEach(p -> p.sendMessage(Component.text()
                            .append(Component.text("[Staff] ", NamedTextColor.RED))
                            .append(Component.text(player.getUsername(), NamedTextColor.YELLOW))
                            .append(Component.text(" has joined the network", NamedTextColor.GREEN))
                            .build()));
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();

        // Clean up player data
        plugin.getPlayerManager().cleanupPlayer(player);

        // Broadcast leave message to staff
        if (player.hasPermission("brennon.staff")) {
            plugin.getServer().getAllPlayers().stream()
                    .filter(p -> p.hasPermission("brennon.staff"))
                    .forEach(p -> p.sendMessage(Component.text()
                            .append(Component.text("[Staff] ", NamedTextColor.RED))
                            .append(Component.text(player.getUsername(), NamedTextColor.YELLOW))
                            .append(Component.text(" has left the network", NamedTextColor.RED))
                            .build()));
        }
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        // Handle initial server connection
        if (!event.getPlayer().getCurrentServer().isPresent()) {
            plugin.getProxyManager().findBestServer("lobby").ifPresent(server -> {
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(server));
            });
        }
    }
}