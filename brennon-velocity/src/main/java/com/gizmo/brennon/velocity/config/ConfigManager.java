package com.gizmo.brennon.velocity.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gizmo.brennon.velocity.BrennonVelocity;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages configuration loading and saving for the Velocity module
 *
 * @author Gizmo0320
 * @since 2025-03-01 03:35:12
 */
public class ConfigManager {
    private final BrennonVelocity plugin;
    private final Logger logger;
    private final Path configPath;
    private final Gson gson;
    private Config config;

    public ConfigManager(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.configPath = plugin.getDataDirectory().resolve("config.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        loadConfig();
    }

    /**
     * Loads the configuration from disk
     */
    private void loadConfig() {
        try {
            if (!Files.exists(configPath)) {
                config = new Config();
                saveConfig();
            } else {
                try (Reader reader = Files.newBufferedReader(configPath)) {
                    config = gson.fromJson(reader, Config.class);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load configuration", e);
            config = new Config();
        }
    }

    /**
     * Saves the configuration to disk
     */
    public void saveConfig() {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                gson.toJson(config, writer);
            }
        } catch (IOException e) {
            logger.error("Failed to save configuration", e);
        }
    }

    /**
     * Reloads the configuration from disk
     */
    public void reloadConfig() {
        // Save current config in case there are unsaved changes
        saveConfig();

        // Load fresh config from disk
        Config oldConfig = config;
        loadConfig();

        // Validate loaded config
        if (config == null) {
            logger.error("Failed to load configuration, rolling back to previous config");
            config = oldConfig;
            saveConfig();
            throw new IllegalStateException("Failed to load configuration");
        }

        logger.info("Configuration reloaded successfully");
    }

    /**
     * Gets the current configuration
     *
     * @return The current configuration
     */
    public Config getConfig() {
        return config;
    }
}