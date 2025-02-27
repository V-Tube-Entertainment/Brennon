package com.gizmo.brennon.core.config;

import com.google.inject.Inject;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.slf4j.Logger;

import java.nio.file.Path;

public class CoreConfig {
    private final Logger logger;
    private final Path configDir;
    private DatabaseConfig databaseConfig;
    private RedisConfig redisConfig;

    @Inject
    public CoreConfig(Logger logger, Path configDir) {
        this.logger = logger;
        this.configDir = configDir;
    }

    public void load() {
        try {
            Path configPath = configDir.resolve("config.conf");
            HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                    .path(configPath)
                    .build();

            CommentedConfigurationNode root = loader.load();

            // Load database config
            CommentedConfigurationNode dbNode = root.node("database");
            this.databaseConfig = new DatabaseConfig(
                    dbNode.node("host").getString("localhost"),
                    dbNode.node("port").getInt(3306),
                    dbNode.node("database").getString("brennon"),
                    dbNode.node("username").getString("root"),
                    dbNode.node("password").getString(""),
                    dbNode.node("pool-size").getInt(10)
            );

            // Load Redis config
            CommentedConfigurationNode redisNode = root.node("redis");
            this.redisConfig = new RedisConfig(
                    redisNode.node("host").getString("localhost"),
                    redisNode.node("port").getInt(6379),
                    redisNode.node("password").getString(""),
                    redisNode.node("database").getInt(0)
            );

            // Save the config with default values if it didn't exist
            loader.save(root);
        } catch (Exception e) {
            logger.error("Failed to load core configuration!", e);
        }
    }

    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    public RedisConfig getRedisConfig() {
        return redisConfig;
    }
}
