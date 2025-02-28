package com.gizmo.brennon.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.gizmo.brennon.core.BrennonCore;
import com.gizmo.brennon.velocity.listener.ConnectionListener;
import com.gizmo.brennon.velocity.listener.ServerListener;
import com.gizmo.brennon.velocity.manager.ProxyManager;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "brennon",
        name = "Brennon",
        version = "1.0.0-SNAPSHOT",
        description = "Network Management System for Velocity",
        authors = {"Gizmo0320"}
)
public class BrennonVelocity {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private BrennonCore core;
    private ProxyManager proxyManager;

    @Inject
    public BrennonVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Initialize BrennonCore
        core = new BrennonCore(dataDirectory);
        core.start().thenRun(() -> {
            // Initialize proxy manager
            proxyManager = new ProxyManager(server, core);

            // Register listeners
            server.getEventManager().register(this, new ConnectionListener(this));
            server.getEventManager().register(this, new ServerListener(core, proxyManager));

            logger.info("Brennon-Velocity has been enabled!");
        }).exceptionally(throwable -> {
            logger.error("Failed to initialize Brennon-Velocity", throwable);
            return null;
        });
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (core != null) {
            core.stop().thenRun(() ->
                    logger.info("Brennon-Velocity has been disabled!")
            ).exceptionally(throwable -> {
                logger.error("Error while disabling Brennon-Velocity", throwable);
                return null;
            });
        }
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public BrennonCore getCore() {
        return core;
    }

    public ProxyManager getProxyManager() {
        return proxyManager;
    }
}
