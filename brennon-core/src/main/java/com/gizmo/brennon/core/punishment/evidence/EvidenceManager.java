package com.gizmo.brennon.core.punishment.evidence;

import com.google.inject.Inject;
import com.gizmo.brennon.core.database.DatabaseManager;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EvidenceManager {
    private final Logger logger;
    private final DatabaseManager databaseManager;

    @Inject
    public EvidenceManager(Logger logger, DatabaseManager databaseManager) {
        this.logger = logger;
        this.databaseManager = databaseManager;
    }

    public void initialize() throws SQLException {
        try (Connection conn = databaseManager.getDataSource().getConnection()) {
            // Create evidence table
            try (PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS evidence (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    punishment_id BIGINT NOT NULL,
                    type VARCHAR(32) NOT NULL,
                    content TEXT NOT NULL,
                    submitted_by UUID NOT NULL,
                    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (punishment_id) REFERENCES punishments(id)
                )""")) {
                stmt.executeUpdate();
            }
        }
    }

    public List<Evidence> getEvidence(long punishmentId) {
        List<Evidence> evidenceList = new ArrayList<>();

        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT id, type, content, submitted_by, submitted_at
                FROM evidence
                WHERE punishment_id = ?
                ORDER BY submitted_at DESC
                """)) {

            stmt.setLong(1, punishmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Evidence evidence = new Evidence(
                            rs.getLong("id"),
                            punishmentId,
                            EvidenceType.valueOf(rs.getString("type")),
                            rs.getString("content"),
                            UUID.fromString(rs.getString("submitted_by")),
                            rs.getTimestamp("submitted_at").toInstant()
                    );
                    evidenceList.add(evidence);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve evidence for punishment {}", punishmentId, e);
        }

        return evidenceList;
    }

    public Evidence addEvidence(long punishmentId, EvidenceType type, String content, UUID submittedBy) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO evidence (punishment_id, type, content, submitted_by, submitted_at)
                VALUES (?, ?, ?, ?, ?)
                """, PreparedStatement.RETURN_GENERATED_KEYS)) {

            Instant now = Instant.now();
            stmt.setLong(1, punishmentId);
            stmt.setString(2, type.name());
            stmt.setString(3, content);
            stmt.setString(4, submittedBy.toString());
            stmt.setObject(5, now);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Evidence(
                            rs.getLong(1),
                            punishmentId,
                            type,
                            content,
                            submittedBy,
                            now
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to add evidence for punishment {}", punishmentId, e);
        }

        return null;
    }

    public boolean removeEvidence(long evidenceId) {
        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                DELETE FROM evidence
                WHERE id = ?
                """)) {

            stmt.setLong(1, evidenceId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error("Failed to remove evidence {}", evidenceId, e);
            return false;
        }
    }

    public List<Evidence> getAllEvidence() {
        List<Evidence> evidenceList = new ArrayList<>();

        try (Connection conn = databaseManager.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT id, punishment_id, type, content, submitted_by, submitted_at
                FROM evidence
                ORDER BY submitted_at DESC
                """);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Evidence evidence = new Evidence(
                        rs.getLong("id"),
                        rs.getLong("punishment_id"),
                        EvidenceType.valueOf(rs.getString("type")),
                        rs.getString("content"),
                        UUID.fromString(rs.getString("submitted_by")),
                        rs.getTimestamp("submitted_at").toInstant()
                );
                evidenceList.add(evidence);
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve all evidence", e);
        }

        return evidenceList;
    }
}
