package com.gizmo.brennon.core.messaging;

public final class MessagingChannels {
    // Existing channels
    public static final String GLOBAL_CHAT = "brennon:chat:global";
    public static final String STAFF_CHAT = "brennon:chat:staff";
    public static final String PRIVATE_MESSAGES = "brennon:chat:private";
    public static final String SERVER_STATUS = "brennon:server:status";
    public static final String PLAYER_UPDATES = "brennon:player:updates";
    public static final String STAFF_ACTIONS = "brennon:staff:actions";
    public static final String ANNOUNCEMENTS = "brennon:announcements";

    // New channels
    public static final String PARTY = "brennon:party";
    public static final String PARTY_CHAT = "brennon:party:chat";
    public static final String FRIEND_UPDATES = "brennon:friend:updates";
    public static final String REPORTS = "brennon:reports";
    public static final String TICKETS = "brennon:tickets";
    public static final String ALERTS = "brennon:alerts";
    public static final String MAINTENANCE = "brennon:maintenance";

    private MessagingChannels() {}
}
