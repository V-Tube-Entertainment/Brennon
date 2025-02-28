package com.gizmo.brennon.core.server;

/**
 * Represents different types of servers in the network.
 *
 * @author Gizmo0320
 * @since 2025-02-28 20:27:24
 */
public enum ServerType {
    MINECRAFT("MINECRAFT"),
    LOBBY("LOBBY"),
    MINIGAME("MINIGAME"),
    CREATIVE("CREATIVE"),
    SURVIVAL("SURVIVAL"),
    PROXY("PROXY"),
    UNKNOWN("UNKNOWN");

    private final String identifier;

    ServerType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets a ServerType from its identifier
     *
     * @param identifier The string identifier
     * @return The matching ServerType or UNKNOWN if not found
     */
    public static ServerType fromString(String identifier) {
        if (identifier == null) return UNKNOWN;

        for (ServerType type : values()) {
            if (type.identifier.equalsIgnoreCase(identifier)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return identifier;
    }
}
