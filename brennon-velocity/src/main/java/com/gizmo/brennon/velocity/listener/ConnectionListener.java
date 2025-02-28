package com.gizmo.brennon.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.gizmo.brennon.core.BrennonCore;
import com.gizmo.brennon.core.user.UserManager;
import com.gizmo.brennon.velocity.manager.ProxyManager;

import java.util.Optional;
import java.util.UUID;

public class ConnectionListener {
    private final BrennonCore core;
    private final ProxyManager proxyManager;
    private final UserManager userManager;

    public ConnectionListener(BrennonCore core, ProxyManager proxyManager) {
        this.core = core;
        this.proxyManager = proxyManager;
        this.userManager = core.getUserManager();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String username = player.getUsername();

        // Register or update user in the network
        userManager.getUser(uuid).thenAccept(user -> {
            if (user.isEmpty()) {
                userManager.createUser(uuid, username);
            } else {
                userManager.updateUsername(uuid, username);
            }
        });
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        Optional<RegisteredServer> result = event.getResult().getServer();

        // If no server is selected, find the best available server
        if (result.isEmpty()) {
            Optional<RegisteredServer> server = proxyManager.findBestServer("lobby");
            server.ifPresent(s -> event.setResult(ServerPreConnectEvent.ServerResult.allowed(s)));
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Update user's last seen time and online status
        userManager.setOnline(uuid, false);
    }
}
