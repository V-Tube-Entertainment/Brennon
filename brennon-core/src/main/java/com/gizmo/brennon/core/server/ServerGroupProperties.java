package com.gizmo.brennon.core.server;

import java.util.Map;

public record ServerGroupProperties(
        boolean autoScaling,
        int minServers,
        int maxServers,
        int playersPerServer,
        boolean randomJoin,
        Map<String, String> customProperties
) {
    public static ServerGroupProperties createDefault() {
        return new ServerGroupProperties(
                false,
                1,
                1,
                100,
                true,
                Map.of()
        );
    }
}
