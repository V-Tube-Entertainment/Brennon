package com.gizmo.brennon.velocity;

import com.gizmo.brennon.velocity.listener.PlayerListener;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.gizmo.brennon.core.BrennonCore;
import com.gizmo.brennon.velocity.command.VelocityCommandManager;
import com.gizmo.brennon.velocity.config.ConfigManager;
import com.gizmo.brennon.velocity.chat.StaffChatManager;
import com.gizmo.brennon.velocity.manager.ProxyManager;
import com.gizmo.brennon.velocity.player.PlayerManager;
import com.gizmo.brennon.velocity.listener.ConnectionListener;
import com.gizmo.brennon.velocity.listener.ChatListener;
import com.gizmo.brennon.velocity.manager.BanManager;

import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

@Plugin(
        id = "brennon",
        name = "Brennon",
        version = "1.0-SNAPSHOT",
        authors = {"Gizmo0320"},
        dependencies = {}
)
public class BrennonVelocity {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private BrennonCore core;
    private ConfigManager configManager;
    private VelocityCommandManager commandManager;
    private StaffChatManager staffChatManager;
    private PlayerManager playerManager;
    private ProxyManager proxyManager;
    private BanManager banManager;

    @Inject
    public BrennonVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        // Initialize core first
        this.core = new BrennonCore(dataDirectory);

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.commandManager = new VelocityCommandManager(this);
        this.staffChatManager = new StaffChatManager(this);
        this.playerManager = new PlayerManager(this);
        this.proxyManager = new ProxyManager(this);
        this.banManager = new BanManager(this);

        // Register listeners
        server.getEventManager().register(this, new ChatListener(this));
        server.getEventManager().register(this, new ConnectionListener(this));
        server.getEventManager().register(this, new PlayerListener(this));

        logger.info("Brennon has been enabled!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (core != null) {
            // If BrennonCore needs cleanup, do it here
            logger.info("Shutting down Brennon Core...");
        }
        logger.info("Brennon has been disabled!");
    }

    public void reloadPermissions() {
        // We don't need to explicitly reload LuckPerms permissions
        // Just log that we're refreshing the cache
        getLogger().info("Refreshing permission cache");

        // Clear any local permission caches if we have them
        getServer().getAllPlayers().forEach(player -> {
            try {
                // The PermissionService will automatically fetch fresh data from LuckPerms
                UUID playerId = player.getUniqueId();
                getCore().getPermissionService().hasPermission(playerId, "brennon.reload");
            } catch (Exception e) {
                getLogger().warning("Failed to refresh permissions for " + player.getUsername() + ": " + e.getMessage());
            }
        });
    }

    // Getters for all managers
    public ProxyServer getServer() { return server; }
    public Logger getLogger() { return logger; }
    public Path getDataDirectory() { return dataDirectory; }
    public BrennonCore getCore() { return core; }
    public ConfigManager getConfigManager() { return configManager; }
    public VelocityCommandManager getCommandManager() { return commandManager; }
    public StaffChatManager getStaffChatManager() { return staffChatManager; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public ProxyManager getProxyManager() { return proxyManager; }
    public BanManager getBanManager() { return banManager; }
}