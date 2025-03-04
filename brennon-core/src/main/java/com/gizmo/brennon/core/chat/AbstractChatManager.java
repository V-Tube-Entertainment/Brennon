package com.gizmo.brennon.core.chat;

import com.gizmo.brennon.core.messaging.MessageBroker;
import com.gizmo.brennon.core.messaging.MessagingChannels;
import com.google.gson.Gson;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class AbstractChatManager implements ChatSystem {
    protected final Map<String, ChatChannel> channels;
    protected final Map<UUID, ChatChannel> playerChannels;
    protected final Map<UUID, UUID> lastMessageSender;
    protected final MessageBroker messageBroker;
    protected final Gson gson;
    protected final String serverName;

    public AbstractChatManager(MessageBroker messageBroker, Gson gson, String serverName) {
        this.messageBroker = messageBroker;
        this.gson = gson;
        this.serverName = serverName;
        this.channels = new ConcurrentHashMap<>();
        this.playerChannels = new ConcurrentHashMap<>();
        this.lastMessageSender = new ConcurrentHashMap<>();
    }

    @Override
    public void initialize() {
        setupMessageBroker();
        registerDefaultChannels();
    }

    @Override
    public void shutdown() {
        channels.clear();
        playerChannels.clear();
        lastMessageSender.clear();
    }

    protected void setupMessageBroker() {
        messageBroker.subscribe(MessagingChannels.CHAT, message -> {
            ChatMessage chatMessage = gson.fromJson(message, ChatMessage.class);
            handleIncomingMessage(chatMessage);
        });
    }

    protected void registerDefaultChannels() {
        registerChannel(new ChatChannel(
                "global",
                "Global",
                "brennon.chat.global",
                "&f",
                "[G]",
                true,
                0
        ));

        registerChannel(new ChatChannel(
                "local",
                "Local",
                "brennon.chat.local",
                "&e",
                "[L]",
                false,
                100
        ));

        registerChannel(new ChatChannel(
                "staff",
                "Staff",
                "brennon.chat.staff",
                "&c",
                "[S]",
                true,
                0
        ));
    }

    @Override
    public void registerChannel(ChatChannel channel) {
        channels.put(channel.getId().toLowerCase(), channel);
    }

    @Override
    public Collection<ChatChannel> getChannels() {
        return Collections.unmodifiableCollection(channels.values());
    }

    @Override
    public ChatChannel getPlayerChannel(UUID playerId) {
        return playerChannels.getOrDefault(playerId, channels.get("global"));
    }

    @Override
    public Optional<UUID> getLastMessageSender(UUID playerId) {
        return Optional.ofNullable(lastMessageSender.get(playerId));
    }

    /**
     * Create a new channel builder
     * @param id The unique identifier for the channel
     * @return A new ChatChannelBuilder instance
     */
    public ChatChannelBuilder createChannel(String id) {
        return new ChatChannelBuilder(id);
    }

    /**
     * Register a custom channel using a builder
     * Example usage:
     * chatManager.registerCustomChannel("trade", channel -> channel
     *     .name("Trade")
     *     .color("&6")
     *     .prefix("[Trade]")
     *     .crossServer(true)
     * );
     *
     * @param id The unique identifier for the channel
     * @param builderFunction A function that configures the channel
     * @return The created and registered ChatChannel
     */
    public ChatChannel registerCustomChannel(String id, Consumer<ChatChannelBuilder> builderFunction) {
        ChatChannelBuilder builder = new ChatChannelBuilder(id);
        builderFunction.accept(builder);
        ChatChannel channel = builder.build();
        registerChannel(channel);
        return channel;
    }

    /**
     * Remove a custom channel
     * @param channelId The ID of the channel to remove
     * @return true if the channel was removed, false if it didn't exist or was a default channel
     */
    public boolean removeChannel(String channelId) {
        // Don't allow removing default channels
        if (isDefaultChannel(channelId)) {
            return false;
        }

        ChatChannel removed = channels.remove(channelId.toLowerCase());
        if (removed != null) {
            // Update any players using this channel to use global
            playerChannels.entrySet().removeIf(entry ->
                    entry.getValue().getId().equals(channelId.toLowerCase()));
        }
        return removed != null;
    }

    /**
     * Check if a channel is a default channel
     * @param channelId The ID of the channel to check
     * @return true if the channel is a default channel
     */
    protected boolean isDefaultChannel(String channelId) {
        return channelId.equalsIgnoreCase("global") ||
                channelId.equalsIgnoreCase("local") ||
                channelId.equalsIgnoreCase("staff");
    }

    protected abstract void handleIncomingMessage(ChatMessage message);
    protected abstract void broadcastMessage(ChatMessage message);
    protected abstract void sendLocalMessage(ChatMessage message);
    protected abstract boolean hasPermission(UUID playerId, String permission);
    protected abstract boolean isPlayerOnline(UUID playerId);
    protected abstract String getPlayerName(UUID playerId);
}