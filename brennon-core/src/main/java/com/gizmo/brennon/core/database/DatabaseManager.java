package com.gizmo.brennon.core.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gizmo.brennon.core.config.Configuration;
import com.gizmo.brennon.core.config.ConfigurationManager;
import com.gizmo.brennon.core.config.ConfigurationType;
import com.gizmo.brennon.core.service.Service;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Singleton
public class DatabaseManager implements Service {
    private final Logger logger;
    private final ConfigurationManager configManager;
    private HikariDataSource dataSource;
    private DatabaseType currentType;

    @Inject
    public DatabaseManager(Logger logger, ConfigurationManager configManager) {
        this.logger = logger;
        this.configManager = configManager;
    }

    @Override
    public void enable() throws Exception {
        // Load database configuration
        Configuration config = configManager.loadConfig("database", ConfigurationType.YAML, getDefaultConfig());

        // Get database type and load driver
        this.currentType = DatabaseType.valueOf(config.get("type", "MYSQL").toUpperCase());
        try {
            Class.forName(currentType.getDriverClass());
        } catch (ClassNotFoundException e) {
            logger.error("Database driver not found: " + currentType.getDriverClass(), e);
            throw e;
        }

        // Configure HikariCP
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(buildJdbcUrl(config));
        hikariConfig.setUsername(config.get("username", "root"));
        hikariConfig.setPassword(config.get("password", ""));
        hikariConfig.setPoolName("BrennonPool");

        // Pool settings
        hikariConfig.setMaximumPoolSize(config.get("pool.maximum-pool-size", 10));
        hikariConfig.setMinimumIdle(config.get("pool.minimum-idle", 5));
        hikariConfig.setMaxLifetime(TimeUnit.MINUTES.toMillis(config.get("pool.max-lifetime-minutes", 30)));
        hikariConfig.setConnectionTimeout(TimeUnit.SECONDS.toMillis(config.get("pool.connection-timeout-seconds", 5)));
        hikariConfig.setIdleTimeout(TimeUnit.MINUTES.toMillis(config.get("pool.idle-timeout-minutes", 10)));

        // Performance settings
        hikariConfig.setAutoCommit(true);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

        // Initialize the connection pool
        try {
            dataSource = new HikariDataSource(hikariConfig);
            logger.info("Database connection pool initialized successfully (Type: {})", currentType);

            // Test connection
            try (Connection conn = getConnection()) {
                logger.info("Database connection test successful");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw e;
        }
    }

    @Override
    public void disable() throws Exception {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }

    /**
     * Gets the HikariCP DataSource
     *
     * @return The configured DataSource
     * @throws IllegalStateException if the datasource is not initialized
     */
    public DataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            throw new IllegalStateException("Database connection pool is not initialized");
        }
        return dataSource;
    }

    /**
     * Gets a connection from the pool
     *
     * @return A database connection
     * @throws SQLException if a connection cannot be established
     */
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Gets the current database type
     *
     * @return The current DatabaseType
     */
    public DatabaseType getCurrentType() {
        return currentType;
    }

    /**
     * Checks if the connection pool is healthy
     *
     * @return true if the pool is initialized and not closed
     */
    public boolean isHealthy() {
        return dataSource != null && !dataSource.isClosed();
    }

    /**
     * Gets current pool statistics
     *
     * @return A map of pool statistics
     */
    public Map<String, Integer> getPoolStats() {
        if (!isHealthy()) {
            return Map.of();
        }
        return Map.of(
                "active-connections", dataSource.getHikariPoolMXBean().getActiveConnections(),
                "idle-connections", dataSource.getHikariPoolMXBean().getIdleConnections(),
                "total-connections", dataSource.getHikariPoolMXBean().getTotalConnections(),
                "threads-awaiting", dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }

    private String buildJdbcUrl(Configuration config) {
        DatabaseType type = DatabaseType.valueOf(config.get("type", "MYSQL").toUpperCase());
        String host = config.get("host", "localhost");
        int port = config.get("port", getDefaultPort(type));
        String database = config.get("database", "brennon");
        String parameters = config.get("parameters", getDefaultParameters(type));

        switch (type) {
            case SQLITE:
                return String.format(type.getJdbcUrlFormat(), database);
            case H2:
                return String.format(type.getJdbcUrlFormat(), database);
            default:
                return String.format(type.getJdbcUrlFormat(), host, port, database, parameters);
        }
    }

    private int getDefaultPort(DatabaseType type) {
        switch (type) {
            case MYSQL:
            case MARIADB:
                return 3306;
            case POSTGRESQL:
                return 5432;
            default:
                return -1;
        }
    }

    private String getDefaultParameters(DatabaseType type) {
        switch (type) {
            case MYSQL:
            case MARIADB:
                return "useSSL=false&serverTimezone=UTC&autoReconnect=true&useUnicode=true&characterEncoding=utf8";
            case POSTGRESQL:
                return "useSSL=false&ApplicationName=Brennon";
            default:
                return "";
        }
    }

    private Map<String, Object> getDefaultConfig() {
        return Map.of(
                "type", "MYSQL",
                "host", "localhost",
                "port", 3306,
                "database", "brennon",
                "username", "root",
                "password", "",
                "parameters", "useSSL=false&serverTimezone=UTC&autoReconnect=true&useUnicode=true&characterEncoding=utf8",
                "pool", Map.of(
                        "maximum-pool-size", 10,
                        "minimum-idle", 5,
                        "max-lifetime-minutes", 30,
                        "connection-timeout-seconds", 5,
                        "idle-timeout-minutes", 10
                )
        );
    }
}