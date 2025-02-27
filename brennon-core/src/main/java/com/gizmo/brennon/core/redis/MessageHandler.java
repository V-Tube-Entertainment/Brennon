package com.gizmo.brennon.core.redis;

@FunctionalInterface
public interface MessageHandler {
    void handle(String message);
}