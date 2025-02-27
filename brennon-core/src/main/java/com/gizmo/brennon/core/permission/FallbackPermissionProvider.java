package com.gizmo.brennon.core.permission;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FallbackPermissionProvider implements PermissionProvider {
    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        return false;
    }

    @Override
    public CompletableFuture<Boolean> hasPermissionAsync(UUID uuid, String permission) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public Optional<String> getPrefix(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getSuffix(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public CompletableFuture<Boolean> addGroup(UUID uuid, String group) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Boolean> removeGroup(UUID uuid, String group) {
        return CompletableFuture.completedFuture(false);
    }
}
