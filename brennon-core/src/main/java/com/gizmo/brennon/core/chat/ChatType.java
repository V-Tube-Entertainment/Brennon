package com.gizmo.brennon.core.chat;

public enum ChatType {
    GLOBAL(true),
    LOCAL(false),
    STAFF(true),
    PRIVATE(true),
    CHANNEL(true),
    ANNOUNCEMENT(true);

    private final boolean crossServer;

    ChatType(boolean crossServer) {
        this.crossServer = crossServer;
    }

    public boolean isCrossServer() {
        return crossServer;
    }
}
