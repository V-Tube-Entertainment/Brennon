package com.gizmo.brennon.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ChatListener {
    private final BrennonVelocity plugin;

    public ChatListener(BrennonVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        // Implement chat filtering, formatting, and routing
        // Add support for staff chat, global chat, and server-specific chat
    }
}
