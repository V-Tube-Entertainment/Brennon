package com.gizmo.brennon.core.api.storage.dao;

import com.gizmo.brennon.core.api.language.Language;
import com.gizmo.brennon.core.api.user.UserSettingType;
import com.gizmo.brennon.core.api.user.UserSettings;
import com.gizmo.brennon.core.api.user.UserStorage;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public interface UserDao
{

    CompletableFuture<Void> createUser( UUID uuid, String username, String ip, Language language, String joinedHost );

    CompletableFuture<Void> createUser( UUID uuid, String username, String ip, Language language, Date login, Date logout, String joinedHost );

    CompletableFuture<Void> updateUser( UUID uuid, String name, String ip, Language language, Date logout );

    CompletableFuture<Boolean> exists( String name );

    CompletableFuture<Boolean> ipExists( String ip );

    CompletableFuture<Optional<UserStorage>> getUserData( UUID uuid );

    CompletableFuture<Optional<UserStorage>> getUserData( String name );

    CompletableFuture<List<String>> getUsersOnIP( String ip );

    CompletableFuture<List<UUID>> getUuidsOnIP( String ip );

    CompletableFuture<Void> setName( UUID uuid, String name );

    CompletableFuture<Void> setLanguage( UUID uuid, Language language );

    CompletableFuture<Void> setJoinedHost( UUID uuid, String joinedHost );

    CompletableFuture<Map<String, Integer>> getJoinedHostList();

    CompletableFuture<Map<String, Integer>> searchJoinedHosts( final String searchTag );

    CompletableFuture<Void> ignoreUser( UUID user, UUID ignore );

    CompletableFuture<Void> unignoreUser( UUID user, UUID unignore );

    CompletableFuture<UUID> getUuidFromName( String targetName );

    CompletableFuture<UserSettings> getSettings( UUID uuid );

    CompletableFuture<Boolean> hasSetting( UUID uuid, UserSettingType type );

    CompletableFuture<Void> registerSetting( UUID uuid, UserSettingType type, Object value );

    CompletableFuture<Void> updateSetting( UUID uuid, UserSettingType type, Object value );

    CompletableFuture<Void> removeSetting( UUID uuid, UserSettingType type );

}
