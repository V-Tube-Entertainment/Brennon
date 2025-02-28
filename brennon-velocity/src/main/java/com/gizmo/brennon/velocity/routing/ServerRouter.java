package com.gizmo.brennon.velocity.routing;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.gizmo.brennon.core.server.ServerInfo;
import com.gizmo.brennon.velocity.BrennonVelocity;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ServerRouter {
    private final BrennonVelocity plugin;

    public ServerRouter(BrennonVelocity plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Boolean> connectToFallback(Player player) {
        return findFallbackServer(player)
                .thenCompose(optServer -> {
                    if (optServer.isEmpty()) {
                        return CompletableFuture.completedFuture(false);
                    }
                    return player.createConnectionRequest(optServer.get()).connect()
                            .thenApply(result -> result.isSuccessful());
                });
    }

    public CompletableFuture<Optional<RegisteredServer>> findFallbackServer(Player player) {
        // First try to find a lobby server
        return CompletableFuture.supplyAsync(() -> {
            Optional<RegisteredServer> lobby = plugin.getProxyManager().findBestServer("lobby");
            if (lobby.isPresent()) {
                return lobby;
            }

            // If no lobby available, try hub servers
            Optional<RegisteredServer> hub = plugin.getProxyManager().findBestServer("hub");
            if (hub.isPresent()) {
                return hub;
            }

            // Last resort: any available server the player can access
            return plugin.getProxyManager().getServers().values().stream()
                    .filter(server -> {
                        ServerInfo info = plugin.getCore().getServerManager()
                                .getServer(server.getServerInfo().getName())
                                .orElse(null);
                        return info != null &&
                                info.isOnline() &&
                                !info.isFull() &&
                                player.hasPermission("brennon.server." + info.id());
                    })
                    .findFirst();
        });
    }
}
