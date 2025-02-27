package com.gizmo.brennon.core.punishment;

public enum PunishmentType {
    BAN("Ban", true),
    TEMP_BAN("Temporary Ban", true),
    MUTE("Mute", false),
    TEMP_MUTE("Temporary Mute", false),
    WARN("Warning", false),
    KICK("Kick", false);

    private final String displayName;
    private final boolean preventJoin;

    PunishmentType(String displayName, boolean preventJoin) {
        this.displayName = displayName;
        this.preventJoin = preventJoin;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean doesPreventJoin() {
        return preventJoin;
    }
}
