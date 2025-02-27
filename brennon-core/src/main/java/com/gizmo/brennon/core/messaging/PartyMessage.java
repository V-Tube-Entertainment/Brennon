package com.gizmo.brennon.core.messaging;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PartyMessage {
    private final UUID partyId;
    private final UUID leaderId;
    private final PartyActionType actionType;
    private final List<UUID> members;
    private final String message;
    private final String serverName;
    private final Instant timestamp;

    public PartyMessage(UUID partyId, UUID leaderId, PartyActionType actionType,
                        List<UUID> members, String message, String serverName, Instant timestamp) {
        this.partyId = partyId;
        this.leaderId = leaderId;
        this.actionType = actionType;
        this.members = members;
        this.message = message;
        this.serverName = serverName;
        this.timestamp = timestamp;
    }

    public UUID getPartyId() {
        return partyId;
    }

    public UUID getLeaderId() {
        return leaderId;
    }

    public PartyActionType getActionType() {
        return actionType;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public String getMessage() {
        return message;
    }

    public String getServerName() {
        return serverName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
