package com.gizmo.brennon.core.punishment;

public enum PunishmentType {
    BAN("banned"),
    MUTE("muted"),
    KICK("kicked"),
    WARNING("warned");

    private final String pastTense;

    PunishmentType(String pastTense) {
        this.pastTense = pastTense;
    }

    public String getPastTense() {
        return pastTense;
    }
}
