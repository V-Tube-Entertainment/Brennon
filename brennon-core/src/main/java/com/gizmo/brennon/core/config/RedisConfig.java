package com.gizmo.brennon.core.config;

public record RedisConfig(
        String host,
        int port,
        String password,
        int database
) {
    public String getRedisUrl() {
        if (password != null && !password.isEmpty()) {
            return String.format("redis://%s@%s:%d/%d", password, host, port, database);
        }
        return String.format("redis://%s:%d/%d", host, port, database);
    }
}
