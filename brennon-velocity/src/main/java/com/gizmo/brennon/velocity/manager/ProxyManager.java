package com.gizmo.brennon.velocity.manager;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.gizmo.brennon.core.BrennonCore;
import com.gizmo.brennon.core.server.ServerManager;
import com.gizmo.brennon.core.server.ServerType;
import com.gizmo.brennon.core.balancing.LoadBalancer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages server registration and routing for the Velocity proxy.
 *
 * @author Gizmo0320
 * @since 2025-02-28 20:27:24
 */
public class ProxyManager {
    private final ProxyServer proxy;
    private final BrennonCore core;
    private final ServerManager serverManager;
    private final LoadBalancer loadBalancer;
    private final Map<String, RegisteredServer> servers;

    public ProxyManager(ProxyServer proxy, BrennonCore core) {
        this.proxy = proxy;
        this.core = core;
        this.serverManager = core.getServerManager();
        this.loadBalancer = core.getLoadBalancer();
        this.servers = new ConcurrentHashMap<>();

        // Register existing servers
        registerServers();
    }

    private void registerServers() {
        serverManager.getAllServers().forEach(server -> {
            ServerInfo info = new ServerInfo(
                    server.id(),
                    new InetSocketAddress(server.host(), server.port())
            );
            RegisteredServer registeredServer = proxy.registerServer(info);
            servers.put(server.id(), registeredServer);
        });
    }

    public Optional<RegisteredServer> getServer(String serverId) {
        return Optional.ofNullable(servers.get(serverId));
    }

    public Optional<RegisteredServer> findBestServer(String groupId) {
        return loadBalancer
                .findBestServer(groupId, null)
                .map(server -> servers.get(server.id()));
    }

    /**
     * Registers a new server with both Velocity and the core server manager
     *
     * @param id The server ID
     * @param host The server host
     * @param port The server port
     * @param type The type of server (defaults to MINECRAFT if null)
     * @param groupId The server group ID (defaults to "default" if null)
     */
    public void registerServer(String id, String host, int port, ServerType type, String groupId) {
        // Register with Velocity
        ServerInfo info = new ServerInfo(id, new InetSocketAddress(host, port));
        RegisteredServer server = proxy.registerServer(info);
        servers.put(id, server);

        // Register with core server manager
        serverManager.registerServer(
                id,                  // Server ID
                id,                  // Server name (using ID for now)
                type != null ? type.getIdentifier() : ServerType.MINECRAFT.getIdentifier(), // Server type
                groupId != null ? groupId : "default",  // Server group
                host,               // Server host
                port,               // Server port
                false               // Not restricted by default
        );
    }

    /**
     * Simplified server registration with default type and group
     */
    public void registerServer(String id, String host, int port) {
        registerServer(id, host, port, ServerType.MINECRAFT, "default");
    }

    public void unregisterServer(String id) {
        servers.remove(id);
        proxy.unregisterServer(
                new ServerInfo(id, new InetSocketAddress("localhost", 0))
        );
        serverManager.unregisterServer(id);
    }

    public Map<String, RegisteredServer> getServers() {
        return servers;
    }
}