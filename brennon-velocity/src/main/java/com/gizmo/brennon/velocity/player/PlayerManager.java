package com.gizmo.brennon.velocity.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.velocitypowered.api.proxy.Player;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages player data and caching
 *
 * @author Gizmo0320
 * @since 2025-03-04 01:26:53
 */
public class PlayerManager {
    private final BrennonVelocity plugin;
    private final Logger logger;
    private final Map<UUID, VelocityPlayer> players;
    private final Path playerDataFolder;
    private final Gson gson;

    public PlayerManager(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.players = new ConcurrentHashMap<>();
        this.playerDataFolder = plugin.getDataDirectory().resolve("players");
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            Files.createDirectories(playerDataFolder);
        } catch (IOException e) {
            logger.severe("Failed to create player data directory: " + e.getMessage());
        }
    }

    public void initializePlayer(Player player) {
        VelocityPlayer vPlayer = loadPlayer(player);
        if (vPlayer == null) {
            vPlayer = new VelocityPlayer(player, plugin.getServer());
        }
        players.put(player.getUniqueId(), vPlayer);
        handleJoin(player);
    }

    public VelocityPlayer getPlayer(Player player) {
        return players.computeIfAbsent(player.getUniqueId(), k -> {
            VelocityPlayer vPlayer = loadPlayer(player);
            if (vPlayer == null) {
                vPlayer = new VelocityPlayer(player, plugin.getServer());
                savePlayer(vPlayer);
            }
            return vPlayer;
        });
    }

    public VelocityPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public Collection<VelocityPlayer> getOnlinePlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    private VelocityPlayer loadPlayer(Player player) {
        Path playerFile = playerDataFolder.resolve(player.getUniqueId().toString() + ".json");
        if (Files.exists(playerFile)) {
            try (Reader reader = Files.newBufferedReader(playerFile)) {
                return gson.fromJson(reader, VelocityPlayer.class);
            } catch (IOException e) {
                logger.warning("Failed to load player data for " + player.getUsername() + ": " + e.getMessage());
            }
        }
        return null;
    }

    public void savePlayer(VelocityPlayer player) {
        Path playerFile = playerDataFolder.resolve(player.getUniqueId().toString() + ".json");
        try (Writer writer = Files.newBufferedWriter(playerFile)) {
            gson.toJson(player, writer);
        } catch (IOException e) {
            logger.severe("Failed to save player data for " + player.getUsername() + ": " + e.getMessage());
        }
    }

    public void cleanupPlayer(Player player) {
        VelocityPlayer vPlayer = players.remove(player.getUniqueId());
        if (vPlayer != null) {
            savePlayer(vPlayer);
        }
        handleQuit(player);
    }

    // Changed to public for listener access
    public void handleJoin(Player player) {
        VelocityPlayer vPlayer = getPlayer(player);
        vPlayer.setLastJoin(Instant.now());
        vPlayer.setLastKnownAddress(player.getRemoteAddress().getAddress().getHostAddress());

        player.getCurrentServer().ifPresent(server ->
                vPlayer.setLastServer(server.getServerInfo().getName()));

        if (player.hasPermission("brennon.staff")) {
            broadcastStaffMessage(player.getUsername(), " has joined the network", NamedTextColor.GREEN);
        }
    }

    // Changed to public for listener access
    public void handleQuit(Player player) {
        VelocityPlayer vPlayer = players.get(player.getUniqueId());
        if (vPlayer != null) {
            savePlayer(vPlayer);
            if (player.hasPermission("brennon.staff")) {
                broadcastStaffMessage(player.getUsername(), " has left the network", NamedTextColor.RED);
            }
        }
    }

    private void broadcastStaffMessage(String playerName, String message, NamedTextColor color) {
        plugin.getServer().getAllPlayers().stream()
                .filter(p -> p.hasPermission("brennon.staff"))
                .forEach(p -> p.sendMessage(Component.text()
                        .append(Component.text("[Staff] ", NamedTextColor.RED))
                        .append(Component.text(playerName, NamedTextColor.YELLOW))
                        .append(Component.text(message, color))
                        .build()));
    }

    public void handleServerSwitch(Player player, String serverName) {
        VelocityPlayer vPlayer = getPlayer(player);
        if (vPlayer != null) {
            vPlayer.setLastServer(serverName);
            savePlayer(vPlayer);

            // Notify staff if the player is staff
            if (player.hasPermission("brennon.staff")) {
                broadcastStaffMessage(
                        player.getUsername(),
                        String.format(" has connected to %s", serverName),
                        NamedTextColor.GREEN
                );
            }
        }
    }
}