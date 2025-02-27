package com.gizmo.brennon.core.server;

import java.util.Map;

public record ServerGroupProperties(
        boolean autoScaling,
        int minServers,
        int maxServers,
        String template,
        Map<String, String> properties
) {
    public static ServerGroupProperties defaults() {
        return new ServerGroupProperties(
                false,
                1,
                5,
                "default",
                Map.of()
        );
    }
}
