package com.gizmo.brennon.velocity.player;

import com.gizmo.brennon.core.player.PlayerData;
import com.velocitypowered.api.proxy.Player;
import com.gizmo.brennon.velocity.BrennonVelocity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    private final BrennonVelocity plugin;
    private final Map<UUID, PlayerData> playerData;

    public PlayerManager(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.playerData = new HashMap<>();
    }

    public void loadPlayerData(Player player) {
        // Load player data from database or file
    }

    public void savePlayerData(Player player) {
        // Save player data to database or file
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerData.get(uuid);
    }

    public void updatePlayerData(UUID uuid, PlayerData data) {
        playerData.put(uuid, data);
    }
}
