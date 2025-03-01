package com.gizmo.brennon.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerDisconnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

/**
 * Handles server-related events
 *
 * @author Gizmo0320
 * @since 2025-03-01 03:05:15
 */
public class ServerListener {
    private final BrennonVelocity plugin;

    public ServerListener(BrennonVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        RegisteredServer target = event.getServer();

        // Check if server exists
        if (!plugin.getProxyManager().getServer(target.getServerInfo().getName()).isPresent()) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            event.getPlayer().sendMessage(Component.text("That server is currently unavailable!", NamedTextColor.RED));
            return;
        }

        // Check if server is at capacity
        if (target.getPlayersConnected().size() >= plugin.getConfigManager().getConfig().getMaxPlayersPerServer()) {
            // Try to find alternative server
            Optional<RegisteredServer> alternative = plugin.getProxyManager().findBestServer(target.getServerInfo().getName());
            if (alternative.isPresent() && !alternative.get().equals(target)) {
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(alternative.get()));
                event.getPlayer().sendMessage(Component.text("Redirecting to a less crowded server...", NamedTextColor.YELLOW));
            } else {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().sendMessage(Component.text("That server is currently full!", NamedTextColor.RED));
            }
        }
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        // Update server status
        plugin.getCore().getServerManager().updateServerStatus(
                event.getServer().getServerInfo().getName(),
                event.getServer().getPlayersConnected().size()
        );

        // Send welcome message
        event.getPlayer().sendMessage(Component.text()
                .append(Component.text("Connected to ", NamedTextColor.GREEN))
                .append(Component.text(event.getServer().getServerInfo().getName(), NamedTextColor.YELLOW))
                .build());
    }

    @Subscribe
    public void onServerDisconnect(ServerDisconnectEvent event) {
        if (event.getServer() != null) {
            // Update server status
            plugin.getCore().getServerManager().updateServerStatus(
                    event.getServer().getServerInfo().getName(),
                    event.getServer().getPlayersConnected().size() - 1  // Subtract 1 as player is leaving
            );
        }
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        if (plugin.getConfigManager().getConfig().isMaintenance()) {
            // Show maintenance MOTD
            event.setPing(event.getPing().asBuilder()
                    .description(Component.text(plugin.getConfigManager().getConfig().getMaintenanceMotd(), NamedTextColor.RED))
                    .build());
        } else {
            // Show normal MOTD
            event.setPing(event.getPing().asBuilder()
                    .description(Component.text(plugin.getConfigManager().getConfig().getMotd()))
                    .build());
        }
    }
}