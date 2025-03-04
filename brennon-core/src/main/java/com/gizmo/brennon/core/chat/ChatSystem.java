package com.gizmo.brennon.core.chat;

import java.util.UUID;
import java.util.Optional;
import java.util.Collection;

public interface ChatSystem {
    /**
     * Initialize the chat system
     */
    void initialize();

    /**
     * Shutdown the chat system and cleanup resources
     */
    void shutdown();

    /**
     * Handle an incoming chat message
     * @param sender The UUID of the sender
     * @param message The message content
     * @param channel The channel ID (optional)
     */
    void handleChat(UUID sender, String message, Optional<String> channel);

    /**
     * Register a new chat channel
     * @param channel The channel to register
     */
    void registerChannel(ChatChannel channel);

    /**
     * Set a player's active channel
     * @param playerId The UUID of the player
     * @param channelId The ID of the channel
     * @return true if successful, false if channel doesn't exist or player doesn't have permission
     */
    boolean setPlayerChannel(UUID playerId, String channelId);

    /**
     * Send a private message between players
     * @param sender The UUID of the sender
     * @param target The UUID of the target
     * @param message The message content
     * @return true if successful, false if either player is offline
     */
    boolean sendPrivateMessage(UUID sender, UUID target, String message);

    /**
     * Get all available channels
     * @return Collection of chat channels
     */
    Collection<ChatChannel> getChannels();

    /**
     * Get a player's current channel
     * @param playerId The UUID of the player
     * @return The player's current channel, or global channel if none set
     */
    ChatChannel getPlayerChannel(UUID playerId);

    /**
     * Get the last player who sent a private message to the specified player
     * @param playerId The UUID of the player
     * @return Optional containing the UUID of the last message sender, or empty if none
     */
    Optional<UUID> getLastMessageSender(UUID playerId);
}