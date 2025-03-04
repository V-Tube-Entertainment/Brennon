package com.gizmo.brennon.velocity.command.adapter;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.gizmo.brennon.core.command.CommandContext;
import com.gizmo.brennon.core.user.UserInfo;
import com.gizmo.brennon.velocity.BrennonVelocity;
import com.gizmo.brennon.velocity.command.VelocityCommandMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Adapts commands to work with Velocity's command system
 *
 * @author Gizmo0320
 * @since 2025-03-04 02:10:30
 */
public class VelocityCommandAdapter implements SimpleCommand {
    private final BrennonVelocity plugin;
    private final Object commandInstance;
    private final VelocityCommandMeta metadata;

    public VelocityCommandAdapter(BrennonVelocity plugin, Object commandInstance, VelocityCommandMeta metadata) {
        this.plugin = plugin;
        this.commandInstance = commandInstance;
        this.metadata = metadata;
    }

    @Override
    public void execute(Invocation invocation) {
        String label = invocation.alias();
        String[] args = invocation.arguments();

        // Check permissions
        if (metadata.getPermission() != null) {
            if (!invocation.source().hasPermission(metadata.getPermission())) {
                invocation.source().sendMessage(
                        Component.text("You do not have permission to use this command!")
                                .color(NamedTextColor.RED)
                );
                return;
            }
        }

        if (invocation.source() instanceof Player player) {
            UUID senderId = player.getUniqueId();
            Optional<UserInfo> userInfo = plugin.getCore().getUserManager().getUser(senderId);

            // Check if it's a staff command and the player has staff permissions
            if (metadata.isStaffCommand() && !player.hasPermission("brennon.staff")) {
                player.sendMessage(
                        Component.text("This command is only available to staff members!")
                                .color(NamedTextColor.RED)
                );
                return;
            }

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
            // Check permissions for tab completion
            if (metadata.getPermission() != null && !invocation.source().hasPermission(metadata.getPermission())) {
                return List.of();
            }

            String label = invocation.alias();
            String[] args = invocation.arguments();

            if (invocation.source() instanceof Player player) {
                UUID senderId = player.getUniqueId();
                Optional<UserInfo> userInfo = plugin.getCore().getUserManager().getUser(senderId);

                // Check staff permissions for tab completion
                if (metadata.isStaffCommand() && !player.hasPermission("brennon.staff")) {
                    return List.of();
                }

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