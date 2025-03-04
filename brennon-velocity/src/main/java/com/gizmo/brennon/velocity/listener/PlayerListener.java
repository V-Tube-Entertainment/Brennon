package com.gizmo.brennon.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.gizmo.brennon.velocity.BrennonVelocity;
import com.gizmo.brennon.velocity.player.PlayerManager;

/**
 * Handles player-related events
 *
 * @author Gizmo0320
 * @since 2025-03-04 01:52:46
 */
public class PlayerListener {
    private final BrennonVelocity plugin;
    private final PlayerManager playerManager;

    public PlayerListener(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        playerManager.initializePlayer(event.getPlayer());
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        playerManager.cleanupPlayer(event.getPlayer());
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        String serverName = event.getServer().getServerInfo().getName();
        playerManager.handleServerSwitch(event.getPlayer(), serverName);
    }
}
