package com.gizmo.brennon.velocity.manager;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.gizmo.brennon.core.BrennonCore;
import com.gizmo.brennon.core.server.ServerManager;
import com.gizmo.brennon.core.server.ServerType;
import com.gizmo.brennon.core.balancing.LoadBalancer;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced ProxyManager with advanced server management and monitoring capabilities
 *
 * @author Gizmo0320
 * @since 2025-03-04 00:28:04
 */
public class ProxyManager {
    private final BrennonVelocity plugin;
    private final ProxyServer proxy;
    private final BrennonCore core;
    private final ServerManager serverManager;
    private final LoadBalancer loadBalancer;
    private final Logger logger;

    private final Map<String, RegisteredServer> servers;
    private final Map<String, ServerHealthStatus> serverHealth;
    private ScheduledTask healthCheckTask;
    private ScheduledTask balancingTask;

    public static class ServerHealthStatus {
        private boolean online;
        private long lastResponseTime;
        private int failedPings;
        private double tps;
        private int playerCount;
        private long lastUpdate;

        public ServerHealthStatus() {
            this.online = false;
            this.lastResponseTime = 0;
            this.failedPings = 0;
            this.tps = 20.0;
            this.playerCount = 0;
            this.lastUpdate = System.currentTimeMillis();
        }

        // Getters and setters
        public boolean isOnline() { return online; }
        public long getLastResponseTime() { return lastResponseTime; }
        public int getFailedPings() { return failedPings; }
        public double getTps() { return tps; }
        public int getPlayerCount() { return playerCount; }
        public long getLastUpdate() { return lastUpdate; }

        public void updateStatus(boolean online, long responseTime, double tps, int playerCount) {
            this.online = online;
            this.lastResponseTime = responseTime;
            this.tps = tps;
            this.playerCount = playerCount;
            this.lastUpdate = System.currentTimeMillis();
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
    }

    public ProxyManager(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.proxy = plugin.getServer();
        this.core = plugin.getCore();
        this.serverManager = core.getServerManager();
        this.loadBalancer = core.getLoadBalancer();
        this.logger = plugin.getLogger();
        this.servers = new ConcurrentHashMap<>();
        this.serverHealth = new ConcurrentHashMap<>();

        initializeServers();
        startMonitoring();
    }

    private void initializeServers() {
        try {
            // Register existing servers from core
            serverManager.getAllServers().forEach(server -> {
                ServerInfo info = new ServerInfo(
                        server.id(),
                        new InetSocketAddress(server.host(), server.port())
                );
                RegisteredServer registeredServer = proxy.registerServer(info);
                servers.put(server.id(), registeredServer);
                serverHealth.put(server.id(), new ServerHealthStatus());

                logger.info("Registered server: " + server.id() +
                        " (" + server.host() + ":" + server.port() + ")");
            });

            logger.info("Successfully initialized " + servers.size() + " servers");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize servers", e);
        }
    }

    private void startMonitoring() {
        // Server health monitoring (every 30 seconds)
        healthCheckTask = proxy.getScheduler()
                .buildTask(plugin, this::performHealthCheck)
                .delay(Duration.ZERO)
                .repeat(Duration.ofSeconds(30))
                .schedule();

        // Load balancing check (every 5 minutes)
        balancingTask = proxy.getScheduler()
                .buildTask(plugin, this::performLoadBalancing)
                .delay(Duration.ofMinutes(1))
                .repeat(Duration.ofMinutes(5))
                .schedule();
    }

    private void performHealthCheck() {
        servers.forEach((id, server) -> {
            long startTime = System.currentTimeMillis();
            server.ping()
                    .thenAccept(ping -> {
                        long responseTime = System.currentTimeMillis() - startTime;
                        ServerHealthStatus health = serverHealth.get(id);

                        if (ping != null) {
                            // Update server health status
                            health.updateStatus(
                                    true,
                                    responseTime,
                                    serverManager.getServer(id)
                                            .map(s -> s.getTps())
                                            .orElse(20.0),
                                    ping.getPlayers()
                                            .map(players -> players.getOnline())
                                            .orElse(0)
                            );

                            // Update core server manager
                            serverManager.updateServerStatus(id, true, health.getPlayerCount());
                        } else {
                            health.incrementFailedPings();
                            if (!health.isOnline()) {
                                serverManager.updateServerStatus(id, false, 0);
                                logger.warning("Server " + id + " is offline (failed pings: " +
                                        health.getFailedPings() + ")");
                            }
                        }
                    })
                    .exceptionally(throwable -> {
                        ServerHealthStatus health = serverHealth.get(id);
                        health.incrementFailedPings();
                        if (!health.isOnline()) {
                            serverManager.updateServerStatus(id, false, 0);
                            logger.warning("Failed to ping server " + id + ": " + throwable.getMessage());
                        }
                        return null;
                    });
        });
    }

    private void performLoadBalancing() {
        try {
            // Group servers by type
            Map<String, List<RegisteredServer>> serversByType = new HashMap<>();
            servers.forEach((id, server) -> {
                serverManager.getServer(id).ifPresent(info -> {
                    serversByType.computeIfAbsent(info.type(), k -> new ArrayList<>())
                            .add(server);
                });
            });

            // Check balance for each server type
            serversByType.forEach((type, typeServers) -> {
                if (typeServers.size() <= 1) return;

                // Calculate average load
                double avgLoad = typeServers.stream()
                        .mapToDouble(server -> loadBalancer.calculateServerLoad(
                                server.getServerInfo().getName()))
                        .average()
                        .orElse(0.0);

                // Find overloaded servers
                typeServers.stream()
                        .filter(server -> {
                            double load = loadBalancer.calculateServerLoad(
                                    server.getServerInfo().getName());
                            return load > avgLoad * 1.25; // 25% over average
                        })
                        .forEach(overloadedServer -> {
                            balanceServer(overloadedServer, typeServers, avgLoad);
                        });
            });
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error during load balancing", e);
        }
    }

    private void balanceServer(RegisteredServer overloadedServer,
                               List<RegisteredServer> availableServers,
                               double averageLoad) {
        // Find suitable target server
        Optional<RegisteredServer> targetServer = availableServers.stream()
                .filter(server -> server != overloadedServer)
                .filter(server -> {
                    double load = loadBalancer.calculateServerLoad(
                            server.getServerInfo().getName());
                    return load < averageLoad;
                })
                .min(Comparator.comparingDouble(server ->
                        loadBalancer.calculateServerLoad(server.getServerInfo().getName())));

        if (targetServer.isPresent()) {
            // Calculate how many players to move
            int playersToMove = (int) Math.ceil(
                    (overloadedServer.getPlayersConnected().size() -
                            targetServer.get().getPlayersConnected().size()) / 2.0);

            // Move players
            overloadedServer.getPlayersConnected().stream()
                    .limit(playersToMove)
                    .forEach(player -> {
                        player.createConnectionRequest(targetServer.get()).fireAndForget();
                        player.sendMessage(Component.text()
                                .append(Component.text("You are being moved to a less crowded server for better performance.",
                                        NamedTextColor.YELLOW))
                                .build());
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
        try {
            // Register with Velocity
            ServerInfo info = new ServerInfo(id, new InetSocketAddress(host, port));
            RegisteredServer server = proxy.registerServer(info);
            servers.put(id, server);
            serverHealth.put(id, new ServerHealthStatus());

            // Register with core server manager
            serverManager.registerServer(
                    id,
                    id,
                    type != null ? type.getIdentifier() : ServerType.MINECRAFT.getIdentifier(),
                    groupId != null ? groupId : "default",
                    host,
                    port,
                    false
            );

            logger.info("Successfully registered server: " + id);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to register server: " + id, e);
            throw e;
        }
    }

    public void unregisterServer(String id) {
        try {
            servers.remove(id);
            serverHealth.remove(id);
            proxy.unregisterServer(
                    new ServerInfo(id, new InetSocketAddress("localhost", 0))
            );
            serverManager.unregisterServer(id);
            logger.info("Successfully unregistered server: " + id);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to unregister server: " + id, e);
            throw e;
        }
    }

    public Map<String, ServerHealthStatus> getServerHealth() {
        return Collections.unmodifiableMap(serverHealth);
    }

    public Map<String, RegisteredServer> getServers() {
        return Collections.unmodifiableMap(servers);
    }

    public void shutdown() {
        if (healthCheckTask != null) {
            healthCheckTask.cancel();
        }
        if (balancingTask != null) {
            balancingTask.cancel();
        }
    }
}