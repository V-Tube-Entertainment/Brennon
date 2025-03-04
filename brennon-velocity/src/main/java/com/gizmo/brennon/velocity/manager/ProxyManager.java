package com.gizmo.brennon.velocity.manager;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.gizmo.brennon.core.BrennonCore;
import com.gizmo.brennon.core.server.ServerManager;
import com.gizmo.brennon.core.server.ServerType;
import com.gizmo.brennon.core.balancing.LoadBalancer;
import com.gizmo.brennon.core.server.ServerStatus;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Manages server registration and routing for the Velocity proxy.
 *
 * @author Gizmo0320
 * @since 2025-03-04 01:24:21
 */
public class ProxyManager {
    private final BrennonVelocity plugin;
    private final ProxyServer proxy;
    private final BrennonCore core;
    private final ServerManager serverManager;
    private final LoadBalancer loadBalancer;
    private final Map<String, RegisteredServer> servers;
    private final Map<String, ServerMonitorStatus> serverMonitoring;

    private static class ServerMonitorStatus {
        private int failedPings;
        private long lastPingTime;
        private int playerCount;

        public ServerMonitorStatus() {
            this.failedPings = 0;
            this.lastPingTime = System.currentTimeMillis();
            this.playerCount = 0;
        }

        public void updateStatus(boolean success, int playerCount) {
            if (success) {
                this.failedPings = 0;
                this.playerCount = playerCount;
            } else {
                this.failedPings++;
            }
            this.lastPingTime = System.currentTimeMillis();
        }
    }

    public ProxyManager(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.proxy = plugin.getServer();
        this.core = plugin.getCore();
        this.serverManager = core.getServerManager();
        this.loadBalancer = core.getLoadBalancer();
        this.servers = new ConcurrentHashMap<>();
        this.serverMonitoring = new ConcurrentHashMap<>();

        // Register existing servers
        registerServers();
        startMonitoring();
    }

    private void registerServers() {
        serverManager.getAllServers().forEach(server -> {
            ServerInfo info = new ServerInfo(
                    server.id(),
                    new InetSocketAddress(server.host(), server.port())
            );
            RegisteredServer registeredServer = proxy.registerServer(info);
            servers.put(server.id(), registeredServer);
            serverMonitoring.put(server.id(), new ServerMonitorStatus());
        });
    }

    private void startMonitoring() {
        proxy.getScheduler()
                .buildTask(plugin, this::checkServers)
                .delay(5, TimeUnit.SECONDS)
                .repeat(30, TimeUnit.SECONDS)
                .schedule();
    }

    private void checkServers() {
        for (Map.Entry<String, RegisteredServer> entry : servers.entrySet()) {
            String serverId = entry.getKey();
            RegisteredServer server = entry.getValue();
            ServerMonitorStatus monitor = serverMonitoring.computeIfAbsent(
                    serverId,
                    k -> new ServerMonitorStatus()
            );

            server.ping().thenAccept(ping -> {
                if (ping != null) {
                    monitor.updateStatus(true, ping.getPlayers().map(p -> p.getOnline()).orElse(0));

                    // Update server status using the core's method
                    serverManager.getServer(serverId).ifPresent(serverInfo -> {
                        // Update server status directly
                        serverManager.updateServerStatus(serverId, true, monitor.playerCount);
                    });
                } else {
                    monitor.updateStatus(false, 0);
                    if (monitor.failedPings >= 3) {
                        // Update server status to offline
                        serverManager.updateServerStatus(serverId, false, 0);
                    }
                }
            }).exceptionally(throwable -> {
                monitor.updateStatus(false, 0);
                return null;
            });
        }
    }

    public Optional<RegisteredServer> getServer(String serverId) {
        return Optional.ofNullable(servers.get(serverId));
    }

    public Optional<RegisteredServer> findBestServer(String groupId) {
        return loadBalancer
                .findBestServer(groupId, null)
                .map(server -> servers.get(server.id()));
    }

    public void registerServer(String id, String host, int port, ServerType type, String groupId) {
        // Register with Velocity
        ServerInfo info = new ServerInfo(id, new InetSocketAddress(host, port));
        RegisteredServer server = proxy.registerServer(info);
        servers.put(id, server);
        serverMonitoring.put(id, new ServerMonitorStatus());

        // Register with core server manager using the correct method signature
        serverManager.registerServer(
                id,                  // Server ID
                id,                  // Server name (using ID for now)
                type != null ? type.getIdentifier() : ServerType.MINECRAFT.getIdentifier(),
                groupId != null ? groupId : "default",
                host,
                port,
                false               // Not restricted by default
        );

        // Notify staff about new server
        proxy.getAllPlayers().stream()
                .filter(p -> p.hasPermission("brennon.staff"))
                .forEach(p -> p.sendMessage(
                        Component.text()
                                .append(Component.text("[Server] ", NamedTextColor.GREEN))
                                .append(Component.text(id, NamedTextColor.YELLOW))
                                .append(Component.text(" has been registered!", NamedTextColor.GREEN))
                                .build()
                ));
    }

    public void registerServer(String id, String host, int port) {
        registerServer(id, host, port, ServerType.MINECRAFT, "default");
    }

    public void unregisterServer(String id) {
        servers.remove(id);
        serverMonitoring.remove(id);
        proxy.unregisterServer(
                new ServerInfo(id, new InetSocketAddress("localhost", 0))
        );
        serverManager.unregisterServer(id);

        // Notify staff about server removal
        proxy.getAllPlayers().stream()
                .filter(p -> p.hasPermission("brennon.staff"))
                .forEach(p -> p.sendMessage(
                        Component.text()
                                .append(Component.text("[Server] ", NamedTextColor.RED))
                                .append(Component.text(id, NamedTextColor.YELLOW))
                                .append(Component.text(" has been unregistered!", NamedTextColor.RED))
                                .build()
                ));
    }

    public Map<String, RegisteredServer> getServers() {
        return Collections.unmodifiableMap(servers);
    }

    public Map<String, ServerMonitorStatus> getServerMonitoringStatus() {
        return Collections.unmodifiableMap(serverMonitoring);
    }

    public Optional<ServerMonitorStatus> getServerMonitoringStatus(String serverId) {
        return Optional.ofNullable(serverMonitoring.get(serverId));
    }
}