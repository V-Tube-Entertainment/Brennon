package com.gizmo.brennon.velocity.command.adapter;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.gizmo.brennon.core.command.CommandContext;
import com.gizmo.brennon.core.user.UserInfo;
import com.gizmo.brennon.velocity.BrennonVelocity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VelocityCommandAdapter implements SimpleCommand {
    private final BrennonVelocity plugin;
    private final Object commandInstance;

    public VelocityCommandAdapter(BrennonVelocity plugin, Object commandInstance) {
        this.plugin = plugin;
        this.commandInstance = commandInstance;
    }

    @Override
    public void execute(Invocation invocation) {
        String label = invocation.alias();
        String[] args = invocation.arguments();

        if (invocation.source() instanceof Player player) {
            UUID senderId = player.getUniqueId();
            // Get user info synchronously since we need it for command execution
            Optional<UserInfo> userInfo = plugin.getCore().getUserManager().getUser(senderId);

            CommandContext context = new CommandContext(
                    label,
                    Arrays.asList(args),
                    senderId,
                    userInfo.orElse(null)
            );

            plugin.getCore().getCommandManager().executeCommand(context);
        } else {
            // Console command execution
            CommandContext context = new CommandContext(
                    label,
                    Arrays.asList(args),
                    UUID.randomUUID(), // Console UUID
                    null
            );

            plugin.getCore().getCommandManager().executeCommand(context);
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {
            String label = invocation.alias();
            String[] args = invocation.arguments();

            if (invocation.source() instanceof Player player) {
                UUID senderId = player.getUniqueId();
                Optional<UserInfo> userInfo = plugin.getCore().getUserManager().getUser(senderId);

                CommandContext context = new CommandContext(
                        label,
                        Arrays.asList(args),
                        senderId,
                        userInfo.orElse(null)
                );

                return plugin.getCore().getCommandManager().tabComplete(context);
            } else {
                // Console tab completion
                CommandContext context = new CommandContext(
                        label,
                        Arrays.asList(args),
                        UUID.randomUUID(),
                        null
                );

                return plugin.getCore().getCommandManager().tabComplete(context);
            }
        });
    }
}