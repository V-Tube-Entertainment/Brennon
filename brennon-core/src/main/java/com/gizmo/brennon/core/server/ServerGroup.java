package com.gizmo.brennon.core.server;

import java.util.List;
import java.util.Map;

public record ServerGroup(
        String id,
        String name,
        List<String> serverIds,
        ServerGroupProperties properties
) {
    public static ServerGroup create(String id, String name, List<String> serverIds, ServerGroupProperties properties) {
        return new ServerGroup(id, name, serverIds, properties);
    }
}
