package com.gizmo.brennon.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.gizmo.brennon.velocity.BrennonVelocity;
import com.gizmo.brennon.core.server.ServerInfo;
import com.gizmo.brennon.core.server.ServerStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

/**
 * Handles server-related events
 *
 * @author Gizmo0320
 * @since 2025-03-01 03:24:31
 */
public class ServerListener {
    private final BrennonVelocity plugin;

    public ServerListener(BrennonVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Optional<RegisteredServer> target = event.getResult().getServer();
        if (!target.isPresent()) {
            return;
        }

        RegisteredServer targetServer = target.get();
        String serverId = targetServer.getServerInfo().getName();

        // Get server info from core
        Optional<ServerInfo> serverInfo = plugin.getCore().getServerManager().getServer(serverId);

        if (!serverInfo.isPresent() || serverInfo.get().status() == ServerStatus.OFFLINE) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            event.getPlayer().sendMessage(Component.text("That server is currently unavailable!", NamedTextColor.RED));
            return;
        }

        // Check if server is at capacity
        ServerInfo server = serverInfo.get();
        if (server.isFull() && !event.getPlayer().hasPermission("brennon.bypass.serverfull")) {
            // Try to find alternative server in the same group
            Optional<RegisteredServer> alternative = plugin.getProxyManager().findBestServer(server.group());

            if (alternative.isPresent()) {
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(alternative.get()));
                event.getPlayer().sendMessage(Component.text("Redirecting to a less crowded server...", NamedTextColor.YELLOW));
            } else {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().sendMessage(Component.text("That server is currently full!", NamedTextColor.RED));
            }
            return;
        }

        // Check if server is restricted
        if (server.restricted() && !event.getPlayer().hasPermission("brennon.server." + server.id())) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            event.getPlayer().sendMessage(Component.text("You don't have permission to join this server!", NamedTextColor.RED));
        }
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        // Send welcome message
        event.getPlayer().sendMessage(Component.text()
                .append(Component.text("Connected to ", NamedTextColor.GREEN))
                .append(Component.text(event.getServer().getServerInfo().getName(), NamedTextColor.YELLOW))
                .build());

        // Note: The core's LoadBalancer and ServerManager will handle player counts
        // through their own monitoring systems, so we don't need to update them here
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        // Check if any lobbies are online
        boolean hasOnlineLobby = plugin.getCore().getServerManager()
                .getServersByType("LOBBY").stream()
                .anyMatch(ServerInfo::isOnline);

        if (!hasOnlineLobby) {
            event.setPing(event.getPing().asBuilder()
                    .description(Component.text("Server is currently unavailable!", NamedTextColor.RED))
                    .build());
            return;
        }

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