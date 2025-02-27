package com.gizmo.brennon.core.user;

public record UserSettings(
        boolean receiveAnnouncements,
        boolean receivePrivateMessages,
        boolean receivePartyInvites,
        boolean receiveStaffMessages,
        String language
) {
    public static UserSettings createDefault() {
        return new UserSettings(true, true, true, true, "en_US");
    }
}
