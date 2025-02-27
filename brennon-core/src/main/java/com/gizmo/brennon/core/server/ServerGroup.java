package com.gizmo.brennon.core.server;

import java.util.Set;

public record ServerGroup(
        String id,
        String name,
        String description,
        boolean restricted,
        Set<String> serverIds,
        ServerGroupType type,
        ServerGroupProperties properties
) {}
