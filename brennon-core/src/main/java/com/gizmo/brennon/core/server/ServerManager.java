package com.gizmo.brennon.core.server;

import com.google.inject.Inject;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.event.EventBus;
import com.gizmo.brennon.core.messaging.MessageBroker;
import com.gizmo.brennon.core.service.Service;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;

import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerManager implements Service {
    private final Logger logger;
    private final DatabaseManager databaseManager;
    private final EventBus eventBus;
    private final MessageBroker messageBroker;
    private final Map<String, ServerInfo> servers;
    private final Gson gson;
    private ScheduledExecutorService executor;

    @Inject
    public ServerManager(Logger logger, DatabaseManager databaseManager,
                         EventBus eventBus, MessageBroker messageBroker) {
        this.logger = logger;
        this.databaseManager = databaseManager;
        this.eventBus = eventBus;
        this.messageBroker = messageBroker;
        this.servers = new ConcurrentHashMap<>();
        this.gson = new Gson();
    }

    @Override
    public void enable() throws Exception {
        initializeDatabase();
        setupMessageBroker();
        startMaintenanceTask();
    }

    @Override
    public void disable() throws Exception {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    private void initializeDatabase() throws SQLException {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS servers (
                    id VARCHAR(64) PRIMARY KEY,
                    name VARCHAR(64) NOT NULL,
                    type VARCHAR(32) NOT NULL,
                    server_group VARCHAR(32) NOT NULL,
                    host VARCHAR(255) NOT NULL,
                    port INT NOT NULL,
                    restricted BOOLEAN NOT NULL DEFAULT FALSE,
                    properties JSON,
                    INDEX idx_group (server_group)
                )
            """);
        }
    }

    private void setupMessageBroker() {
        messageBroker.subscribe("brennon:servers", message -> {
            JsonObject data = message.data().getAsJsonObject();
            String action = data.get("action").getAsString();
            String serverId = data.get("serverId").getAsString();

            switch (action) {
                case "UPDATE" -> {
                    ServerInfo serverInfo = gson.fromJson(data.get("info"), ServerInfo.class);
                    servers.put(serverId, serverInfo);
                }
                case "OFFLINE" -> {
                    ServerInfo current = servers.get(serverId);
                    if (current != null) {
                        servers.put(serverId, new ServerInfo(
                                current.id(),
                                current.name(),
                                current.type(),
                                current.group(),
                                current.host(),
                                current.port(),
                                current.restricted(),
                                ServerStatus.OFFLINE,
                                current.properties(),
                                current.maxPlayers(),
                                0,
                                0.0,
                                0.0,
                                0L,
                                0L,
                                Instant.now()
                        ));
                    }
                }
            }
        });
    }

    private void startMaintenanceTask() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            Instant threshold = Instant.now().minusSeconds(30);
            servers.forEach((id, server) -> {
                if (server.lastUpdate().isBefore(threshold) && server.status() != ServerStatus.OFFLINE) {
                    ServerInfo offlineServer = new ServerInfo(
                            server.id(),
                            server.name(),
                            server.type(),
                            server.group(),
                            server.host(),
                            server.port(),
                            server.restricted(),
                            ServerStatus.OFFLINE,
                            server.properties(),
                            server.maxPlayers(),
                            0,
                            0.0,
                            0.0,
                            0L,
                            0L,
                            Instant.now()
                    );
                    servers.put(id, offlineServer);

                    // Notify other servers
                    JsonObject data = new JsonObject();
                    data.addProperty("action", "OFFLINE");
                    data.addProperty("serverId", id);
                    messageBroker.publish("brennon:servers", data);
                }
            });
        }, 15, 15, TimeUnit.SECONDS);
    }

    public void registerServer(String id, String name, String type,
