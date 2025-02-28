package com.gizmo.brennon.velocity.command;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.proxy.ProxyServer;
import com.gizmo.brennon.velocity.BrennonVelocity;
import com.gizmo.brennon.velocity.command.adapter.VelocityCommandAdapter;

public class VelocityCommandManager {
    private final BrennonVelocity plugin;
    private final ProxyServer server;
    private final CommandManager commandManager;
    private final com.gizmo.brennon.core.command.CommandManager coreCommandManager;

    @Inject
    public VelocityCommandManager(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.commandManager = server.getCommandManager();
        this.coreCommandManager = plugin.getCore().getCommandManager();

        registerCommands();
    }

    private void registerCommands() {
        // Server management commands
        registerCommand("server", new ServerCommand(plugin));
        registerCommand("serverinfo", new ServerInfoCommand(plugin));
        registerCommand("send", new SendCommand(plugin));

        // Network commands
        registerCommand("network", new NetworkCommand(plugin));
        registerCommand("find", new FindPlayerCommand(plugin));

        // Staff commands
        registerCommand("staffchat", new StaffChatCommand(plugin));
        registerCommand("alert", new AlertCommand(plugin));
        registerCommand("maintenance", new MaintenanceCommand(plugin));
    }

    private void registerCommand(String name, Object command) {
        // Register with core command manager first
        coreCommandManager.registerCommands(command);

        // Create Velocity command adapter
        VelocityCommandAdapter adapter = new VelocityCommandAdapter(plugin, command);

        // Register with Velocity
        CommandMeta meta = commandManager.metaBuilder(name)
                .plugin(plugin)
                .build();

        commandManager.register(meta, adapter);
    }

    public void unregisterAll() {
        commandManager.getMetadata("server").forEach(commandManager::unregister);
        commandManager.getMetadata("serverinfo").forEach(commandManager::unregister);
        commandManager.getMetadata("send").forEach(commandManager::unregister);
        commandManager.getMetadata("network").forEach(commandManager::unregister);
        commandManager.getMetadata("find").forEach(commandManager::unregister);
        commandManager.getMetadata("staffchat").forEach(commandManager::unregister);
        commandManager.getMetadata("alert").forEach(commandManager::unregister);
        commandManager.getMetadata("maintenance").forEach(commandManager::unregister);
    }
}