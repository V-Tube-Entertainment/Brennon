package com.gizmo.brennon.core.platform;

import net.kyori.adventure.audience.Audience;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.UUID;

public interface Platform {
    /**
     * Gets the platform type.
     *
     * @return The platform type
     */
    PlatformType getType();

    /**
     * Gets the platform version.
     *
     * @return The platform version
     */
    String getVersion();

    /**
     * Gets the data directory for the platform.
     *
     * @return The data directory
     */
    Path getDataDirectory();

    /**
     * Gets the platform logger.
     *
     * @return The logger
     */
    Logger getLogger();

    /**
     * Gets an audience for the specified player.
     *
     * @param playerId The player's UUID
     * @return The audience for the player, or null if not found
     */
    Audience getPlayer(UUID playerId);

    /**
     * Gets an audience for all players on the platform.
     *
     * @return The audience for all players
     */
    Audience getAllPlayers();
}
