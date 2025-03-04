package com.gizmo.brennon.core.chat;

import com.gizmo.brennon.core.messaging.MessageBroker;
import com.gizmo.brennon.core.messaging.MessagingChannels;
import com.google.gson.Gson;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    @Override
    public void registerChannel(ChatChannel channel) {
        channels.put(channel.getId().toLowerCase(), channel);
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

    protected abstract void handleIncomingMessage(ChatMessage message);
    protected abstract void broadcastMessage(ChatMessage message);
    protected abstract void sendLocalMessage(ChatMessage message);
    protected abstract boolean hasPermission(UUID playerId, String permission);
    protected abstract boolean isPlayerOnline(UUID playerId);
    protected abstract String getPlayerName(UUID playerId);
}
