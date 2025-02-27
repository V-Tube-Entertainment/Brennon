package com.gizmo.brennon.core.redis;

public record RedisConfig(
        String host,
        int port,
        String password,
        int database
) {
    public static RedisConfig defaultConfig() {
        return new RedisConfig("localhost", 6379, "", 0);
    }
}
