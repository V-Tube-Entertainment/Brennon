package com.gizmo.brennon.velocity.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Instant;
import java.util.UUID;

public record ChatMessage(
        UUID senderId,
        String username,
        String content,
        Instant timestamp
) {
    private static final Gson GSON = new GsonBuilder().create();

    public static ChatMessage fromJson(String json) {
        return GSON.fromJson(json, ChatMessage.class);
    }

    public String toJson() {
        return GSON.toJson(this);
    }
}
