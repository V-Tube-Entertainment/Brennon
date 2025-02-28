package com.gizmo.brennon.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
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
    public void onServerConnected(ServerPostConnectEvent event) {
        RegisteredServer server = event.getPlayer().getCurrentServer()
                .map(connection -> connection.getServer())
                .orElse(null);

        if (server != null) {
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

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        if (event.getResult().getServer().isPresent()) {
            RegisteredServer server = event.getResult().getServer().get();
            String serverId = server.getServerInfo().getName();

            // Update server metrics before connection
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
}