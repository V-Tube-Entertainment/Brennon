package com.gizmo.brennon.core.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.gizmo.brennon.core.config.CoreConfig;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.redis.RedisManager;
import com.gizmo.brennon.core.service.ServiceRegistry;
import com.gizmo.brennon.core.punishment.PunishmentService;
import com.gizmo.brennon.core.ticket.TicketService;
import com.gizmo.brennon.core.announcement.AnnouncementService;
import com.gizmo.brennon.core.event.EventBus;
import com.gizmo.brennon.core.messaging.MessageBroker;

import java.util.UUID;

public class CoreModule extends AbstractModule {
    private final String serverId;

    public CoreModule() {
        this.serverId = UUID.randomUUID().toString();
    }

    @Override
    protected void configure() {
        bind(CoreConfig.class).in(Singleton.class);
        bind(DatabaseManager.class).in(Singleton.class);
        bind(RedisManager.class).in(Singleton.class);
        bind(ServiceRegistry.class).in(Singleton.class);
        bind(PunishmentService.class).in(Singleton.class);
        bind(TicketService.class).in(Singleton.class);
        bind(AnnouncementService.class).in(Singleton.class);
        bind(EventBus.class).in(Singleton.class);
        bind(MessageBroker.class).in(Singleton.class);

        // Bind server ID
        bind(String.class).annotatedWith(ServerId.class).toInstance(serverId);
    }

    @Provides
    @Singleton
    MessageBroker provideMessageBroker(Logger logger, RedisManager redisManager) {
        return new MessageBroker(logger, redisManager, serverId);
    }
}
