package com.gizmo.brennon.core.chat;

import java.time.Instant;
import java.util.UUID;

public class ChatMessage {
    private final UUID sender;
    private final String senderName;
    private final String message;
    private final ChatType type;
    private final String serverName;
    private final UUID target;
    private final String channel;
    private final Instant timestamp;

    public ChatMessage(UUID sender, String senderName, String message, ChatType type,
                       String serverName, UUID target, String channel) {
        this.sender = sender;
        this.senderName = senderName;
        this.message = message;
        this.type = type;
        this.serverName = serverName;
        this.target = target;
        this.channel = channel;
        this.timestamp = Instant.now();
    }

    public UUID getSender() { return sender; }
    public String getSenderName() { return senderName; }
    public String getMessage() { return message; }
    public ChatType getType() { return type; }
    public String getServerName() { return serverName; }
    public UUID getTarget() { return target; }
    public String getChannel() { return channel; }
    public Instant getTimestamp() { return timestamp; }
}
