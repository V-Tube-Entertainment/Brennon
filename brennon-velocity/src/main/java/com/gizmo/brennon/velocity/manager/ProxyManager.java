package com.gizmo.brennon.velocity.manager;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.gizmo.brennon.core.BrennonCore;
import com.gizmo.brennon.core.server.ServerManager;
import com.gizmo.brennon.core.server.ServerType;
import com.gizmo.brennon.core.balancing.LoadBalancer;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Manages server registration and routing for the Velocity proxy.
 *
 * @author Gizmo0320
 * @since 2025-03-04 01:04:38
 */
public class ProxyManager {
    private final BrennonVelocity plugin;
    private final ProxyServer proxy;
    private final BrennonCore core;
    private final ServerManager serverManager;
    private final LoadBalancer loadBalancer;
    private final Map<String, RegisteredServer> servers;
    private final Map<String, ServerStatus> serverStatuses;

    public static class ServerStatus {
        private boolean online;
        private long lastPing;
        private int playerCount;
        private double tps;
        private double memoryUsage;
        private int failedPings;

        public ServerStatus() {
            this.online = false;
            this.lastPing = System.currentTimeMillis();
            this.playerCount = 0;
            this.tps = 20.0;
            this.memoryUsage = 0.0;
            this.failedPings = 0;
        }

        public void updateStatus(boolean online, int playerCount, double tps, double memoryUsage) {
            this.online = online;
            this.playerCount = playerCount;
            this.tps = tps;
            this.memoryUsage = memoryUsage;
            this.lastPing = System.currentTimeMillis();
            if (online) {
                this.failedPings = 0;
            }
        }

        public void incrementFailedPings() {
            this.failedPings++;
            if (this.failedPings >= 3) {
                this.online = false;
            }
        }

        public boolean isOnline() { return online; }
        public int getPlayerCount() { return playerCount; }
        public double getTps() { return tps; }
        public double getMemoryUsage() { return memoryUsage; }
    }

    public ProxyManager(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.proxy = plugin.getServer();
        this.core = plugin.getCore();
        this.serverManager = core.getServerManager();
        this.loadBalancer = core.getLoadBalancer();
        this.servers = new ConcurrentHashMap<>();
        this.serverStatuses = new ConcurrentHashMap<>();

        // Register existing servers
        registerServers();

        // Start server monitoring
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
            serverStatuses.put(server.id(), new ServerStatus());
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
            ServerStatus status = serverStatuses.computeIfAbsent(serverId, k -> new ServerStatus());

            server.ping().thenAccept(ping -> {
                if (ping != null) {
                    status.updateStatus(
                            true,
                            ping.getPlayers().map(p -> p.getOnline()).orElse(0),
                            serverManager.getServer(serverId).map(s -> s.getTps()).orElse(20.0),
                            serverManager.getServer(serverId).map(s -> s.getMemoryUsage()).orElse(0.0)
                    );
                } else {
                    status.incrementFailedPings();
                }

                updateServerStatus(serverId, status.isOnline(), status.getPlayerCount());
            }).exceptionally(throwable -> {
                status.incrementFailedPings();
                updateServerStatus(serverId, false, 0);
                return null;
            });
        }
    }

    private void updateServerStatus(String serverId, boolean online, int playerCount) {
        serverManager.updateServerStatus(serverId, online, playerCount);

        if (!online) {
            // Notify staff about server status change
            proxy.getAllPlayers().stream()
                    .filter(p -> p.hasPermission("brennon.staff"))
                    .forEach(p -> p.sendMessage(
                            Component.text()
                                    .append(Component.text("[Server] ", NamedTextColor.RED))
                                    .append(Component.text(serverId, NamedTextColor.YELLOW))
                                    .append(Component.text(" is now offline!", NamedTextColor.RED))
                                    .build()
                    ));
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
        serverStatuses.put(id, new ServerStatus());

        // Register with core server manager
        serverManager.registerServer(
                id,                  // Server ID
                id,                  // Server name (using ID for now)
                type != null ? type.getIdentifier() : ServerType.MINECRAFT.getIdentifier(),
                groupId != null ? groupId : "default",
                host,
                port,
                false
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
        serverStatuses.remove(id);
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

    public Map<String, ServerStatus> getServerStatuses() {
        return Collections.unmodifiableMap(serverStatuses);
    }

    public Optional<ServerStatus> getServerStatus(String serverId) {
        return Optional.ofNullable(serverStatuses.get(serverId));
    }

    public boolean isServerOnline(String serverId) {
        return getServerStatus(serverId)
                .map(ServerStatus::isOnline)
                .orElse(false);
    }
}