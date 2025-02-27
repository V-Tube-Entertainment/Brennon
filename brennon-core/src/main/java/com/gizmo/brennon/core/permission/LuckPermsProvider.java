package com.gizmo.brennon.core.permission;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LuckPermsProvider implements PermissionProvider {
    private final LuckPerms luckPerms;

    public LuckPermsProvider(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        User user = luckPerms.getUserManager().getUser(uuid);
        return user != null && user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    @Override
    public CompletableFuture<Boolean> hasPermissionAsync(UUID uuid, String permission) {
        return luckPerms.getUserManager().loadUser(uuid)
                .thenApply(user -> user.getCachedData().getPermissionData().checkPermission(permission).asBoolean());
    }

    @Override
    public Optional<String> getPrefix(UUID uuid) {
        User user = luckPerms.getUserManager().getUser(uuid);
        return user != null ?
                Optional.ofNullable(user.getCachedData().getMetaData().getPrefix()) :
                Optional.empty();
    }

    @Override
    public Optional<String> getSuffix(UUID uuid) {
        User user = luckPerms.getUserManager().getUser(uuid);
        return user != null ?
                Optional.ofNullable(user.getCachedData().getMetaData().getSuffix()) :
                Optional.empty();
    }

    @Override
    public CompletableFuture<Boolean> addGroup(UUID uuid, String group) {
        return luckPerms.getUserManager().loadUser(uuid)
                .thenApplyAsync(user -> {
                    InheritanceNode node = InheritanceNode.builder(group).build();
                    user.data().add(node);
                    luckPerms.getUserManager().saveUser(user);
                    return true;
                });
    }

    @Override
    public CompletableFuture<Boolean> removeGroup(UUID uuid, String group) {
        return luckPerms.getUserManager().loadUser(uuid)
                .thenApplyAsync(user -> {
                    InheritanceNode node = InheritanceNode.builder(group).build();
                    user.data().remove(node);
                    luckPerms.getUserManager().saveUser(user);
                    return true;
                });
    }
}