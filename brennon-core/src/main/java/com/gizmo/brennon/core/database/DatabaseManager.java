package com.gizmo.brennon.core.database;

import com.google.inject.Inject;
import com.gizmo.brennon.core.config.CoreConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;

import javax.sql.DataSource;

public class DatabaseManager {
    private final Logger logger;
    private final CoreConfig config;
    private HikariDataSource dataSource;

    @Inject
    public DatabaseManager(Logger logger, CoreConfig config) {
        this.logger = logger;
        this.config = config;
    }

    public void initialize() {
        logger.info("Initializing database connection...");
        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getDatabaseConfig().getJdbcUrl());
            hikariConfig.setUsername(config.getDatabaseConfig().username());
            hikariConfig.setPassword(config.getDatabaseConfig().password());
            hikariConfig.setMaximumPoolSize(config.getDatabaseConfig().poolSize());
            hikariConfig.setPoolName("BrennonPool");

            this.dataSource = new HikariDataSource(hikariConfig);
            logger.info("Database connection established successfully!");
        } catch (Exception e) {
            logger.error("Failed to initialize database connection!", e);
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection closed successfully!");
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
