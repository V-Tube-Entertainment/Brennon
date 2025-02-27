package com.gizmo.brennon.core.config;

import com.google.inject.Inject;
import com.gizmo.brennon.core.service.Service;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.nio.file.WatchService;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConfigurationManager implements Service {
    private final Logger logger;
    private final Path configDir;
    private final Map<String, ConfigurationContainer> configurations;
    private WatchService watchService;
    private ScheduledExecutorService executor;

    @Inject
    public ConfigurationManager(Logger logger, Path configDir) {
        this.logger = logger;
        this.configDir = configDir;
        this.configurations = new ConcurrentHashMap<>();
    }

    @Override
    public void enable() throws Exception {
        configDir.toFile().mkdirs();
        watchService = configDir.getFileSystem().newWatchService();
        configDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        startWatchService();
    }

    @Override
    public void disable() throws Exception {
        if (executor != null) {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        if (watchService != null) {
            watchService.close();
        }
    }

    private void startWatchService() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(() -> {
            try {
                WatchKey key = watchService.poll();
                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path changed = (Path) event.context();
                        String fileName = changed.toString();

                        ConfigurationContainer container = configurations.get(fileName);
                        if (container != null) {
                            reloadConfiguration(fileName, container);
                        }
                    }
                    key.reset();
                }
            } catch (Exception e) {
                logger.error("Error in configuration watch service", e);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public <T> T loadConfiguration(String fileName, Class<T> configClass) {
        try {
            Path configPath = configDir.resolve(fileName);
            HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                    .path(configPath)
                    .build();

            CommentedConfigurationNode node = loader.load();
            T config = node.get(configClass);

            configurations.put(fileName, new ConfigurationContainer(
                    configClass,
                    config,
                    loader,
                    node
            ));

            return config;
        } catch (Exception e) {
            logger.error("Failed to load configuration: " + fileName, e);
            return null;
        }
    }

    private void reloadConfiguration(String fileName, ConfigurationContainer container) {
        try {
            CommentedConfigurationNode node = container.loader().load();
            Object newConfig = node.get(container.configClass());

            configurations.put(fileName, new ConfigurationContainer(
                    container.configClass(),
                    newConfig,
                    container.loader(),
                    node
            ));

            // Notify configuration change listeners
            for (ConfigurationChangeListener listener : container.listeners()) {
                try {
                    listener.onConfigurationChanged(newConfig);
                } catch (Exception e) {
                    logger.error("Error notifying configuration change listener", e);
                }
            }

            logger.info("Reloaded configuration: {}", fileName);
        } catch (Exception e) {
            logger.error("Failed to reload configuration: " + fileName, e);
        }
    }

    public void addChangeListener(String fileName, ConfigurationChangeListener listener) {
        ConfigurationContainer container = configurations.get(fileName);
        if (container != null) {
            container.listeners().add(listener);
        }
    }

    public void removeChangeListener(String fileName, ConfigurationChangeListener listener) {
        ConfigurationContainer container = configurations.get(fileName);
        if (container != null) {
            container.listeners().remove(listener);
        }
    }

    public void saveConfiguration(String fileName) {
        ConfigurationContainer container = configurations.get(fileName);
        if (container != null) {
            try {
                container.loader().save(container.node());
                logger.info("Saved configuration: {}", fileName);
            } catch (Exception e) {
                logger.error("Failed to save configuration: " + fileName, e);
            }
        }
    }
}
