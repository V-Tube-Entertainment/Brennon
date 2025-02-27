package com.gizmo.brennon.core.messaging;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gizmo.brennon.core.redis.RedisManager;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Singleton
public class MessageService implements Service {
    private final Logger logger;
    private final RedisManager redisManager;
    private final Gson gson;
    private final Map<String, Consumer<String>> messageHandlers;

    private String serverName;

    @Inject
    public MessageService(Logger logger, RedisManager redisManager) {
        this.logger = logger;
        this.redisManager = redisManager;
        this.gson = new Gson();
        this.messageHandlers = new ConcurrentHashMap<>();
    }

    @Override
    public void enable() throws Exception {
        // Register all channel handlers
        registerChannelHandlers();

        // Subscribe to all channels
        messageHandlers.forEach((channel, handler) ->
                redisManager.subscribe(channel, handler::accept));

        logger.info("MessageService enabled successfully");
    }

    @Override
    public void disable() throws Exception {
        // Unsubscribe from all channels
        messageHandlers.keySet().forEach(redisManager::unsubscribe);
        messageHandlers.clear();

        logger.info("MessageService disabled successfully");
    }

    private void registerChannelHandlers() {
        // Chat channels
        messageHandlers.put(MessagingChannels.GLOBAL_CHAT, this::handleGlobalChat);
        messageHandlers.put(MessagingChannels.STAFF_CHAT, this::handleStaffChat);
        messageHandlers.put(MessagingChannels.PRIVATE_MESSAGES, this::handlePrivateMessage);

        // Server and player status
        messageHandlers.put(MessagingChannels.SERVER_STATUS, this::handleServerStatus);
        messageHandlers.put(MessagingChannels.PLAYER_UPDATES, this::handlePlayerUpdate);

        // Staff and moderation
        messageHandlers.put(MessagingChannels.STAFF_ACTIONS, this::handleStaffAction);
        messageHandlers.put(MessagingChannels.REPORTS, this::handleReport);

        // Party system
        messageHandlers.put(MessagingChannels.PARTY, this::handlePartyMessage);
        messageHandlers.put(MessagingChannels.PARTY_CHAT, this::handlePartyChat);

        // System messages
        messageHandlers.put(MessagingChannels.ANNOUNCEMENTS, this::handleAnnouncement);
        messageHandlers.put(MessagingChannels.ALERTS, this::handleAlert);
        messageHandlers.put(MessagingChannels.MAINTENANCE, this::handleMaintenance);
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    // Chat Methods
    public void sendGlobalMessage(UUID sender, String message) {
        ChatMessage chatMessage = new ChatMessage(sender, message, serverName, Instant.now());
        redisManager.publish(MessagingChannels.GLOBAL_CHAT, gson.toJson(chatMessage));
    }

    public void sendStaffMessage(UUID sender, String message) {
        ChatMessage chatMessage = new ChatMessage(sender, message, serverName, Instant.now());
        redisManager.publish(MessagingChannels.STAFF_CHAT, gson.toJson(chatMessage));
    }

    public void sendPrivateMessage(UUID sender, UUID recipient, String message) {
        PrivateMessage pm = new PrivateMessage(sender, recipient, message, serverName, Instant.now());
        redisManager.publish(MessagingChannels.PRIVATE_MESSAGES, gson.toJson(pm));
    }

    // Server Status Methods
    public void sendServerStatus(ServerStatus status, int playerCount, int maxPlayers, double tps, double ramUsage) {
        ServerStatusMessage message = new ServerStatusMessage(
                serverName, status, playerCount, maxPlayers, tps, ramUsage, Instant.now()
        );
        redisManager.publish(MessagingChannels.SERVER_STATUS, gson.toJson(message));
    }

    // Player Update Methods
    public void sendPlayerUpdate(UUID playerId, String username, PlayerUpdateType type, String targetServer) {
        PlayerUpdateMessage message = new PlayerUpdateMessage(
                playerId, username, type, serverName, targetServer, Instant.now()
        );
        redisManager.publish(MessagingChannels.PLAYER_UPDATES, gson.toJson(message));
    }

    // Staff Action Methods
    public void sendStaffAction(UUID staffId, String staffName, UUID targetId, String targetName,
                                StaffActionType actionType, String reason) {
        StaffActionMessage message = new StaffActionMessage(
                staffId, staffName, targetId, targetName, actionType, serverName, reason, Instant.now()
        );
        redisManager.publish(MessagingChannels.STAFF_ACTIONS, gson.toJson(message));
    }

    // Party System Methods
    public void sendPartyMessage(UUID partyId, UUID leaderId, PartyActionType actionType,
                                 List<UUID> members, String message) {
        PartyMessage partyMessage = new PartyMessage(
                partyId, leaderId, actionType, members, message, serverName, Instant.now()
        );
        redisManager.publish(MessagingChannels.PARTY, gson.toJson(partyMessage));
    }

    public void sendPartyChatMessage(UUID partyId, UUID sender, String message) {
        PartyMessage chatMessage = new PartyMessage(
                partyId, sender, PartyActionType.CHAT, null, message, serverName, Instant.now()
        );
        redisManager.publish(MessagingChannels.PARTY_CHAT, gson.toJson(chatMessage));
    }

    // System Message Methods
    public void sendAnnouncement(String message, AnnouncementType type) {
        Announcement announcement = new Announcement(message, type, serverName, Instant.now());
        redisManager.publish(MessagingChannels.ANNOUNCEMENTS, gson.toJson(announcement));
    }

    public void sendMaintenanceAlert(String message, boolean enabling) {
        MaintenanceMessage maintenance = new MaintenanceMessage(message, enabling, serverName, Instant.now());
        redisManager.publish(MessagingChannels.MAINTENANCE, gson.toJson(maintenance));
    }

    // Message Handlers
    private void handleGlobalChat(String json) {
        try {
            ChatMessage message = gson.fromJson(json, ChatMessage.class);
            // Implement your chat handling logic here
            logger.debug("Received global chat message from {}: {}", message.getSender(), message.getMessage());
        } catch (Exception e) {
            logger.error("Error handling global chat message", e);
        }
    }

    private void handleStaffChat(String json) {
        try {
            ChatMessage message = gson.fromJson(json, ChatMessage.class);
            // Implement your staff chat handling logic here
            logger.debug("Received staff chat message from {}: {}", message.getSender(), message.getMessage());
        } catch (Exception e) {
            logger.error("Error handling staff chat message", e);
        }
    }

    private void handlePrivateMessage(String json) {
        try {
            PrivateMessage message = gson.fromJson(json, PrivateMessage.class);
            // Implement your private message handling logic here
            logger.debug("Received private message: {} -> {}: {}",
                    message.getSender(), message.getRecipient(), message.getMessage());
        } catch (Exception e) {
            logger.error("Error handling private message", e);
        }
    }

    private void handleServerStatus(String json) {
        try {
            ServerStatusMessage message = gson.fromJson(json, ServerStatusMessage.class);
            // Implement server status handling logic
            logger.debug("Received server status update from {}: {}",
                    message.getServerName(), message.getStatus());
        } catch (Exception e) {
            logger.error("Error handling server status message", e);
        }
    }

    private void handlePlayerUpdate(String json) {
        try {
            PlayerUpdateMessage message = gson.fromJson(json, PlayerUpdateMessage.class);
            // Implement player update handling logic
            logger.debug("Received player update for {}: {}",
                    message.getUsername(), message.getType());
        } catch (Exception e) {
            logger.error("Error handling player update message", e);
        }
    }

    private void handleStaffAction(String json) {
        try {
            StaffActionMessage message = gson.fromJson(json, StaffActionMessage.class);
            // Implement staff action handling logic
            logger.debug("Received staff action: {} performed {} on {}",
                    message.getStaffName(), message.getActionType(), message.getTargetName());
        } catch (Exception e) {
            logger.error("Error handling staff action message", e);
        }
    }

    private void handlePartyMessage(String json) {
        try {
            PartyMessage message = gson.fromJson(json, PartyMessage.class);
            // Implement party message handling logic
            logger.debug("Received party message for party {}: {}",
                    message.getPartyId(), message.getActionType());
        } catch (Exception e) {
            logger.error("Error handling party message", e);
        }
    }

    private void handlePartyChat(String json) {
        try {
            PartyMessage message = gson.fromJson(json, PartyMessage.class);
            // Implement party chat handling logic
            logger.debug("Received party chat message for party {}: {}",
                    message.getPartyId(), message.getMessage());
        } catch (Exception e) {
            logger.error("Error handling party chat message", e);
        }
    }

    private void handleAnnouncement(String json) {
        try {
            Announcement announcement = gson.fromJson(json, Announcement.class);
            // Implement announcement handling logic
            logger.debug("Received announcement from {}: {}",
                    announcement.getServerName(), announcement.getMessage());
        } catch (Exception e) {
            logger.error("Error handling announcement", e);
        }
    }

    private void handleAlert(String json) {
        try {
            AlertMessage alert = gson.fromJson(json, AlertMessage.class);
            // Implement alert handling logic
            logger.debug("Received alert: {}", alert.getMessage());
        } catch (Exception e) {
            logger.error("Error handling alert message", e);
        }
    }

    private void handleMaintenance(String json) {
        try {
            MaintenanceMessage maintenance = gson.fromJson(json, MaintenanceMessage.class);
            // Implement maintenance message handling logic
            logger.debug("Received maintenance message from {}: {}",
                    maintenance.getServerName(), maintenance.getMessage());
        } catch (Exception e) {
            logger.error("Error handling maintenance message", e);
        }
    }

    private void handleReport(String json) {
        try {
            ReportMessage report = gson.fromJson(json, ReportMessage.class);
            // Implement report handling logic
            logger.debug("Received report: {} reported {} for {}",
                    report.getReporterName(), report.getTargetName(), report.getReason());
        } catch (Exception e) {
            logger.error("Error handling report message", e);
        }
    }
}