package com.gizmo.brennon.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.gizmo.brennon.core.BrennonCore;
import com.gizmo.brennon.core.server.ServerManager;
import com.gizmo.brennon.core.messaging.ServerStatus;
import com.gizmo.brennon.core.messaging.ServerStatusMessage;
import com.gizmo.brennon.velocity.manager.ProxyManager;
import com.google.gson.Gson;

import java.time.Instant;

public class ServerListener {
    private final BrennonCore core;
    private final ProxyManager proxyManager;
    private final ServerManager serverManager;
    private final Gson gson;

    public ServerListener(BrennonCore core, ProxyManager proxyManager) {
        this.core = core;
        this.proxyManager = proxyManager;
        this.serverManager = core.getServerManager();
        this.gson = new Gson();
    }

    @Subscribe
    public void onServerConnected(ServerPostConnectEvent event) {
        RegisteredServer server = event.getPlayer().getCurrentServer()
                .map(connection -> connection.getServer())
                .orElse(null);

        if (server != null) {
            String serverId = server.getServerInfo().getName();

            serverManager.getServer(serverId).ifPresent(info -> {
                int playerCount = server.getPlayersConnected().size();
                ServerStatusMessage statusMessage = new ServerStatusMessage(
                        serverId,
                        ServerStatus.ONLINE,
                        playerCount,
                        -1, // Velocity doesn't track max players at proxy level
                        20.0,
                        0.0,
                        Instant.now()
                );

                core.getMessageBroker().publish("brennon:server:status", gson.toJson(statusMessage));
            });
        }
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        if (event.getResult().getServer().isPresent()) {
            RegisteredServer server = event.getResult().getServer().get();
            String serverId = server.getServerInfo().getName();

            serverManager.getServer(serverId).ifPresent(info -> {
                int playerCount = server.getPlayersConnected().size();
                ServerStatusMessage statusMessage = new ServerStatusMessage(
                        serverId,
                        ServerStatus.ONLINE,
                        playerCount,
                        -1, // Velocity doesn't track max players at proxy level
                        20.0,
                        0.0,
                        Instant.now()
                );

                core.getMessageBroker().publish("brennon:server:status", gson.toJson(statusMessage));
            });
        }
    }
}