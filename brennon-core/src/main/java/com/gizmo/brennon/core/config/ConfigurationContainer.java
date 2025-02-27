package com.gizmo.brennon.core.config;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

record ConfigurationContainer(
        Class<?> configClass,
        Object config,
        HoconConfigurationLoader loader,
        CommentedConfigurationNode node,
        Set<ConfigurationChangeListener> listeners
) {
    ConfigurationContainer(
            Class<?> configClass,
            Object config,
            HoconConfigurationLoader loader,
            CommentedConfigurationNode node
    ) {
        this(configClass, config, loader, node, new CopyOnWriteArraySet<>());
    }
}
