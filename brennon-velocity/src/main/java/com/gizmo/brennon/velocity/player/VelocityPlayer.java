package com.gizmo.brennon.velocity.player;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;
import java.util.UUID;

/**
 * Wrapper class for Velocity Player objects
 *
 * @author Gizmo0320
 * @since 2025-02-28 20:48:42
 */
public class VelocityPlayer {
    private final Player player;
    private String lastMessage;
    private long lastMessageTime;

    public VelocityPlayer(Player player) {
        this.player = player;
        this.lastMessage = "";
        this.lastMessageTime = 0L;
    }

    public Player getHandle() {
        return player;
    }

    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    public String getUsername() {
        return player.getUsername();
    }

    public void sendMessage(Component message) {
        player.sendMessage(message);
    }

    public void sendMessage(String message) {
        player.sendMessage(Component.text(message));
    }

    public void sendError(String message) {
        player.sendMessage(Component.text(message, NamedTextColor.RED));
    }

    public void sendSuccess(String message) {
        player.sendMessage(Component.text(message, NamedTextColor.GREEN));
    }

    public Optional<ServerConnection> getCurrentServer() {
        return player.getCurrentServer();
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
        this.lastMessageTime = System.currentTimeMillis();
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }
}
