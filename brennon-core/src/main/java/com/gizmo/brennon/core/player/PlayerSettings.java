package com.gizmo.brennon.core.player;

import java.util.HashMap;
import java.util.Map;

public class PlayerSettings {
    private boolean privateMessages;
    private boolean partyInvites;
    private boolean friendRequests;
    private boolean staffNotifications;
    private boolean announcements;
    private boolean chatMentions;
    private Map<String, Boolean> gameSpecificSettings;

    public PlayerSettings() {
        this.privateMessages = true;
        this.partyInvites = true;
        this.friendRequests = true;
        this.staffNotifications = true;
        this.announcements = true;
        this.chatMentions = true;
        this.gameSpecificSettings = new HashMap<>();
    }

    public void copyFrom(PlayerSettings other) {
        this.privateMessages = other.privateMessages;
        this.partyInvites = other.partyInvites;
        this.friendRequests = other.friendRequests;
        this.staffNotifications = other.staffNotifications;
        this.announcements = other.announcements;
        this.chatMentions = other.chatMentions;
        this.gameSpecificSettings = new HashMap<>(other.gameSpecificSettings);
    }

    // Getters and Setters
    public boolean allowsPrivateMessages() { return privateMessages; }
    public void setPrivateMessages(boolean value) { this.privateMessages = value; }

    public boolean allowsPartyInvites() { return partyInvites; }
    public void setPartyInvites(boolean value) { this.partyInvites = value; }

    public boolean allowsFriendRequests() { return friendRequests; }
    public void setFriendRequests(boolean value) { this.friendRequests = value; }

    public boolean allowsStaffNotifications() { return staffNotifications; }
    public void setStaffNotifications(boolean value) { this.staffNotifications = value; }

    public boolean allowsAnnouncements() { return announcements; }
    public void setAnnouncements(boolean value) { this.announcements = value; }

    public boolean allowsChatMentions() { return chatMentions; }
    public void setChatMentions(boolean value) { this.chatMentions = value; }

    public void setGameSetting(String game, boolean value) {
        gameSpecificSettings.put(game.toLowerCase(), value);
    }

    public boolean getGameSetting(String game, boolean defaultValue) {
        return gameSpecificSettings.getOrDefault(game.toLowerCase(), defaultValue);
    }
}