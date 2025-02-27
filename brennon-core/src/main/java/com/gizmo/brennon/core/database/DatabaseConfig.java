package com.gizmo.brennon.core.database;

public record DatabaseConfig(
        String jdbcUrl,
        String username,
        String password,
        int maxPoolSize
) {
    public static DatabaseConfig defaultConfig() {
        return new DatabaseConfig(
                "jdbc:mysql://localhost:3306/brennon",
                "root",
                "",
                10
        );
    }
}
