package com.gizmo.brennon.core.server;

import com.google.inject.Inject;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGroupManager implements Service {
    private final Logger logger;
    private final DatabaseManager databaseManager;
    private final Map<String, ServerGroup> groups;

    @Inject
    public ServerGroupManager(Logger logger, DatabaseManager databaseManager) {
        this.logger = logger;
        this.databaseManager = databaseManager;
        this.groups = new ConcurrentHashMap<>();
    }

    @Override
    public void enable() throws Exception {
        initializeDatabase();
        loadGroups();
    }

    @Override
    public void disable() throws Exception {
        groups.clear();
    }

    private void initializeDatabase() throws Exception {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS server_groups (
                    id VARCHAR(64) PRIMARY KEY,
                    name VARCHAR(64) NOT NULL,
                    auto_scaling BOOLEAN NOT NULL DEFAULT FALSE,
                    min_servers INT NOT NULL DEFAULT 1,
                    max_servers INT NOT NULL DEFAULT 5,
                    template VARCHAR(64) NOT NULL DEFAULT 'default',
                    properties JSON NOT NULL DEFAULT '{}'
                )""")) {
            stmt.executeUpdate();
        }
    }

    private void loadGroups() {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM server_groups");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                ServerGroup group = new ServerGroup(
                        id,
                        rs.getString("name"),
                        getServerIdsForGroup(id),
                        new ServerGroupProperties(
                                rs.getBoolean("auto_scaling"),
                                rs.getInt("min_servers"),
                                rs.getInt("max_servers"),
                                rs.getString("template"),
                                Map.of() // Load properties from JSON
                        )
                );
                groups.put(id, group);
            }
        } catch (Exception e) {
            logger.error("Failed to load server groups", e);
        }
    }

    private List<String> getServerIdsForGroup(String groupId) {
        List<String> serverIds = new ArrayList<>();
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM servers WHERE server_group = ?")) {
            stmt.setString(1, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    serverIds.add(rs.getString("id"));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get server IDs for group: " + groupId, e);
        }
        return serverIds;
    }

    public Optional<ServerGroup> getGroup(String id) {
        return Optional.ofNullable(groups.get(id));
    }

    public Collection<ServerGroup> getAllGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    public void reloadGroups() {
        logger.info("Reloading server groups...");
        groups.clear();
        try {
            loadGroups();
            logger.info("Server groups reloaded successfully");
        } catch (Exception e) {
            logger.error("Failed to reload server groups", e);
            throw new RuntimeException("Failed to reload server groups", e);
        }
    }
}