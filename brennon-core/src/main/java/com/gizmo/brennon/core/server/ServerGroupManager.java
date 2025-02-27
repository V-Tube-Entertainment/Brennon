package com.gizmo.brennon.core.server;

import com.google.inject.Inject;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.messaging.MessageBroker;
import com.gizmo.brennon.core.service.Service;
import com.google.gson.Gson;
import org.slf4j.Logger;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGroupManager implements Service {
    private final Logger logger;
    private final DatabaseManager databaseManager;
    private final MessageBroker messageBroker;
    private final Map<String, ServerGroup> groups;
    private final Gson gson;

    @Inject
    public ServerGroupManager(Logger logger, DatabaseManager databaseManager, MessageBroker messageBroker) {
        this.logger = logger;
        this.databaseManager = databaseManager;
        this.messageBroker = messageBroker;
        this.groups = new ConcurrentHashMap<>();
        this.gson = new Gson();
    }

    @Override
    public void enable() throws Exception {
        initializeDatabase();
        loadGroups();
    }

    private void initializeDatabase() throws SQLException {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS server_groups (
                    id VARCHAR(64) PRIMARY KEY,
                    name VARCHAR(64) NOT NULL,
                    description TEXT,
                    restricted BOOLEAN NOT NULL DEFAULT FALSE,
                    type VARCHAR(32) NOT NULL,
                    properties JSON NOT NULL
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS server_group_members (
                    group_id VARCHAR(64) NOT NULL,
                    server_id VARCHAR(64) NOT NULL,
                    PRIMARY KEY (group_id, server_id),
                    FOREIGN KEY (group_id) REFERENCES server_groups(id) ON DELETE CASCADE
                )
                """);
        }
    }

    private void loadGroups() {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT g.*, GROUP_CONCAT(m.server_id) as server_ids
                FROM server_groups g
                LEFT JOIN server_group_members m ON g.id = m.group_id
                GROUP BY g.id
                """)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String serverIdsStr = rs.getString("server_ids");
                    Set<String> serverIds = serverIdsStr != null ?
                            new HashSet<>(Arrays.asList(serverIdsStr.split(","))) :
                            new HashSet<>();

                    ServerGroup group = new ServerGroup(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getBoolean("restricted"),
                            serverIds,
                            ServerGroupType.valueOf(rs.getString("type")),
                            gson.fromJson(rs.getString("properties"), ServerGroupProperties.class)
                    );
                    groups.put(group.id(), group);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load server groups", e);
        }
    }

    public Optional<ServerGroup> getGroup(String id) {
        return Optional.ofNullable(groups.get(id));
    }

    public Collection<ServerGroup> getAllGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    public ServerGroup createGroup(String id, String name, String description,
                                   ServerGroupType type, ServerGroupProperties properties) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO server_groups (id, name, description, type, properties)
                VALUES (?, ?, ?, ?, ?)
                """)) {

            stmt.setString(1, id);
            stmt.setString(2, name);
            stmt.setString(3, description);
            stmt.setString(4, type.name());
            stmt.setString(5, gson.toJson(properties));

            stmt.executeUpdate();

            ServerGroup group = new ServerGroup(
                    id,
                    name,
                    description,
                    false,
                    new HashSet<>(),
                    type,
                    properties
            );

            groups.put(id, group);
            return group;
        } catch (SQLException e) {
            logger.error("Failed to create server group", e);
            return null;
        }
    }

    public void updateGroup(ServerGroup group) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                UPDATE server_groups 
                SET name = ?, description = ?, restricted = ?, type = ?, properties = ?
                WHERE id = ?
                """)) {

            stmt.setString(1, group.name());
            stmt.setString(2, group.description());
            stmt.setBoolean(3, group.restricted());
            stmt.setString(4, group.type().name());
            stmt.setString(5, gson.toJson(group.properties()));
            stmt.setString(6, group.id());

            stmt.executeUpdate();
            groups.put(group.id(), group);
        } catch (SQLException e) {
            logger.error("Failed to update server group", e);
        }
    }

    public void deleteGroup(String id) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM server_groups WHERE id = ?")) {

            stmt.setString(1, id);
            stmt.executeUpdate();
            groups.remove(id);
        } catch (SQLException e) {
            logger.error("Failed to delete server group", e);
        }
    }
}