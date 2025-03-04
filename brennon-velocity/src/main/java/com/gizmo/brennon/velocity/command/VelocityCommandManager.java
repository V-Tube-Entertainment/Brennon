package com.gizmo.brennon.velocity.command;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.proxy.ProxyServer;
import com.gizmo.brennon.velocity.BrennonVelocity;
import com.gizmo.brennon.velocity.command.adapter.VelocityCommandAdapter;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages command registration and execution for Velocity
 *
 * @author Gizmo0320
 * @since 2025-03-04 02:10:30
 */

@Singleton
public class VelocityCommandManager {
    private final BrennonVelocity plugin;
    private final ProxyServer server;
    private final CommandManager commandManager;
    private final com.gizmo.brennon.core.command.CommandManager coreCommandManager;
    private final List<String> registeredCommands;
    private final Map<String, VelocityCommandMeta> commandMetadata;

    @Inject
    public VelocityCommandManager(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.commandManager = server.getCommandManager();
        this.coreCommandManager = plugin.getCore().getCommandManager();
        this.registeredCommands = new ArrayList<>();
        this.commandMetadata = new HashMap<>();

        registerCommands();
    }

    private void registerCommands() {
        // Server management commands
        registerCommand(
                VelocityCommandMeta.builder("server")
                        .permission("brennon.command.server")
                        .description("Switch to another server")
                        .aliases("s", "join")
                        .build(),
                new ServerCommand(plugin)
        );

        registerCommand(
                VelocityCommandMeta.builder("serverinfo")
                        .permission("brennon.command.serverinfo")
                        .description("View server information")
                        .aliases("sinfo", "si")
                        .build(),
                new ServerInfoCommand(plugin)
        );

        registerCommand(
                VelocityCommandMeta.builder("send")
                        .permission("brennon.command.send")
                        .description("Send a player to another server")
                        .staffCommand()
                        .build(),
                new SendCommand(plugin)
        );

        // Network commands
        registerCommand(
                VelocityCommandMeta.builder("network")
                        .permission("brennon.command.network")
                        .description("Network management commands")
                        .aliases("n", "net")
                        .staffCommand()
                        .build(),
                new NetworkCommand(plugin)
        );

        registerCommand(
                VelocityCommandMeta.builder("find")
                        .permission("brennon.command.find")
                        .description("Find a player on the network")
                        .aliases("locate", "where")
                        .staffCommand()
                        .build(),
                new FindPlayerCommand(plugin)
        );

        // Staff commands
        registerCommand(
                VelocityCommandMeta.builder("staffchat")
                        .permission("brennon.command.staffchat")
                        .description("Toggle staff chat")
                        .aliases("sc", "staff")
                        .staffCommand()
                        .build(),
                new StaffChatCommand(plugin)
        );

        registerCommand(
                VelocityCommandMeta.builder("alert")
                        .permission("brennon.command.alert")
                        .description("Send a network-wide alert")
                        .aliases("broadcast", "bc")
                        .staffCommand()
                        .build(),
                new AlertCommand(plugin)
        );

        registerCommand(
                VelocityCommandMeta.builder("maintenance")
                        .permission("brennon.command.maintenance")
                        .description("Toggle maintenance mode")
                        .aliases("maint")
                        .staffCommand()
                        .build(),
                new MaintenanceCommand(plugin)
        );
    }

    private void registerCommand(VelocityCommandMeta meta, Object command) {
        // Register with core command manager first
        coreCommandManager.registerCommands(command);

        // Create Velocity command adapter with metadata
        VelocityCommandAdapter adapter = new VelocityCommandAdapter(plugin, command, meta);

        // Register main command
        CommandMeta velocityMeta = commandManager.metaBuilder(meta.getName())
                .plugin(plugin)
                .aliases(meta.getAliases().toArray(new String[0]))
                .build();

        commandManager.register(velocityMeta, adapter);
        registeredCommands.add(meta.getName());
        commandMetadata.put(meta.getName(), meta);

        // Register aliases
        for (String alias : meta.getAliases()) {
            registeredCommands.add(alias);
        }
    }

    public void unregisterAll() {
        for (String command : registeredCommands) {
            commandManager.unregister(command);
        }
        registeredCommands.clear();
        commandMetadata.clear();
    }

    public VelocityCommandMeta getCommandMeta(String name) {
        return commandMetadata.get(name);
    }
}