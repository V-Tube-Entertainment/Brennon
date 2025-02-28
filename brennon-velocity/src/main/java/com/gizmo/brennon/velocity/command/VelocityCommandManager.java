package com.gizmo.brennon.velocity.command;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.proxy.ProxyServer;
import com.gizmo.brennon.velocity.BrennonVelocity;

public class VelocityCommandManager {
    private final BrennonVelocity plugin;
    private final ProxyServer server;
    private final CommandManager commandManager;

    @Inject
    public VelocityCommandManager(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.commandManager = server.getCommandManager();

        registerCommands();
    }

    private void registerCommands() {
        // Server management commands
        registerCommand("server", new ServerCommand(plugin));
        registerCommand("serverinfo", new ServerInfoCommand(plugin));

        // Network commands
        registerCommand("network", new NetworkCommand(plugin));
        registerCommand("send", new SendCommand(plugin));

        // Player management commands
        registerCommand("find", new FindPlayerCommand(plugin));
        registerCommand("alert", new AlertCommand(plugin));

        // Staff commands
        registerCommand("staffchat", new StaffChatCommand(plugin));
        registerCommand("maintenance", new MaintenanceCommand(plugin));
    }

    private void registerCommand(String name, Object command) {
        CommandMeta meta = commandManager.metaBuilder(name)
                .plugin(plugin)
                .build();

        commandManager.register(meta, command);
    }
}
