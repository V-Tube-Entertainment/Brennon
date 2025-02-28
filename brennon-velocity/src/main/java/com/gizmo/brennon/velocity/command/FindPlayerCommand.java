package com.gizmo.brennon.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.gizmo.brennon.velocity.BrennonVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FindPlayerCommand implements SimpleCommand {
    private final BrennonVelocity plugin;

    public FindPlayerCommand(BrennonVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) {
            invocation.source().sendMessage(Component.text("Usage: /find <player>", NamedTextColor.RED));
            return;
        }

        String playerName = args[0];
        Optional<Player> targetPlayer = plugin.getServer().getPlayer(playerName);

        if (targetPlayer.isEmpty()) {
            invocation.source().sendMessage(Component.text()
                    .append(Component.text("Player ", NamedTextColor.RED))
                    .append(Component.text(playerName, NamedTextColor.YELLOW))
                    .append(Component.text(" is not online.", NamedTextColor.RED))
                    .build());
            return;
        }

        Player player = targetPlayer.get();
        Optional<String> serverName = player.getCurrentServer()
                .map(connection -> connection.getServerInfo().getName());

        if (serverName.isEmpty()) {
            invocation.source().sendMessage(Component.text()
                    .append(Component.text(player.getUsername(), NamedTextColor.YELLOW))
                    .append(Component.text(" is not connected to any server.", NamedTextColor.RED))
                    .build());
            return;
        }

        invocation.source().sendMessage(Component.text()
                .append(Component.text(player.getUsername(), NamedTextColor.YELLOW))
                .append(Component.text(" is currently on ", NamedTextColor.GREEN))
                .append(Component.text(serverName.get(), NamedTextColor.YELLOW))
                .build());
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> suggestions = new ArrayList<>();

            if (invocation.arguments().length == 1) {
                plugin.getServer().getAllPlayers().forEach(player ->
                        suggestions.add(player.getUsername()));
            }

            return suggestions;
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("brennon.command.find");
    }
}
