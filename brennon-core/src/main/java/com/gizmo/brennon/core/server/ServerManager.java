package com.gizmo.brennon.core.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.messaging.MessageBroker;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServerManager implements Service {
    private final Logger logger;
    private final DatabaseManager databaseManager;
    private final MessageBroker messageBroker;
    private final Map<String, ServerInfo> servers;
    private final Gson gson;

    @Inject
    public ServerManager(Logger logger, DatabaseManager databaseManager, MessageBroker messageBroker) {
        this.logger = logger;
        this.databaseManager = databaseManager;
        this.messageBroker = messageBroker;
        this.servers = new ConcurrentHashMap<>();
        this.gson = new Gson();
    }

    @Override
    public void enable() throws Exception {
        initializeDatabase();
        loadServers();
        setupMessageBroker();
    }

    @Override
    public void disable() throws Exception {
        servers.clear();
    }

    private void initializeDatabase() throws SQLException {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS servers (
                    id VARCHAR(64) PRIMARY KEY,
                    name VARCHAR(64) NOT NULL,
                    type VARCHAR(32) NOT NULL,
                    server_group VARCHAR(64),
                    host VARCHAR(255) NOT NULL,
                    port INT NOT NULL,
                    restricted BOOLEAN NOT NULL DEFAULT FALSE,
                    properties JSON NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )""")) {
            stmt.executeUpdate();
        }
    }

    private void loadServers() {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM servers");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ServerInfo server = new ServerInfo(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("server_group"),
                        rs.getString("host"),
                        rs.getInt("port"),
                        rs.getBoolean("restricted"),
                        ServerStatus.OFFLINE,
                        gson.fromJson(rs.getString("properties"), Map.class),
                        100, // Max players
                        0,   // Online players
                        20.0, // TPS
                        0.0, // Memory usage
                        0.0, // CPU usage
                        0L,  // Last heartbeat
                        0L,  // Uptime
                        rs.getTimestamp("created_at").toInstant()
                );
                servers.put(server.id(), server);
            }
        } catch (SQLException e) {
            logger.error("Failed to load servers from database", e);
        }
    }

    private void setupMessageBroker() {
        messageBroker.subscribe("brennon:servers", message -> {
            try {
                JsonObject data = gson.fromJson(message, JsonObject.class);
                String type = data.get("type").getAsString();
                String serverId = data.get("serverId").getAsString();

                switch (type) {
                    case "HEARTBEAT" -> handleHeartbeat(data);
                    case "STATUS" -> handleStatusUpdate(data);
                    case "SHUTDOWN" -> handleServerShutdown(serverId);
                    default -> logger.warn("Unknown server message type: {}", type);
                }
            } catch (Exception e) {
                logger.error("Error handling server message", e);
            }
        });
    }

    private void handleHeartbeat(JsonObject data) {
        String serverId = data.get("serverId").getAsString();
        ServerInfo server = servers.get(serverId);

        if (server != null) {
            ServerInfo updatedServer = new ServerInfo(
                    server.id(),
                    server.name(),
                    server.type(),
                    server.group(),
                    server.host(),
                    server.port(),
                    server.restricted(),
                    ServerStatus.ONLINE,
                    server.properties(),
                    data.get("maxPlayers").getAsInt(),
                    data.get("onlinePlayers").getAsInt(),
                    data.get("tps").getAsDouble(),
                    data.get("memoryUsage").getAsDouble(),
                    data.get("cpuUsage").getAsDouble(),
                    System.currentTimeMillis(),
                    data.get("uptime").getAsLong(),
                    server.createdAt()
            );
            servers.put(serverId, updatedServer);
        }
    }

    private void handleStatusUpdate(JsonObject data) {
        String serverId = data.get("serverId").getAsString();
        ServerInfo server = servers.get(serverId);

        if (server != null) {
            ServerStatus newStatus = ServerStatus.valueOf(data.get("status").getAsString());
            ServerInfo updatedServer = new ServerInfo(
                    server.id(),
                    server.name(),
                    server.type(),
                    server.group(),
                    server.host(),
                    server.port(),
                    server.restricted(),
                    newStatus,
                    server.properties(),
                    server.maxPlayers(),
                    server.onlinePlayers(),
                    server.tps(),
                    server.memoryUsage(),
                    server.cpuUsage(),
                    server.lastHeartbeat(),
                    server.uptime(),
                    server.createdAt()
            );
            servers.put(serverId, updatedServer);
        }
    }

    private void handleServerShutdown(String serverId) {
        ServerInfo server = servers.get(serverId);
        if (server != null) {
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
                    0.0,
                    0L,
                    0L,
                    server.createdAt()
            );
            servers.put(serverId, offlineServer);
        }
    }

    public Optional<ServerInfo> getServer(String id) {
        return Optional.ofNullable(servers.get(id));
    }

    public Collection<ServerInfo> getAllServers() {
        return Collections.unmodifiableCollection(servers.values());
    }

    public Collection<ServerInfo> getServersByGroup(String group) {
        return servers.values().stream()
                .filter(server -> group.equals(server.group()))
                .collect(Collectors.toUnmodifiableList());
    }

    public Collection<ServerInfo> getServersByType(String type) {
        return servers.values().stream()
                .filter(server -> type.equals(server.type()))
                .collect(Collectors.toUnmodifiableList());
    }

    public Collection<ServerInfo> getOnlineServers() {
        return servers.values().stream()
                .filter(ServerInfo::isOnline)
                .collect(Collectors.toUnmodifiableList());
    }

    public void registerServer(String id, String name, String type, String group,
                               String host, int port, boolean restricted) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO servers 
                (id, name, type, server_group, host, port, restricted, properties)
                VALUES (?, ?, ?, ?, ?, ?, ?, '{}')
                """)) {

            stmt.setString(1, id);
            stmt.setString(2, name);
            stmt.setString(3, type);
            stmt.setString(4, group);
            stmt.setString(5, host);
            stmt.setInt(6, port);
            stmt.setBoolean(7, restricted);

            stmt.executeUpdate();

            ServerInfo server = new ServerInfo(
                    id,
                    name,
                    type,
                    group,
                    host,
                    port,
                    restricted,
                    ServerStatus.OFFLINE,
                    Map.of(),
                    100,
                    0,
                    20.0,
                    0.0,
                    0.0,
                    0L,
                    0L,
                    Instant.now()
            );

            servers.put(id, server);

            // Notify other servers
            JsonObject data = new JsonObject();
            data.addProperty("type", "SERVER_REGISTERED");
            data.addProperty("serverId", id);
            messageBroker.publish("brennon:servers", data);
        } catch (SQLException e) {
            logger.error("Failed to register server", e);
        }
    }

    public void unregisterServer(String id) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM servers WHERE id = ?")) {

            stmt.setString(1, id);
            stmt.executeUpdate();
            servers.remove(id);

            // Notify other servers
            JsonObject data = new JsonObject();
            data.addProperty("type", "SERVER_UNREGISTERED");
            data.addProperty("serverId", id);
            messageBroker.publish("brennon:servers", data);
        } catch (SQLException e) {
            logger.error("Failed to unregister server", e);
        }
    }

    public void updateServerGroup(String serverId, String newGroup) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE servers SET server_group = ? WHERE id = ?")) {

            stmt.setString(1, newGroup);
            stmt.setString(2, serverId);

            if (stmt.executeUpdate() > 0) {
                ServerInfo existingServer = servers.get(serverId);
                if (existingServer != null) {
                    ServerInfo updatedServer = new ServerInfo(
                            existingServer.id(),
                            existingServer.name(),
                            existingServer.type(),
                            newGroup,
                            existingServer.host(),
                            existingServer.port(),
                            existingServer.restricted(),
                            existingServer.status(),
                            existingServer.properties(),
                            existingServer.maxPlayers(),
                            existingServer.onlinePlayers(),
                            existingServer.tps(),
                            existingServer.memoryUsage(),
                            existingServer.cpuUsage(),
                            existingServer.lastHeartbeat(),
                            existingServer.uptime(),
                            existingServer.createdAt()
                    );
                    servers.put(serverId, updatedServer);

                    // Notify other servers
                    JsonObject data = new JsonObject();
                    data.addProperty("type", "SERVER_GROUP_UPDATED");
                    data.addProperty("serverId", serverId);
                    data.addProperty("group", newGroup);
                    messageBroker.publish("brennon:servers", data);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to update server group", e);
        }
    }

    public void updateServerProperties(String serverId, Map<String, String> properties) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE servers SET properties = ? WHERE id = ?")) {

            stmt.setString(1, gson.toJson(properties));
            stmt.setString(2, serverId);

            if (stmt.executeUpdate() > 0) {
                ServerInfo existingServer = servers.get(serverId);
                if (existingServer != null) {
                    ServerInfo updatedServer = new ServerInfo(
                            existingServer.id(),
                            existingServer.name(),
                            existingServer.type(),
                            existingServer.group(),
                            existingServer.host(),
                            existingServer.port(),
                            existingServer.restricted(),
                            existingServer.status(),
                            new HashMap<>(properties), // Create new map to ensure immutability
                            existingServer.maxPlayers(),
                            existingServer.onlinePlayers(),
                            existingServer.tps(),
                            existingServer.memoryUsage(),
                            existingServer.cpuUsage(),
                            existingServer.lastHeartbeat(),
                            existingServer.uptime(),
                            existingServer.createdAt()
                    );
                    servers.put(serverId, updatedServer);

                    // Notify other servers
                    JsonObject data = new JsonObject();
                    data.addProperty("type", "SERVER_PROPERTIES_UPDATED");
                    data.addProperty("serverId", serverId);
                    data.add("properties", gson.toJsonTree(properties));
                    messageBroker.publish("brennon:servers", data);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to update server properties", e);
        }
    }

    public boolean isServerOnline(String serverId) {
        return getServer(serverId)
                .map(ServerInfo::isOnline)
                .orElse(false);
    }

    public int getOnlineCount() {
        return (int) servers.values().stream()
                .filter(ServerInfo::isOnline)
                .count();
    }

    public boolean hasAvailableServers(String group) {
        return getServersByGroup(group).stream()
                .anyMatch(server -> server.isOnline() && !server.isFull());
    }
}