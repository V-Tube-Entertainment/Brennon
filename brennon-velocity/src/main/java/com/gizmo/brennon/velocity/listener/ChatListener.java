package com.gizmo.brennon.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.gizmo.brennon.velocity.BrennonVelocity;
import com.gizmo.brennon.velocity.player.VelocityPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Handles chat-related events
 *
 * @author Gizmo0320
 * @since 2025-02-28 20:52:30
 */
public class ChatListener {
    private final BrennonVelocity plugin;
    private static final long CHAT_COOLDOWN = 2000; // 2 seconds

    public ChatListener(BrennonVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        VelocityPlayer vPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        // Check chat cooldown
        if (!player.hasPermission("brennon.chat.bypass.cooldown")) {
            long timeSinceLastMessage = System.currentTimeMillis() - vPlayer.getLastMessageTime();
            if (timeSinceLastMessage < CHAT_COOLDOWN) {
                player.sendMessage(Component.text("Please wait before sending another message!", NamedTextColor.RED));
                event.setResult(PlayerChatEvent.ChatResult.denied());
                return;
            }
        }

        // Check for duplicate messages
        if (!player.hasPermission("brennon.chat.bypass.duplicate") &&
                message.equalsIgnoreCase(vPlayer.getLastMessage())) {
            player.sendMessage(Component.text("Please don't send the same message twice!", NamedTextColor.RED));
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }

        // Check for chat filter
        if (!player.hasPermission("brennon.chat.bypass.filter") &&
                plugin.getConfigManager().getConfig().isFilterEnabled()) {
            for (String filtered : plugin.getConfigManager().getConfig().getFilteredWords()) {
                if (message.toLowerCase().contains(filtered.toLowerCase())) {
                    player.sendMessage(Component.text("Your message contains inappropriate content!", NamedTextColor.RED));
                    event.setResult(PlayerChatEvent.ChatResult.denied());
                    return;
                }
            }
        }

        // Handle staff chat
        if (plugin.getStaffChatManager().isInStaffChat(player)) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            plugin.getStaffChatManager().broadcastStaffMessage(player, message);
            return;
        }

        // Update player's last message
        vPlayer.setLastMessage(message);

        // Format chat message
        String format = plugin.getConfigManager().getConfig().getGlobalChatFormat()
                .replace("%server%", player.getCurrentServer().map(server ->
                        server.getServerInfo().getName()).orElse("Unknown"))
                .replace("%player%", player.getUsername())
                .replace("%message%", message);

        // Send to all players
        plugin.getServer().getAllPlayers().forEach(p ->
                p.sendMessage(Component.text(format)));

        // Log chat message
        plugin.getLogger().info(String.format("<%s> %s", player.getUsername(), message));
    }
}
