package com.gizmo.brennon.core;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.gizmo.brennon.core.config.CoreConfig;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.redis.RedisManager;
import com.gizmo.brennon.core.platform.Platform;
import com.gizmo.brennon.core.service.ServiceRegistry;
import org.slf4j.Logger;

public class BrennonCore {
    private final Logger logger;
    private final Injector injector;
    private final CoreConfig config;
    private final DatabaseManager databaseManager;
    private final RedisManager redisManager;
    private final ServiceRegistry serviceRegistry;
    private final Platform platform;

    @Inject
    public BrennonCore(
            Logger logger,
            Injector injector,
            CoreConfig config,
            DatabaseManager databaseManager,
            RedisManager redisManager,
            ServiceRegistry serviceRegistry,
            Platform platform
    ) {
        this.logger = logger;
        this.injector = injector;
        this.config = config;
        this.databaseManager = databaseManager;
        this.redisManager = redisManager;
        this.serviceRegistry = serviceRegistry;
        this.platform = platform;
    }

    public void enable() {
        logger.info("Enabling Brennon Core...");
        try {
            config.load();
            databaseManager.initialize();
            redisManager.initialize();
            serviceRegistry.enableServices();
            logger.info("Brennon Core has been enabled successfully!");
        } catch (Exception e) {
            logger.error("Failed to enable Brennon Core!", e);
        }
    }

    public void disable() {
        logger.info("Disabling Brennon Core...");
        try {
            serviceRegistry.disableServices();
            redisManager.shutdown();
            databaseManager.shutdown();
            logger.info("Brennon Core has been disabled successfully!");
        } catch (Exception e) {
            logger.error("Failed to disable Brennon Core properly!", e);
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public Injector getInjector() {
        return injector;
    }

    public CoreConfig getConfig() {
        return config;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public Platform getPlatform() {
        return platform;
    }
}
