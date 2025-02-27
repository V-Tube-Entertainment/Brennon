package com.gizmo.brennon.core.messaging;

import com.google.gson.JsonElement;

import java.time.Instant;
import java.util.UUID;

public record Message(
        UUID id,
        String channel,
        String source,
        JsonElement data,
        Instant timestamp
) {
    public static Message create(String channel, String source, JsonElement data) {
        return new Message(UUID.randomUUID(), channel, source, data, Instant.now());
    }
}
