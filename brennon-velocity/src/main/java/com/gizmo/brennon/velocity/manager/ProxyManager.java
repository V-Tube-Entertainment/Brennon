package com.gizmo.brennon.velocity.manager;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.gizmo.brennon.core.BrennonCore;
import com.gizmo.brennon.core.server.ServerManager;
import com.gizmo.brennon.core.balancing.LoadBalancer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
        return core.getLoadBalancer()
                .findBestServer(groupId, null)
                .map(server -> servers.get(server.id()));
    }

    public void registerServer(String id, String host, int port) {
        ServerInfo info = new ServerInfo(id, new InetSocketAddress(host, port));
        RegisteredServer server = proxy.registerServer(info);
        servers.put(id, server);

        // Register with core server manager
        serverManager.registerServer(
                id,
                id,  // Using ID as name for now
                "MINECRAFT",
                "default",
                host,
                port,
                false
        );
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
