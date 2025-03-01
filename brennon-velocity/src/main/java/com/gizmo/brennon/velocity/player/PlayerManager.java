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
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages player data and caching
 *
 * @author Gizmo0320
 * @since 2025-03-01 05:04:50
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

        // Create player data directory if it doesn't exist
        try {
            Files.createDirectories(playerDataFolder);
        } catch (IOException e) {
            logger.severe("Failed to create player data directory: " + e.getMessage());
        }
    }

    /**
     * Gets a VelocityPlayer instance for the given player
     *
     * @param player The player to get data for
     * @return The VelocityPlayer instance
     */
    public VelocityPlayer getPlayer(Player player) {
        return players.computeIfAbsent(player.getUniqueId(), k -> {
            VelocityPlayer vPlayer = loadPlayer(player);
            if (vPlayer == null) {
                vPlayer = new VelocityPlayer(player);
                savePlayer(vPlayer);
            }
            return vPlayer;
        });
    }

    /**
     * Gets a VelocityPlayer instance by UUID
     *
     * @param uuid The UUID of the player
     * @return The VelocityPlayer instance, or null if not found
     */
    public VelocityPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    /**
     * Gets all online players
     *
     * @return Collection of all online VelocityPlayer instances
     */
    public Collection<VelocityPlayer> getOnlinePlayers() {
        return players.values();
    }

    /**
     * Loads a player's data from disk
     *
     * @param player The player to load data for
     * @return The loaded VelocityPlayer instance, or null if loading fails
     */
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

    /**
     * Saves a player's data to disk
     *
     * @param player The player data to save
     */
    public void savePlayer(VelocityPlayer player) {
        Path playerFile = playerDataFolder.resolve(player.getUniqueId().toString() + ".json");
        try (Writer writer = Files.newBufferedWriter(playerFile)) {
            gson.toJson(player, writer);
        } catch (IOException e) {
            logger.severe("Failed to save player data for " + player.getUsername() + ": " + e.getMessage());
        }
    }

    /**
     * Called when a player joins the server
     *
     * @param player The joining player
     */
    public void handleJoin(Player player) {
        VelocityPlayer vPlayer = getPlayer(player);
        vPlayer.setLastJoin(java.time.Instant.now());
        vPlayer.setLastKnownAddress(player.getRemoteAddress().getAddress().getHostAddress());

        // Update server if they're connected to one
        player.getCurrentServer().ifPresent(server ->
                vPlayer.setLastServer(server.getServerInfo().getName()));

        // Broadcast join message to staff
        if (player.hasPermission("brennon.staff")) {
            plugin.getServer().getAllPlayers().stream()
                    .filter(p -> p.hasPermission("brennon.staff"))
                    .forEach(p -> p.sendMessage(Component.text()
                            .append(Component.text("[Staff] ", NamedTextColor.RED))
                            .append(Component.text(player.getUsername(), NamedTextColor.YELLOW))
                            .append(Component.text(" has joined the network", NamedTextColor.GRAY))
                            .build()));
        }
    }

    /**
     * Called when a player leaves the server
     *
     * @param player The leaving player
     */
    public void handleQuit(Player player) {
        VelocityPlayer vPlayer = players.remove(player.getUniqueId());
        if (vPlayer != null) {
            savePlayer(vPlayer);
        }
    }

    /**
     * Called when a player switches servers
     *
     * @param player The player switching servers
     * @param serverName The name of the new server
     */
    public void handleServerSwitch(Player player, String serverName) {
        VelocityPlayer vPlayer = getPlayer(player);
        vPlayer.setLastServer(serverName);
        savePlayer(vPlayer);
    }

    /**
     * Saves all player data
     */
    public void saveAll() {
        players.values().forEach(this::savePlayer);
    }
}