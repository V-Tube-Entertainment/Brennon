package com.gizmo.brennon.velocity.command.adapter;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.gizmo.brennon.core.command.CommandContext;
import com.gizmo.brennon.core.user.UserInfo;
import com.gizmo.brennon.velocity.BrennonVelocity;

import java.util.Arrays;
import java.util.List;
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
            plugin.getCore().getUserManager().getUser(senderId)
                    .thenCompose(optUser -> {
                        UserInfo userInfo = optUser.orElse(null);
                        CommandContext context = new CommandContext(
                                label,
                                Arrays.asList(args),
                                senderId,
                                userInfo
                        );
                        return plugin.getCore().getCommandManager().executeCommand(context);
                    });
        } else {
            // Console command
            CommandContext context = new CommandContext(
                    label,
                    Arrays.asList(args),
                    UUID.randomUUID(),
                    null
            );
            plugin.getCore().getCommandManager().executeCommand(context);
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String label = invocation.alias();
        String[] args = invocation.arguments();

        if (invocation.source() instanceof Player player) {
            UUID senderId = player.getUniqueId();
            return plugin.getCore().getUserManager().getUser(senderId)
                    .thenApply(optUser -> {
                        UserInfo userInfo = optUser.orElse(null);
                        CommandContext context = new CommandContext(
                                label,
                                Arrays.asList(args),
                                senderId,
                                userInfo
                        );
                        return plugin.getCore().getCommandManager().tabComplete(context);
                    });
        } else {
            // Console tab completion
            CommandContext context = new CommandContext(
                    label,
                    Arrays.asList(args),
                    UUID.randomUUID(),
                    null
            );
            return CompletableFuture.completedFuture(
                    plugin.getCore().getCommandManager().tabComplete(context)
            );
        }
    }
}