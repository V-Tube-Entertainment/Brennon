package com.gizmo.brennon.velocity.chat;

import com.gizmo.brennon.core.messaging.ChatMessage;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.gizmo.brennon.core.messaging.MessageBroker;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GlobalChatManager {
    private final BrennonVelocity plugin;
    private final ProxyServer server;
    private final MessageBroker messageBroker;

    public GlobalChatManager(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.messageBroker = plugin.getCore().getMessageBroker();

        setupMessageBroker();
    }

    private void setupMessageBroker() {
        messageBroker.subscribe("brennon:chat", message -> {
            ChatMessage chatMessage = ChatMessage.fromJson(message);
            broadcastMessage(chatMessage);
        });
    }

    public void sendGlobalMessage(Player sender, String message) {
        ChatMessage chatMessage = new ChatMessage(
                sender.getUniqueId(),
                sender.getUsername(),
                message,
                System.currentTimeMillis()
        );

        messageBroker.publish("brennon:chat", chatMessage.toJson());
    }

    private void broadcastMessage(ChatMessage message) {
        Component component = Component.text()
                .append(Component.text(message.username(), NamedTextColor.YELLOW))
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(message.content(), NamedTextColor.WHITE))
                .build();

        server.getAllPlayers().forEach(player ->
                player.sendMessage(component));
    }
}
