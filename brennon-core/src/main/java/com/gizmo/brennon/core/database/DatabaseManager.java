package com.gizmo.brennon.core.database;

import com.gizmo.brennon.core.service.Service;
import com.google.inject.Inject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager implements Service {
    private final Logger logger;
    private final DatabaseConfig config;
    private HikariDataSource dataSource;

    @Inject
    public DatabaseManager(Logger logger, DatabaseConfig config) {
        this.logger = logger;
        this.config = config;
    }

    @Override
    public void enable() throws Exception {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.jdbcUrl());
        hikariConfig.setUsername(config.username());
        hikariConfig.setPassword(config.password());
        hikariConfig.setMaximumPoolSize(config.maxPoolSize());

        dataSource = new HikariDataSource(hikariConfig);

        // Test connection
        try (Connection conn = dataSource.getConnection()) {
            logger.info("Successfully connected to database");
        }
    }

    @Override
    public void disable() throws Exception {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
