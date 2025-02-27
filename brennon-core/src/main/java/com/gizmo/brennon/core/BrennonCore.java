package com.gizmo.brennon.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.gizmo.brennon.core.announcement.AnnouncementService;
import com.gizmo.brennon.core.balancing.LoadBalancer;
import com.gizmo.brennon.core.command.CommandManager;
import com.gizmo.brennon.core.config.ConfigurationManager;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.logging.NetworkLogger;
import com.gizmo.brennon.core.messaging.MessageBroker;
import com.gizmo.brennon.core.module.CoreModule;
import com.gizmo.brennon.core.monitoring.NetworkMonitor;
import com.gizmo.brennon.core.permission.PermissionService;
import com.gizmo.brennon.core.punishment.PunishmentService;
import com.gizmo.brennon.core.redis.RedisManager;
import com.gizmo.brennon.core.server.ServerGroupManager;
import com.gizmo.brennon.core.server.ServerManager;
import com.gizmo.brennon.core.service.Service;
import com.gizmo.brennon.core.ticket.TicketService;
import com.gizmo.brennon.core.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BrennonCore {
    private final Logger logger;
    private final Path dataDirectory;
    private final List<Service> services;
    private final ExecutorService executorService;
    private Injector injector;

    // Core services
    private ConfigurationManager configManager;
    private DatabaseManager databaseManager;
    private RedisManager redisManager;
    private MessageBroker messageBroker;
    private NetworkLogger networkLogger;

    // Feature services
    private CommandManager commandManager;
    private ServerManager serverManager;
    private ServerGroupManager serverGroupManager;
    private UserManager userManager;
    private PermissionService permissionService;
    private PunishmentService punishmentService;
    private TicketService ticketService;
    private AnnouncementService announcementService;
    private NetworkMonitor networkMonitor;
    private LoadBalancer loadBalancer;

    public BrennonCore(Path dataDirectory) {
        this.logger = LoggerFactory.getLogger(BrennonCore.class);
        this.dataDirectory = dataDirectory;
        this.services = new ArrayList<>();
        this.executorService = Executors.newCachedThreadPool();
    }

    public CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Starting Brennon Core...");
                long startTime = System.currentTimeMillis();

                // Create and configure injector
                injector = Guice.createInjector(new CoreModule(dataDirectory));

                // Initialize core services first
                initializeCoreServices();

                // Initialize feature services
                initializeFeatureServices();

                // Enable all services in order
                enableServices();

                long endTime = System.currentTimeMillis();
                logger.info("Brennon Core started successfully! ({}ms)", endTime - startTime);
            } catch (Exception e) {
                logger.error("Failed to start Brennon Core", e);
                throw new RuntimeException("Failed to start Brennon Core", e);
            }
        }, executorService);
    }

    public CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Stopping Brennon Core...");
                long startTime = System.currentTimeMillis();

                // Disable services in reverse order
                disableServices();

                // Shutdown executor
                executorService.shutdown();

                long endTime = System.currentTimeMillis();
                logger.info("Brennon Core stopped successfully! ({}ms)", endTime - startTime);
            } catch (Exception e) {
                logger.error("Failed to stop Brennon Core", e);
                throw new RuntimeException("Failed to stop Brennon Core", e);
            }
        }, executorService);
    }

    private void initializeCoreServices() {
        logger.info("Initializing core services...");

        // Core infrastructure
        configManager = injector.getInstance(ConfigurationManager.class);
        services.add(configManager);

        databaseManager = injector.getInstance(DatabaseManager.class);
        services.add(databaseManager);

        redisManager = injector.getInstance(RedisManager.class);
        services.add(redisManager);

        messageBroker = injector.getInstance(MessageBroker.class);
        services.add(messageBroker);

        networkLogger = injector.getInstance(NetworkLogger.class);
        services.add(networkLogger);
    }

    private void initializeFeatureServices() {
        logger.info("Initializing feature services...");

        // Server management
        serverManager = injector.getInstance(ServerManager.class);
        services.add(serverManager);

        serverGroupManager = injector.getInstance(ServerGroupManager.class);
        services.add(serverGroupManager);

        // User management
        userManager = injector.getInstance(UserManager.class);
        services.add(userManager);

        permissionService = injector.getInstance(PermissionService.class);
        services.add(permissionService);

        // Feature services
        commandManager = injector.getInstance(CommandManager.class);
        services.add(commandManager);

        punishmentService = injector.getInstance(PunishmentService.class);
        services.add(punishmentService);

        ticketService = injector.getInstance(TicketService.class);
        services.add(ticketService);

        announcementService = injector.getInstance(AnnouncementService.class);
        services.add(announcementService);

        // Monitoring and load balancing
        networkMonitor = injector.getInstance(NetworkMonitor.class);
        services.add(networkMonitor);

        loadBalancer = injector.getInstance(LoadBalancer.class);
        services.add(loadBalancer);
    }

    private void enableServices() {
        logger.info("Enabling services...");

        for (Service service : services) {
            try {
                String serviceName = service.getClass().getSimpleName();
                logger.debug("Enabling {}...", serviceName);
                service.enable();
                logger.debug("{} enabled successfully", serviceName);
            } catch (Exception e) {
                logger.error("Failed to enable service: " + service.getClass().getSimpleName(), e);
                throw new RuntimeException("Failed to enable service: " + service.getClass().getSimpleName(), e);
            }
        }
    }

    private void disableServices() {
        logger.info("Disabling services...");

        // Disable in reverse order
        for (int i = services.size() - 1; i >= 0; i--) {
            Service service = services.get(i);
            try {
                String serviceName = service.getClass().getSimpleName();
                logger.debug("Disabling {}...", serviceName);
                service.disable();
                logger.debug("{} disabled successfully", serviceName);
            } catch (Exception e) {
                logger.error("Failed to disable service: " + service.getClass().getSimpleName(), e);
            }
        }
    }

    // Getters for services
    public ConfigurationManager getConfigManager() { return configManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public RedisManager getRedisManager() { return redisManager; }
    public MessageBroker getMessageBroker() { return messageBroker; }
    public NetworkLogger getNetworkLogger() { return networkLogger; }
    public CommandManager getCommandManager() { return commandManager; }
    public ServerManager getServerManager() { return serverManager; }
    public ServerGroupManager getServerGroupManager() { return serverGroupManager; }
    public UserManager getUserManager() { return userManager; }
    public PermissionService getPermissionService() { return permissionService; }
    public PunishmentService getPunishmentService() { return punishmentService; }
    public TicketService getTicketService() { return ticketService; }
    public AnnouncementService getAnnouncementService() { return announcementService; }
    public NetworkMonitor getNetworkMonitor() { return networkMonitor; }
    public LoadBalancer getLoadBalancer() { return loadBalancer; }
    public Injector getInjector() { return injector; }

    // Version info
    public static String getVersion() {
        return "1.0.0";
    }

    public static String getBuildDate() {
        return "2025-02-27";
    }
}
