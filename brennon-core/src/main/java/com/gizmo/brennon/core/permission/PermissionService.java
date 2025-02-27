package com.gizmo.brennon.core.permission;

import com.google.inject.Inject;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PermissionService implements Service {
    private final Logger logger;
    private PermissionProvider provider;

    @Inject
    public PermissionService(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void enable() {
        // Try to initialize LuckPerms provider
        try {
            Class.forName("net.luckperms.api.LuckPerms");
            initializeLuckPerms();
        } catch (ClassNotFoundException e) {
            logger.warn("LuckPerms not found, using fallback permission provider");
            provider = new FallbackPermissionProvider();
        }
    }

    private void initializeLuckPerms() {
        try {
            net.luckperms.api.LuckPerms luckPerms = net.luckperms.api.LuckPermsProvider.get();
            provider = new LuckPermsProvider(luckPerms);
            logger.info("Successfully initialized LuckPerms provider");
        } catch (Exception e) {
            logger.error("Failed to initialize LuckPerms provider", e);
            provider = new FallbackPermissionProvider();
        }
    }

    public boolean hasPermission(UUID uuid, String permission) {
        return provider.hasPermission(uuid, permission);
    }

    public CompletableFuture<Boolean> hasPermissionAsync(UUID uuid, String permission) {
        return provider.hasPermissionAsync(uuid, permission);
    }

    public Optional<String> getPrefix(UUID uuid) {
        return provider.getPrefix(uuid);
    }

    public Optional<String> getSuffix(UUID uuid) {
        return provider.getSuffix(uuid);
    }

    public CompletableFuture<Boolean> addGroup(UUID uuid, String group) {
        return provider.addGroup(uuid, group);
    }

    public CompletableFuture<Boolean> removeGroup(UUID uuid, String group) {
        return provider.removeGroup(uuid, group);
    }

    @Override
    public void disable() throws Exception {
        // Cleanup if needed
    }
}
