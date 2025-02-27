package com.gizmo.brennon.core.user;

import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.messaging.MessageBroker;
import com.gizmo.brennon.core.service.Service;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UserManager implements Service {
    private final Logger logger;
    private final DatabaseManager databaseManager;
    private final MessageBroker messageBroker;
    private final Map<UUID, UserInfo> users;
    private final Gson gson;

    @Inject
    public UserManager(Logger logger, DatabaseManager databaseManager, MessageBroker messageBroker) {
        this.logger = logger;
        this.databaseManager = databaseManager;
        this.messageBroker = messageBroker;
        this.users = new ConcurrentHashMap<>();
        this.gson = new Gson();
    }

    @Override
    public void enable() throws Exception {
        initializeDatabase();
        loadOnlineUsers();
        setupMessageBroker();
    }

    @Override
    public void disable() throws Exception {
        // Mark all users as offline when shutting down
        for (UUID uuid : new ArrayList<>(users.keySet())) {
            handleUserQuit(uuid);
        }
        users.clear();
    }

    private void initializeDatabase() throws SQLException {
        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            // Create users table
            try (PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS users (
                    uuid CHAR(36) PRIMARY KEY,
                    username VARCHAR(16) NOT NULL,
                    display_name VARCHAR(48),
                    locale VARCHAR(10) DEFAULT 'en_US',
                    first_seen TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    last_seen TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    metadata JSON NOT NULL DEFAULT '{}',
                    properties JSON NOT NULL DEFAULT '{}'
                )""")) {
                stmt.executeUpdate();
            }

            // Create sessions table
            try (PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS user_sessions (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_uuid CHAR(36) NOT NULL,
                    server_id VARCHAR(64) NOT NULL,
                    ip_address VARCHAR(45) NOT NULL,
                    status VARCHAR(16) NOT NULL DEFAULT 'ONLINE',
                    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    left_at TIMESTAMP,
                    FOREIGN KEY (user_uuid) REFERENCES users(uuid)
                )""")) {
                stmt.executeUpdate();
            }
        }
    }

    private void loadOnlineUsers() {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT 
                    u.*, 
                    s.server_id, 
                    s.ip_address,
                    s.status
                FROM users u
                JOIN user_sessions s ON u.uuid = s.user_uuid
                WHERE s.left_at IS NULL
                """);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                UserInfo userInfo = new UserInfo(
                        uuid,
                        rs.getString("username"),
                        rs.getString("display_name"),
                        rs.getString("server_id"),
                        UserStatus.valueOf(rs.getString("status")),
                        rs.getString("ip_address"),
                        rs.getString("locale"),
                        gson.fromJson(rs.getString("metadata"), Map.class),
                        gson.fromJson(rs.getString("properties"), Map.class),
                        rs.getTimestamp("first_seen").toInstant(),
                        rs.getTimestamp("last_seen").toInstant()
                );
                users.put(uuid, userInfo);
            }
        } catch (SQLException e) {
            logger.error("Failed to load online users", e);
        }
    }

    private void setupMessageBroker() {
        messageBroker.subscribe("brennon:users", message -> {
            try {
                JsonObject data = gson.fromJson(message, JsonObject.class);
                String type = data.get("type").getAsString();
                UUID uuid = UUID.fromString(data.get("uuid").getAsString());

                switch (type) {
                    case "JOIN" -> {
                        String username = data.get("username").getAsString();
                        String ipAddress = data.get("ipAddress").getAsString();
                        String server = data.get("server").getAsString();
                        handleUserJoin(uuid, username, ipAddress, server);
                    }
                    case "QUIT" -> handleUserQuit(uuid);
                    case "SWITCH" -> {
                        String server = data.get("server").getAsString();
                        handleUserSwitch(uuid, server);
                    }
                    case "STATUS" -> {
                        UserStatus status = UserStatus.valueOf(data.get("status").getAsString());
                        updateUserStatus(uuid, status);
                    }
                    default -> logger.warn("Unknown user action: {}", type);
                }
            } catch (Exception e) {
                logger.error("Error handling user message", e);
            }
        });
    }

    public void handleUserJoin(UUID uuid, String username, String ipAddress, String serverId) {
        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            Instant now = Instant.now();

            // Update or insert user
            try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO users (uuid, username, display_name, first_seen, last_seen)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE 
                    username = ?,
                    last_seen = ?
                """)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, username);
                stmt.setString(3, username);
                stmt.setObject(4, now);
                stmt.setObject(5, now);
                stmt.setString(6, username);
                stmt.setObject(7, now);
                stmt.executeUpdate();
            }

            // Create new session
            try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO user_sessions (user_uuid, server_id, ip_address, status)
                VALUES (?, ?, ?, ?)
                """)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, serverId);
                stmt.setString(3, ipAddress);
                stmt.setString(4, UserStatus.ONLINE.name());
                stmt.executeUpdate();
            }

            // Update in-memory cache
            UserInfo userInfo = new UserInfo(
                    uuid,
                    username,
                    username,
                    serverId,
                    UserStatus.ONLINE,
                    ipAddress,
                    "en_US",
                    new HashMap<>(),
                    new HashMap<>(),
                    now,
                    now
            );
            users.put(uuid, userInfo);

            // Notify other servers
            JsonObject data = new JsonObject();
            data.addProperty("type", "USER_JOINED");
            data.addProperty("uuid", uuid.toString());
            data.addProperty("username", username);
            data.addProperty("server", serverId);
            messageBroker.publish("brennon:users", data);
        } catch (SQLException e) {
            logger.error("Failed to handle user join", e);
        }
    }

    public void handleUserQuit(UUID uuid) {
        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            Instant now = Instant.now();

            // Close active session
            try (PreparedStatement stmt = conn.prepareStatement("""
                UPDATE user_sessions 
                SET left_at = ?, status = ?
                WHERE user_uuid = ? AND left_at IS NULL
                """)) {
                stmt.setObject(1, now);
                stmt.setString(2, UserStatus.OFFLINE.name());
                stmt.setString(3, uuid.toString());
                stmt.executeUpdate();
            }

            // Update last seen
            try (PreparedStatement stmt = conn.prepareStatement("""
                UPDATE users 
                SET last_seen = ?
                WHERE uuid = ?
                """)) {
                stmt.setObject(1, now);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            }

            // Remove from cache
            users.remove(uuid);

            // Notify other servers
            JsonObject data = new JsonObject();
            data.addProperty("type", "USER_QUIT");
            data.addProperty("uuid", uuid.toString());
            messageBroker.publish("brennon:users", data);
        } catch (SQLException e) {
            logger.error("Failed to handle user quit", e);
        }
    }

    public void handleUserSwitch(UUID uuid, String newServer) {
        UserInfo user = users.get(uuid);
        if (user == null) return;

        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            Instant now = Instant.now();

            // Close current session
            try (PreparedStatement stmt = conn.prepareStatement("""
                UPDATE user_sessions 
                SET left_at = ?
                WHERE user_uuid = ? AND left_at IS NULL
                """)) {
                stmt.setObject(1, now);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            }

            // Create new session
            try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO user_sessions (user_uuid, server_id, ip_address, status)
                VALUES (?, ?, ?, ?)
                """)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, newServer);
                stmt.setString(3, user.ipAddress());
                stmt.setString(4, user.status().name());
                stmt.executeUpdate();
            }

            // Update cache
            UserInfo updatedUser = new UserInfo(
                    user.uuid(),
                    user.username(),
                    user.displayName(),
                    newServer,
                    user.status(),
                    user.ipAddress(),
                    user.locale(),
                    user.metadata(),
                    user.properties(),
                    user.firstJoin(),
                    now
            );
            users.put(uuid, updatedUser);

            // Notify other servers
            JsonObject data = new JsonObject();
            data.addProperty("type", "USER_SWITCHED");
            data.addProperty("uuid", uuid.toString());
            data.addProperty("server", newServer);
            messageBroker.publish("brennon:users", data);
        } catch (SQLException e) {
            logger.error("Failed to handle user switch", e);
        }
    }

    public void updateUserStatus(UUID uuid, UserStatus newStatus) {
        UserInfo user = users.get(uuid);
        if (user == null) return;

        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            // Update current session status
            try (PreparedStatement stmt = conn.prepareStatement("""
                UPDATE user_sessions 
                SET status = ?
                WHERE user_uuid = ? AND left_at IS NULL
                """)) {
                stmt.setString(1, newStatus.name());
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            }

            // Update cache
            UserInfo updatedUser = new UserInfo(
                    user.uuid(),
                    user.username(),
                    user.displayName(),
                    user.currentServer(),
                    newStatus,
                    user.ipAddress(),
                    user.locale(),
                    user.metadata(),
                    user.properties(),
                    user.firstJoin(),
                    user.lastSeen()
            );
            users.put(uuid, updatedUser);

            // Notify other servers
            JsonObject data = new JsonObject();
            data.addProperty("type", "STATUS_CHANGED");
            data.addProperty("uuid", uuid.toString());
            data.addProperty("status", newStatus.name());
            messageBroker.publish("brennon:users", data);
        } catch (SQLException e) {
            logger.error("Failed to update user status", e);
        }
    }

    public Optional<UserInfo> getUser(UUID uuid) {
        return Optional.ofNullable(users.get(uuid));
    }

    public Collection<UserInfo> getOnlineUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    public Collection<UserInfo> getUsersByServer(String serverId) {
        return users.values().stream()
                .filter(user -> serverId.equals(user.currentServer()))
                .collect(Collectors.toUnmodifiableList());
    }

    public Collection<UserInfo> getUsersByStatus(UserStatus status) {
        return users.values().stream()
                .filter(user -> status == user.status())
                .collect(Collectors.toUnmodifiableList());
    }

    public boolean isUserOnline(UUID uuid) {
        return users.containsKey(uuid);
    }

    public int getOnlineCount() {
        return users.size();
    }

    public int getOnlineCount(String serverId) {
        return (int) users.values().stream()
                .filter(user -> serverId.equals(user.currentServer()))
                .count();
    }

    public void updateUserMetadata(UUID uuid, String key, String value) {
        UserInfo user = users.get(uuid);
        if (user == null) return;

        Map<String, String> newMetadata = new HashMap<>(user.metadata());
        newMetadata.put(key, value);

        UserInfo updatedUser = new UserInfo(
                user.uuid(),
                user.username(),
                user.displayName(),
                user.currentServer(),
                user.status(),
                user.ipAddress(),
                user.locale(),
                newMetadata,
                user.properties(),
                user.firstJoin(),
                user.lastSeen()
        );
        users.put(uuid, updatedUser);

        // Update database
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                UPDATE users 
                SET metadata = ?
                WHERE uuid = ?
                """)) {
            stmt.setString(1, gson.toJson(newMetadata));
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update user metadata", e);
        }
    }
}