package com.gizmo.brennon.velocity.player;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a player on the Velocity proxy
 *
 * @author Gizmo0320
 * @since 2025-03-01 05:18:45
 */
public class VelocityPlayer {
    private final UUID uniqueId;
    private final String username;
    private String lastServer;
    private final Set<String> permissions;
    private String prefix;
    private String suffix;
    private boolean staffChat;
    private String lastMessage;
    private long lastMessageTime;
    private final Instant firstJoin;
    private Instant lastJoin;
    private String lastKnownAddress;
    private final transient ProxyServer server;

    public VelocityPlayer(Player player, ProxyServer server) {
        this.uniqueId = player.getUniqueId();
        this.username = player.getUsername();
        this.permissions = new HashSet<>();
        this.firstJoin = Instant.now();
        this.lastJoin = Instant.now();
        this.lastKnownAddress = player.getRemoteAddress().getAddress().getHostAddress();
        this.server = server;
        player.getCurrentServer().ifPresent(serverConnection ->
                this.lastServer = serverConnection.getServerInfo().getName());
    }

    // Getters
    public UUID getUniqueId() { return uniqueId; }
    public String getUsername() { return username; }
    public String getLastServer() { return lastServer; }
    public Set<String> getPermissions() { return new HashSet<>(permissions); }
    public String getPrefix() { return prefix; }
    public String getSuffix() { return suffix; }
    public boolean isInStaffChat() { return staffChat; }
    public String getLastMessage() { return lastMessage; }
    public long getLastMessageTime() { return lastMessageTime; }
    public Instant getFirstJoin() { return firstJoin; }
    public Instant getLastJoin() { return lastJoin; }
    public String getLastKnownAddress() { return lastKnownAddress; }

    // Setters
    public void setLastServer(String server) { this.lastServer = server; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public void setStaffChat(boolean staffChat) { this.staffChat = staffChat; }
    public void setLastMessage(String message) {
        this.lastMessage = message;
        this.lastMessageTime = System.currentTimeMillis();
    }
    public void setLastJoin(Instant lastJoin) { this.lastJoin = lastJoin; }
    public void setLastKnownAddress(String address) { this.lastKnownAddress = address; }

    // Permission management
    public void addPermission(String permission) { permissions.add(permission.toLowerCase()); }
    public void removePermission(String permission) { permissions.remove(permission.toLowerCase()); }
    public boolean hasPermission(String permission) { return permissions.contains(permission.toLowerCase()); }

    // Utility methods
    public void sendMessage(Component message) {
        Player player = getPlayer();
        if (player != null) {
            player.sendMessage(message);
        }
    }

    public void sendMessage(String message, NamedTextColor color) {
        sendMessage(Component.text(message, color));
    }

    public Player getPlayer() {
        return server.getPlayer(uniqueId).orElse(null);
    }

    public boolean isOnline() {
        return getPlayer() != null;
    }

    public void disconnect(Component reason) {
        Player player = getPlayer();
        if (player != null) {
            player.disconnect(reason);
        }
    }
}