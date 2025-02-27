package com.gizmo.brennon.core.config;

public record DatabaseConfig(
        String host,
        int port,
        String database,
        String username,
        String password,
        int poolSize
) {
    public String getJdbcUrl() {
        return String.format("jdbc:mariadb://%s:%d/%s", host, port, database);
    }
}
