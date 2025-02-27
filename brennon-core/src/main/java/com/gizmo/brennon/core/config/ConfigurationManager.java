package com.gizmo.brennon.core.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ConfigurationManager implements Service {
    private final Logger logger;
    private final Map<String, Configuration> configurations;
    private final Yaml yaml;
    private final Gson gson;
    private Path configDirectory;

    @Inject
    public ConfigurationManager(Logger logger) {
        this.logger = logger;
        this.configurations = new ConcurrentHashMap<>();

        // Setup YAML
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        this.yaml = new Yaml(options);

        // Setup JSON
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    @Override
    public void enable() throws Exception {
        this.configDirectory = Paths.get("config");
        Files.createDirectories(configDirectory);
        logger.info("Configuration system enabled. Directory: {}", configDirectory);
    }

    @Override
    public void disable() throws Exception {
        configurations.clear();
        logger.info("Configuration system disabled");
    }

    /**
     * Loads or reloads a configuration file
     *
     * @param name The name of the configuration (without extension)
     * @param type The configuration type (YAML or JSON)
     * @param defaultConfig The default configuration values
     * @return The loaded configuration
     */
    public Configuration loadConfig(String name, ConfigurationType type, Map<String, Object> defaultConfig) {
        try {
            Path configPath = configDirectory.resolve(name + type.getExtension());
            Configuration config;

            if (Files.notExists(configPath)) {
                Files.createDirectories(configPath.getParent());
                config = new Configuration(defaultConfig);
                saveConfig(name, type, config);
            } else {
                config = loadConfigFromFile(configPath, type);
                // Merge with defaults to ensure all required values exist
                config.merge(defaultConfig);
                saveConfig(name, type, config); // Save back to update with any new defaults
            }

            configurations.put(name, config);
            return config;
        } catch (IOException e) {
            logger.error("Failed to load configuration: " + name, e);
            return new Configuration(defaultConfig);
        }
    }

    /**
     * Gets a loaded configuration
     *
     * @param name The name of the configuration
     * @return The configuration if loaded
     */
    public Optional<Configuration> getConfig(String name) {
        return Optional.ofNullable(configurations.get(name));
    }

    /**
     * Saves a configuration to file
     *
     * @param name The name of the configuration
     * @param type The configuration type
     * @param config The configuration to save
     */
    public void saveConfig(String name, ConfigurationType type, Configuration config) {
        try {
            Path configPath = configDirectory.resolve(name + type.getExtension());
            String output = type == ConfigurationType.YAML
                    ? yaml.dump(config.getAll())
                    : gson.toJson(config.getAll());

            Files.writeString(configPath, output);
        } catch (IOException e) {
            logger.error("Failed to save configuration: " + name, e);
        }
    }

    private Configuration loadConfigFromFile(Path path, ConfigurationType type) throws IOException {
        String content = Files.readString(path);
        Map<String, Object> data;

        if (type == ConfigurationType.YAML) {
            @SuppressWarnings("unchecked")
            Map<String, Object> yamlData = yaml.load(content);
            data = yamlData != null ? yamlData : new HashMap<>();
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonData = gson.fromJson(content, Map.class);
            data = jsonData != null ? jsonData : new HashMap<>();
        }

        return new Configuration(data);
    }

    /**
     * Reloads all configurations from disk
     */
    public void reloadAll() {
        configurations.forEach((name, config) -> {
            try {
                ConfigurationType type = detectConfigType(name);
                loadConfig(name, type, config.getAll());
            } catch (Exception e) {
                logger.error("Failed to reload configuration: " + name, e);
            }
        });
    }

    private ConfigurationType detectConfigType(String name) {
        if (Files.exists(configDirectory.resolve(name + ".yml"))) {
            return ConfigurationType.YAML;
        }
        return ConfigurationType.JSON;
    }
}
