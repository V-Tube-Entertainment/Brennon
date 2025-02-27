package com.gizmo.brennon.core.ticket;

import com.google.inject.Inject;
import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.redis.RedisManager;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TicketService implements Service {
    private final Logger logger;
    private final DatabaseManager databaseManager;
    private final RedisManager redisManager;

    @Inject
    public TicketService(Logger logger, DatabaseManager databaseManager, RedisManager redisManager) {
        this.logger = logger;
        this.databaseManager = databaseManager;
        this.redisManager = redisManager;
    }

    @Override
    public void enable() throws Exception {
        initializeDatabase();
    }

    @Override
    public void disable() throws Exception {
        // Cleanup if needed
    }

    private void initializeDatabase() throws Exception {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {

            // Create tickets table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tickets (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    creator_id CHAR(36) NOT NULL,
                    creator_name VARCHAR(16) NOT NULL,
                    title VARCHAR(255) NOT NULL,
                    status VARCHAR(32) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    assigned_to_id CHAR(36),
                    assigned_to_name VARCHAR(16),
                    category VARCHAR(64) NOT NULL,
                    INDEX idx_creator_id (creator_id),
                    INDEX idx_status (status),
                    INDEX idx_assigned_to_id (assigned_to_id)
                )
            """);

            // Create ticket responses table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ticket_responses (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    ticket_id BIGINT NOT NULL,
                    responder_id CHAR(36) NOT NULL,
                    responder_name VARCHAR(16) NOT NULL,
                    message TEXT NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    is_staff_response BOOLEAN NOT NULL,
                    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
                    INDEX idx_ticket_id (ticket_id)
                )
            """);
        }
    }

    public Ticket createTicket(UUID creatorId, String creatorName, String title, String category) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     """
                     INSERT INTO tickets (creator_id, creator_name, title, status, category)
                     VALUES (?, ?, ?, ?, ?)
                     """, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, creatorId.toString());
            stmt.setString(2, creatorName);
            stmt.setString(3, title);
            stmt.setString(4, TicketStatus.OPEN.name());
            stmt.setString(5, category);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Ticket ticket = new Ticket(
                            rs.getLong(1),
                            creatorId,
                            creatorName,
                            title,
                            TicketStatus.OPEN,
                            Instant.now(),
                            new ArrayList<>(),
                            null,
                            null,
                            category
                    );

                    notifyTicketCreated(ticket);
                    return ticket;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to create ticket", e);
        }
        return null;
    }

    public TicketResponse addResponse(long ticketId, UUID responderId, String responderName,
                                      String message, boolean isStaffResponse) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     """
                     INSERT INTO ticket_responses 
                     (ticket_id, responder_id, responder_name, message, is_staff_response)
                     VALUES (?, ?, ?, ?, ?)
                     """, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, ticketId);
            stmt.setString(2, responderId.toString());
            stmt.setString(3, responderName);
            stmt.setString(4, message);
            stmt.setBoolean(5, isStaffResponse);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    TicketResponse response = new TicketResponse(
                            rs.getLong(1),
                            ticketId,
                            responderId,
                            responderName,
                            message,
                            Instant.now(),
                            isStaffResponse
                    );

                    notifyTicketResponse(response);
                    return response;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to add ticket response", e);
        }
        return null;
    }

    public boolean updateTicketStatus(long ticketId, TicketStatus newStatus) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE tickets SET status = ? WHERE id = ?")) {

            stmt.setString(1, newStatus.name());
            stmt.setLong(2, ticketId);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                notifyTicketStatusUpdate(ticketId, newStatus);
                return true;
            }
        } catch (Exception e) {
            logger.error("Failed to update ticket status", e);
        }
        return false;
    }

    public boolean assignTicket(long ticketId, UUID staffId, String staffName) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     """
                     UPDATE tickets 
                     SET assigned_to_id = ?, assigned_to_name = ?, status = ?
                     WHERE id = ?
                     """)) {

            stmt.setString(1, staffId.toString());
            stmt.setString(2, staffName);
            stmt.setString(3, TicketStatus.IN_PROGRESS.name());
            stmt.setLong(4, ticketId);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                notifyTicketAssigned(ticketId, staffId, staffName);
                return true;
            }
        } catch (Exception e) {
            logger.error("Failed to assign ticket", e);
        }
        return false;
    }

    private void notifyTicketCreated(Ticket ticket) {
        try {
            String channel = "brennon:tickets";
            String message = String.format("CREATE:%d", ticket.id());
            redisManager.sync().publish(channel, message);
        } catch (Exception e) {
            logger.error("Failed to publish ticket creation notification", e);
        }
    }

    private void notifyTicketResponse(TicketResponse response) {
        try {
            String channel = "brennon:tickets";
            String message = String.format("RESPONSE:%d:%d", response.ticketId(), response.id());
            redisManager.sync().publish(channel, message);
        } catch (Exception e) {
            logger.error("Failed to publish ticket response notification", e);
        }
    }

    private void notifyTicketStatusUpdate(long ticketId, TicketStatus newStatus) {
        try {
            String channel = "brennon:tickets";
            String message = String.format("STATUS:%d:%s", ticketId, newStatus.name());
            redisManager.sync().publish(channel, message);
        } catch (Exception e) {
            logger.error("Failed to publish ticket status update notification", e);
        }
    }

    private void notifyTicketAssigned(long ticketId, UUID staffId, String staffName) {
        try {
            String channel = "brennon:tickets";
            String message = String.format("ASSIGN:%d:%s:%s", ticketId, staffId, staffName);
            redisManager.sync().publish(channel, message);
        } catch (Exception e) {
            logger.error("Failed to publish ticket assignment notification", e);
        }
    }
}
