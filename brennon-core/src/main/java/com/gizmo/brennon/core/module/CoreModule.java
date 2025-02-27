package com.gizmo.brennon.core.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.gizmo.brennon.core.config.ConfigurationManager;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.redis.RedisManager;
import com.gizmo.brennon.core.service.ServiceRegistry;
import com.gizmo.brennon.core.punishment.PunishmentService;
import com.gizmo.brennon.core.ticket.TicketService;
import com.gizmo.brennon.core.announcement.AnnouncementService;
import com.gizmo.brennon.core.event.EventBus;
import com.gizmo.brennon.core.messaging.MessageBroker;
import com.gizmo.brennon.core.logging.NetworkLogger;
import com.gizmo.brennon.core.balancing.LoadBalancer;
import com.gizmo.brennon.core.scheduler.TaskScheduler;
import com.gizmo.brennon.core.command.CommandManager;
import org.slf4j.Logger;

import java.nio.file.Path;

public class CoreModule extends AbstractModule {
    private final Path dataDirectory;
    private final String serverId;

    public CoreModule(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.serverId = java.util.UUID.randomUUID().toString();
    }

    @Override
    protected void configure() {
        bind(String.class).annotatedWith(ServerId.class).toInstance(serverId);
        bind(Path.class).annotatedWith(DataDirectory.class).toInstance(dataDirectory);

        // Core Services
        bind(ServiceRegistry.class).in(Singleton.class);
        bind(DatabaseManager.class).in(Singleton.class);
        bind(RedisManager.class).in(Singleton.class);
        bind(EventBus.class).in(Singleton.class);
        bind(MessageBroker.class).in(Singleton.class);
        bind(ConfigurationManager.class).in(Singleton.class);
        bind(TaskScheduler.class).in(Singleton.class);
        bind(CommandManager.class).in(Singleton.class);

        // Feature Services
        bind(PunishmentService.class).in(Singleton.class);
        bind(TicketService.class).in(Singleton.class);
        bind(AnnouncementService.class).in(Singleton.class);
        bind(NetworkLogger.class).in(Singleton.class);
        bind(LoadBalancer.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    Path configDirectory(@DataDirectory Path dataDir) {
        return dataDir.resolve("config");
    }
}
