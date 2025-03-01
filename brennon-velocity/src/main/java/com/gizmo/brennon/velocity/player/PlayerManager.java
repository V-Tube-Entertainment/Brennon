package com.gizmo.brennon.velocity.player;

import com.velocitypowered.api.proxy.Player;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player data and states
 *
 * @author Gizmo0320
 * @since 2025-02-28 20:52:30
 */
public class PlayerManager {
    private final BrennonVelocity plugin;
    private final Map<UUID, VelocityPlayer> players;

    public PlayerManager(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.players = new ConcurrentHashMap<>();
    }

    public void initializePlayer(Player player) {
        VelocityPlayer vPlayer = new VelocityPlayer(player);
        players.put(player.getUniqueId(), vPlayer);

        // Load any saved data from core
        plugin.getCore().getUserManager().getUser(player.getUniqueId()).ifPresent(user -> {
            // Initialize user data
            if (user.hasPermission("brennon.staff")) {
                broadcastToStaff(Component.text()
                        .append(Component.text("[Staff] ", NamedTextColor.RED))
                        .append(Component.text(player.getUsername(), NamedTextColor.YELLOW))
                        .append(Component.text(" is now online", NamedTextColor.GREEN))
                        .build());
            }
        });
    }

    public void cleanupPlayer(Player player) {
        VelocityPlayer vPlayer = players.remove(player.getUniqueId());
        if (vPlayer != null) {
            // Save any necessary data to core
            plugin.getCore().getUserManager().getUser(player.getUniqueId()).ifPresent(user -> {
                // Save user data
                if (user.hasPermission("brennon.staff")) {
                    broadcastToStaff(Component.text()
                            .append(Component.text("[Staff] ", NamedTextColor.RED))
                            .append(Component.text(player.getUsername(), NamedTextColor.YELLOW))
                            .append(Component.text(" has gone offline", NamedTextColor.RED))
                            .build());
                }
            });
        }
    }

    public VelocityPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public Collection<VelocityPlayer> getOnlinePlayers() {
        return players.values();
    }

    public void broadcastMessage(Component message) {
        plugin.getServer().getAllPlayers().forEach(player ->
                player.sendMessage(message));
    }

    public void broadcastToStaff(Component message) {
        plugin.getServer().getAllPlayers().stream()
                .filter(player -> player.hasPermission("brennon.staff"))
                .forEach(player -> player.sendMessage(message));
    }

    public void kickAll(String reason) {
        Component kickMessage = Component.text(reason, NamedTextColor.RED);
        plugin.getServer().getAllPlayers().forEach(player ->
                player.disconnect(kickMessage));
    }

    public void kickAllExceptStaff(String reason) {
        Component kickMessage = Component.text(reason, NamedTextColor.RED);
        plugin.getServer().getAllPlayers().stream()
                .filter(player -> !player.hasPermission("brennon.staff"))
                .forEach(player -> player.disconnect(kickMessage));
    }
}