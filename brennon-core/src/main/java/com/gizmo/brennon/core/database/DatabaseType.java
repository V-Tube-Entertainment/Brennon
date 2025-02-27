package com.gizmo.brennon.core.database;

public enum DatabaseType {
    MYSQL("com.mysql.cj.jdbc.Driver", "jdbc:mysql://%s:%d/%s?%s"),
    MARIADB("org.mariadb.jdbc.Driver", "jdbc:mariadb://%s:%d/%s?%s"),
    POSTGRESQL("org.postgresql.Driver", "jdbc:postgresql://%s:%d/%s?%s"),
    SQLITE("org.sqlite.JDBC", "jdbc:sqlite:%s"),
    H2("org.h2.Driver", "jdbc:h2:%s");

    private final String driverClass;
    private final String jdbcUrlFormat;

    DatabaseType(String driverClass, String jdbcUrlFormat) {
        this.driverClass = driverClass;
        this.jdbcUrlFormat = jdbcUrlFormat;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public String getJdbcUrlFormat() {
        return jdbcUrlFormat;
    }
}
