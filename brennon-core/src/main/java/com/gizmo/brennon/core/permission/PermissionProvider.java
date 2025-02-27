package com.gizmo.brennon.core.permission;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PermissionProvider {
    boolean hasPermission(UUID uuid, String permission);
    CompletableFuture<Boolean> hasPermissionAsync(UUID uuid, String permission);
    Optional<String> getPrefix(UUID uuid);
    Optional<String> getSuffix(UUID uuid);
    CompletableFuture<Boolean> addGroup(UUID uuid, String group);
    CompletableFuture<Boolean> removeGroup(UUID uuid, String group);
}
