package com.gizmo.brennon.core.chat;

/**
 * Builder class for creating custom chat channels.
 */
public class ChatChannelBuilder {
    private String id;
    private String name;
    private String permission;
    private String color = "&f"; // Default white
    private String prefix = "[C]"; // Default custom channel prefix
    private boolean crossServer = false;
    private double radius = 0;

    public ChatChannelBuilder(String id) {
        this.id = id.toLowerCase();
        this.name = id;
        this.permission = "brennon.chat." + id.toLowerCase();
    }

    /**
     * Set the display name of the channel
     * @param name The display name
     * @return The builder instance
     */
    public ChatChannelBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the permission required to use this channel
     * @param permission The permission node
     * @return The builder instance
     */
    public ChatChannelBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    /**
     * Set the color for messages in this channel
     * @param color The color code (e.g., "&a", "&b", etc.)
     * @return The builder instance
     */
    public ChatChannelBuilder color(String color) {
        this.color = color;
        return this;
    }

    /**
     * Set the prefix shown before messages in this channel
     * @param prefix The prefix (e.g., "[Trade]", "[Help]")
     * @return The builder instance
     */
    public ChatChannelBuilder prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Set whether messages in this channel should be sent across servers
     * @param crossServer True if messages should be cross-server
     * @return The builder instance
     */
    public ChatChannelBuilder crossServer(boolean crossServer) {
        this.crossServer = crossServer;
        return this;
    }

    /**
     * Set the radius for local chat (0 for unlimited)
     * @param radius The radius in blocks
     * @return The builder instance
     */
    public ChatChannelBuilder radius(double radius) {
        this.radius = radius;
        return this;
    }

    /**
     * Create the chat channel with the configured settings
     * @return A new ChatChannel instance
     */
    public ChatChannel build() {
        return new ChatChannel(id, name, permission, color, prefix, crossServer, radius);
    }
}
