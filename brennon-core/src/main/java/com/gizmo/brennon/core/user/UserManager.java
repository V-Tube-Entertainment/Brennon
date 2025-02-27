package com.gizmo.brennon.core.user;

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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager implements Service {
    private final Logger logger;
    private final DatabaseManager databaseManager;
    private final EventBus eventBus;
    private final MessageBroker messageBroker;
    private final Map<UUID, NetworkUser> onlineUsers;
    private final Gson gson;

    @Inject
    public UserManager(Logger logger, DatabaseManager databaseManager,
                       EventBus eventBus, MessageBroker messageBroker) {
        this.logger = logger;
        this.databaseManager = databaseManager;
        this.eventBus = eventBus;
        this.messageBroker = messageBroker;
        this.onlineUsers = new ConcurrentHashMap<>();
        this.gson = new Gson();
    }

    @Override
    public void enable() throws Exception {
        initializeDatabase();
        setupMessageBroker();
    }

    private void initializeDatabase() throws SQLException {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    uuid CHAR(36) PRIMARY KEY,
                    username VARCHAR(16) NOT NULL,
                    first_join TIMESTAMP NOT NULL,
                    last_join TIMESTAMP NOT NULL,
                    last_seen TIMESTAMP NOT NULL,
                    last_ip VARCHAR(45) NOT NULL,
                    settings JSON NOT NULL,
                    INDEX idx_username (username)
                )
            """);
        }
    }

    private void setupMessageBroker() {
        messageBroker.subscribe("brennon:users", message -> {
            JsonObject data = message.data().getAsJsonObject();
            String action = data.get("action").getAsString();
            UUID uuid = UUID.fromString(data.get("uuid").getAsString());

            switch (action) {
                case "JOIN" -> {
                    String server = data.get("server").getAsString();
                    handleUserJoin(uuid, server);
                }
                case "QUIT" -> handleUserQuit(uuid);
                case "SWITCH" -> {
                    String server = data.get("server").getAsString();
                    handleUserSwitch(uuid, server);
                }
            }
        });
    }

    public NetworkUser handleUserJoin(UUID uuid, String username, String ipAddress, String server) {
        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            NetworkUser user = getOrCreateUser(conn, uuid, username, ipAddress);
            user = user.withServer(server);
            onlineUsers.put(uuid, user);

            // Notify other servers
            JsonObject data = new JsonObject();
            data.addProperty("action", "JOIN");
            data.addProperty("uuid", uuid.toString());
            data.addProperty("server", server);
            messageBroker.publish("brennon:users", data);

            return user;
        } catch (SQLException e) {
            logger.error("Error handling user join", e);
            return null;
        }
    }

    private NetworkUser getOrCreateUser(Connection conn, UUID uuid, String username, String ipAddress)
            throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Update existing user
                try (PreparedStatement updateStmt = conn.prepareStatement(
                        """
                        UPDATE users 
                        SET username = ?, last_join = CURRENT_TIMESTAMP, 
                            last_seen = CURRENT_TIMESTAMP, last_ip = ?
                        WHERE uuid = ?
                        """)) {
                    updateStmt.setString(1, username);
                    updateStmt.setString(2, ipAddress);
                    updateStmt.setString(3, uuid.toString());
                    updateStmt.executeUpdate();
                }

                return new NetworkUser(
                        uuid,
                        username,
                        null,
                        rs.getTimestamp("first_join").toInstant(),
                        Instant.now(),
                        Instant.now(),
                        ipAddress,
                        gson.fromJson(rs.getString("settings"), UserSettings.class)
                );
            } else {
                // Create new user
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        """
                        INSERT INTO users 
                        (uuid, username, first_join, last_join, last_seen, last_ip, settings)
                        VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?)
                        """)) {
                    insertStmt.setString(1, uuid.toString());
                    insertStmt.setString(2, username);
                    insertStmt.setString(3, ipAddress);
                    insertStmt.setString(4, gson.toJson(UserSettings.createDefault()));
                    insertStmt.executeUpdate();
                }

                return new NetworkUser(
                        uuid,
                        username,
                        null,
                        Instant.now(),
                        Instant.now(),
                        Instant.now(),
                        ipAddress,
                        UserSettings.createDefault()
                );
            }
        }
    }

    public void handleUserQuit(UUID uuid) {
        NetworkUser user = onlineUsers.remove(uuid);
        if (user != null) {
            try (Connection conn = databaseManager.getDataSource().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE users SET last_seen = CURRENT_TIMESTAMP WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();

                // Notify other servers
                JsonObject data = new JsonObject();
                data.addProperty("action", "QUIT");
                data.addProperty("uuid", uuid.toString());
                messageBroker.publish("brennon:users", data);
            } catch (SQLException e) {
                logger.error("Error handling user quit", e);
            }
        }
    }

    public void handleUserSwitch(UUID uuid, String newServer) {
        NetworkUser user = onlineUsers.get(uuid);
        if (user != null) {
            onlineUsers.put(uuid, user.withServer(newServer));

            // Notify other servers
            JsonObject data = new JsonObject();
            data.addProperty("action", "SWITCH");
            data.addProperty("uuid", uuid.toString());
            data.addProperty("server", newServer);
            messageBroker.publish("brennon:users", data);
        }
    }

    private void handleMessage(String channel, String message) {
        try {
            String[] parts = message.split(":");
            if (parts.length < 2) return;

            UUID uuid = UUID.fromString(parts[0]);
            String action = parts[1];

            switch (action) {
                case "JOIN" -> {
                    if (parts.length >= 5) {
                        String username = parts[2];
                        String ipAddress = parts[3];
                        String server = parts[4];
                        handleUserJoin(uuid, username, ipAddress, server);
                    }
                }
                case "QUIT" -> handleUserQuit(uuid);
                default -> logger.warn("Unknown user action: {}", action);
            }
        } catch (Exception e) {
            logger.error("Error handling user message", e);
        }
    }

    public NetworkUser getUser(UUID uuid) {
        return onlineUsers.get(uuid);
    }

    public boolean isOnline(UUID uuid) {
        return onlineUsers.containsKey(uuid);
    }

    public Map<UUID, NetworkUser> getOnlineUsers() {
        return Map.copyOf(onlineUsers);
    }

    @Override
    public void disable() throws Exception {
        // Cleanup if needed
    }
}
