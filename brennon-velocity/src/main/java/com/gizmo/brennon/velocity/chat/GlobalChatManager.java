package com.gizmo.brennon.velocity.chat;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.gizmo.brennon.core.messaging.ChatMessage;
import com.gizmo.brennon.core.messaging.MessageBroker;
import com.gizmo.brennon.core.messaging.MessagingChannels;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import com.google.gson.Gson;

import java.time.Instant;
import java.util.Optional;

public class GlobalChatManager {
    private final BrennonVelocity plugin;
    private final ProxyServer server;
    private final MessageBroker messageBroker;
    private final Gson gson;

    public GlobalChatManager(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.messageBroker = plugin.getCore().getMessageBroker();
        this.gson = new Gson();

        setupMessageBroker();
    }

    private void setupMessageBroker() {
        messageBroker.subscribe(MessagingChannels.GLOBAL_CHAT, message -> {
            ChatMessage chatMessage = gson.fromJson(message, ChatMessage.class);
            broadcastMessage(chatMessage);
        });
    }

    public void sendGlobalMessage(Player sender, String message) {
        ChatMessage chatMessage = new ChatMessage(
                sender.getUniqueId(),
                message,
                plugin.getServer().getBoundAddress().getHostString(),
                Instant.now()
        );

        messageBroker.publish(MessagingChannels.GLOBAL_CHAT, gson.toJson(chatMessage));
    }

    private void broadcastMessage(ChatMessage message) {
        Optional<String> senderName = plugin.getServer()
                .getPlayer(message.getSender())
                .map(Player::getUsername);

        if (senderName.isEmpty()) return;

        Component component = Component.text()
                .append(Component.text(senderName.get(), NamedTextColor.YELLOW))
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(message.getMessage(), NamedTextColor.WHITE))
                .build();

        server.getAllPlayers().forEach(player ->
                player.sendMessage(component));
    }
}