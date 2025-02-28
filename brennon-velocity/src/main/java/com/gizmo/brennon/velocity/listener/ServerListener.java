package com.gizmo.brennon.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.server.ServerConnectedEvent;
import com.velocitypowered.api.event.server.ServerDisconnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.gizmo.brennon.core.BrennonCore;
import com.gizmo.brennon.core.server.ServerManager;
import com.gizmo.brennon.velocity.manager.ProxyManager;

public class ServerListener {
    private final BrennonCore core;
    private final ProxyManager proxyManager;
    private final ServerManager serverManager;

    public ServerListener(BrennonCore core, ProxyManager proxyManager) {
        this.core = core;
        this.proxyManager = proxyManager;
        this.serverManager = core.getServerManager();
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        RegisteredServer server = event.getServer();
        String serverId = server.getServerInfo().getName();

        // Update server metrics
        serverManager.getServer(serverId).ifPresent(info -> {
            int playerCount = server.getPlayersConnected().size();
            // Update player count in metrics
            core.getNetworkMonitor().recordMetrics(
                    info.createMetricsBuilder()
                            .onlinePlayers(playerCount)
                            .build()
            );
        });
    }

    @Subscribe
    public void onServerDisconnect(ServerDisconnectEvent event) {
        RegisteredServer server = event.getServer();
        String serverId = server.getServerInfo().getName();

        // Update server metrics
        serverManager.getServer(serverId).ifPresent(info -> {
            int playerCount = server.getPlayersConnected().size();
            // Update player count in metrics
            core.getNetworkMonitor().recordMetrics(
                    info.createMetricsBuilder()
                            .onlinePlayers(playerCount)
                            .build()
            );
        });
    }
}
