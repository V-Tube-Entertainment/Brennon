package com.gizmo.brennon.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.gizmo.brennon.velocity.BrennonVelocity;
import com.gizmo.brennon.velocity.player.PlayerManager;

/**
 * Handles player-related events
 *
 * @author Gizmo0320
 * @since 2025-03-01 05:04:50
 */
public class PlayerListener {
    private final BrennonVelocity plugin;
    private final PlayerManager playerManager;

    public PlayerListener(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        playerManager.handleJoin(event.getPlayer());
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        playerManager.handleQuit(event.getPlayer());
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        playerManager.handleServerSwitch(
                event.getPlayer(),
                event.getServer().getServerInfo().getName()
        );
    }
}
