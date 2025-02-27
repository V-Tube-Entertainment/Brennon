package com.gizmo.brennon.core.permission;


import net.luckperms.api.LuckPerms;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LuckPermsHelper {
    private final LuckPerms luckPerms;

    public LuckPermsHelper(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    public CompletableFuture<Collection<String>> getUserGroups(UUID uuid) {
        return luckPerms.getUserManager().loadUser(uuid)
                .thenApply(user -> user.getNodes().stream()
                        .filter(Node::isGroupNode)
                        .map(Node::getKey)
                        .collect(Collectors.toSet()));
    }

    public CompletableFuture<Boolean> hasPermission(UUID uuid, String permission) {
        return luckPerms.getUserManager().loadUser(uuid)
                .thenApply(user -> user.getCachedData().getPermissionData().checkPermission(permission).asBoolean());
    }

    public CompletableFuture<Optional<String>> getPrimaryGroup(UUID uuid) {
        return luckPerms.getUserManager().loadUser(uuid)
                .thenApply(user -> Optional.ofNullable(user.getPrimaryGroup()));
    }

    public CompletableFuture<Void> addPermission(UUID uuid, String permission) {
        return luckPerms.getUserManager().loadUser(uuid)
                .thenAccept(user -> {
                    user.data().add(Node.builder(permission).build());
                    luckPerms.getUserManager().saveUser(user);
                });
    }

    public CompletableFuture<Void> removePermission(UUID uuid, String permission) {
        return luckPerms.getUserManager().loadUser(uuid)
                .thenAccept(user -> {
                    user.data().remove(Node.builder(permission).build());
                    luckPerms.getUserManager().saveUser(user);
                });
    }
}