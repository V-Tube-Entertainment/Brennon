package com.gizmo.brennon.velocity.listener;

import com.gizmo.brennon.core.BrennonCore;
import com.gizmo.brennon.velocity.manager.ProxyManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.gizmo.brennon.core.user.UserInfo;
import com.gizmo.brennon.core.user.UserManager;
import com.gizmo.brennon.velocity.BrennonVelocity;

import java.util.Optional;
import java.util.UUID;

public class ConnectionListener {
    private final BrennonVelocity plugin;
    private final UserManager userManager;

    public ConnectionListener(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.userManager = plugin.getCore().getUserManager();
    }

    @Subscribe
    public void onPlayerLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String username = player.getUsername();

        Optional<UserInfo> userInfo = userManager.getUser(uuid);
        if (userInfo.isEmpty()) {
            // Create new user if they don't exist
            handleUserJoin(uuid, username, player.getRemoteAddress().getHostString(), "");
        } else {
            // Update existing user information
            handleUserJoin(uuid, username, player.getRemoteAddress().getHostString(),
                    userInfo.get().currentServer());
        }
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        handleUserQuit(uuid);
    }

    private void handleUserJoin(UUID uuid, String username, String ipAddress, String serverId) {
        userManager.handleUserJoin(uuid, username, ipAddress, serverId);
    }

    private void handleUserQuit(UUID uuid) {
        userManager.handleUserQuit(uuid);
    }
}