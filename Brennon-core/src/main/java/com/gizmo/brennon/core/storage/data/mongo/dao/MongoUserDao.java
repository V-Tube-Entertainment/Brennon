package com.gizmo.brennon.core.storage.data.mongo.dao;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.language.Language;
import com.gizmo.brennon.core.api.storage.dao.UserDao;
import com.gizmo.brennon.core.api.user.UserSetting;
import com.gizmo.brennon.core.api.user.UserSettingType;
import com.gizmo.brennon.core.api.user.UserSettings;
import com.gizmo.brennon.core.api.user.UserStorage;
import com.gizmo.brennon.core.storage.mongodb.MongoDBStorageManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MongoUserDao implements UserDao
{

    @Override
    public CompletableFuture<Void> createUser( UUID uuid, String username, String ip, Language language, String joinedHost )
    {
        final Date date = new Date( System.currentTimeMillis() );

        return createUser( uuid, username, ip, language, date, date, joinedHost );
    }

    @Override
    public CompletableFuture<Void> createUser( UUID uuid, String username, String ip, Language language, Date login, Date logout, String joinedHost )
    {
        return CompletableFuture.runAsync( () ->
        {
            final LinkedHashMap<String, Object> data = Maps.newLinkedHashMap();

            data.put( "uuid", uuid.toString() );
            data.put( "username", username );
            data.put( "ip", ip );
            data.put( "language", language.getName() );
            data.put( "firstlogin", login );
            data.put( "lastlogout", logout );
            data.put( "joined_host", joinedHost );
            data.put( "current_server", null );

            db().getCollection( "bu_users" ).insertOne( new Document( data ) );
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> updateUser( UUID uuid, String name, String ip, Language language, Date date )
    {
        return CompletableFuture.runAsync( () ->
        {
            final List<Bson> updates = Lists.newArrayList();

            updates.add( Updates.set( "username", name ) );
            updates.add( Updates.set( "ip", ip ) );
            updates.add( Updates.set( "language", language.getName() ) );

            if ( date != null )
            {
                updates.add( Updates.set( "lastlogout", date ) );
            }

            db().getCollection( "bu_users" )
                    .findOneAndUpdate( Filters.eq( "uuid", uuid.toString() ), Updates.combine( updates ) );
        } );
    }

    @Override
    public CompletableFuture<Boolean> exists( String name )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            return db().getCollection( "bu_users" ).find(
                    name.contains( "." ) ? Filters.eq( "ip", name ) : Filters.eq( "username", name )
            ).iterator().hasNext();
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Boolean> ipExists( String ip )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            return db().getCollection( "bu_users" ).find( Filters.eq( "ip", ip ) ).iterator().hasNext();
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Optional<UserStorage>> getUserData( UUID uuid )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            final MongoCollection<Document> userColl = db().getCollection( "bu_users" );
            final Document document = userColl.find( Filters.eq( "uuid", uuid.toString() ) ).first();

            if ( document != null )
            {
                return Optional.of( getUserStorageFromDocument( document ) );
            }
            return Optional.empty();
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Optional<UserStorage>> getUserData( String name )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            final MongoCollection<Document> userColl = db().getCollection( "bu_users" );
            final Document document = userColl.find( Filters.or(
                    Filters.eq( "username", name ),
                    Filters.eq( "ip", name )
            ) ).first();

            if ( document != null )
            {
                return Optional.of( getUserStorageFromDocument( document ) );
            }
            return Optional.empty();
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<List<String>> getUsersOnIP( String ip )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            List<String> users = Lists.newArrayList();

            for ( Document document : db().getCollection( "bu_users" ).find( Filters.eq( "ip", ip ) ) )
            {
                users.add( document.getString( "username" ) );
            }

            return users;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<List<UUID>> getUuidsOnIP( String ip )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            List<UUID> uuids = Lists.newArrayList();

            for ( Document document : db().getCollection( "bu_users" ).find( Filters.eq( "ip", ip ) ) )
            {
                uuids.add( UUID.fromString( document.getString( "uuid" ) ) );
            }

            return uuids;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> setName( UUID uuid, String name )
    {
        return CompletableFuture.runAsync( () ->
        {
            db().getCollection( "bu_users" )
                    .findOneAndUpdate( Filters.eq( "uuid", uuid.toString() ), Updates.set( "username", name ) );

        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> setLanguage( UUID uuid, Language language )
    {
        return CompletableFuture.runAsync( () ->
        {
            db().getCollection( "bu_users" )
                    .findOneAndUpdate( Filters.eq( "uuid", uuid.toString() ), Updates.set( "language", language.getName() ) );

        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> setJoinedHost( UUID uuid, String joinedHost )
    {
        return CompletableFuture.runAsync( () ->
        {
            db().getCollection( "bu_users" )
                    .findOneAndUpdate( Filters.eq( "uuid", uuid.toString() ), Updates.set( "joined_host", joinedHost ) );

        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Map<String, Integer>> getJoinedHostList()
    {
        return CompletableFuture.supplyAsync( () ->
        {
            final Map<String, Integer> map = Maps.newHashMap();
            final MongoCollection<Document> collection = db().getCollection( "bu_users" );

            for ( Document doc : collection.aggregate( Collections.singletonList(
                    Aggregates.group( "$joined_host", Accumulators.sum( "count", 1 ) )
            ) ) )
            {
                final String joinedHost = doc.getString( "_id" );

                if ( joinedHost != null )
                {
                    map.put( joinedHost, doc.getInteger( "count" ) );
                }
            }

            return map;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Map<String, Integer>> searchJoinedHosts( String searchTag )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            final Map<String, Integer> map = Maps.newHashMap();
            final MongoCollection<Document> collection = db().getCollection( "bu_users" );

            for ( Document doc : collection.aggregate( Arrays.asList(
                    Aggregates.match( Filters.regex( "joined_host", searchTag ) ),
                    Aggregates.group( "$joined_host", Accumulators.sum( "count", 1 ) )
            ) ) )
            {
                final String joinedHost = doc.getString( "_id" );

                if ( joinedHost != null )
                {
                    map.put( joinedHost, doc.getInteger( "count" ) );
                }
            }

            return map;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> ignoreUser( UUID user, UUID ignore )
    {
        return CompletableFuture.runAsync( () ->
        {
            final LinkedHashMap<String, Object> data = Maps.newLinkedHashMap();
            final MongoCollection<Document> ignoredUsersColl = db().getCollection( "bu_ignoredusers" );

            data.put( "user", user.toString() );
            data.put( "ignored", ignore.toString() );

            ignoredUsersColl.insertOne( new Document( data ) );
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> unignoreUser( UUID user, UUID unignore )
    {
        return CompletableFuture.runAsync( () ->
        {
            final MongoCollection<Document> ignoredUsersColl = db().getCollection( "bu_ignoredusers" );

            ignoredUsersColl.findOneAndDelete(
                    Filters.and(
                            Filters.eq( "user", user.toString() ),
                            Filters.eq( "ignored", unignore.toString() )
                    )
            );
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<UUID> getUuidFromName( final String targetName )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            final MongoCollection<Document> userColl = db().getCollection( "bu_users" );
            final Document document = userColl.find( Filters.eq( "username", targetName ) )
                    .projection( new BasicDBObject().append( "uuid", 1 ) )
                    .first();

            if ( document != null )
            {
                return UUID.fromString( document.getString( "uuid" ) );
            }
            return null;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<UserSettings> getSettings( UUID uuid )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            List<UserSetting> userSettings = new ArrayList<>();
            MongoCollection<Document> settingsColl = db().getCollection( "bu_user_settings" );

            settingsColl.find( Filters.eq( "uuid", uuid.toString() ) )
                    .forEach( (Consumer<Document>) document -> userSettings.add( new UserSetting(
                            UserSettingType.valueOf( document.getString( "setting_type" ) ),
                            document.get( "setting_value" )
                    ) ) );

            return new UserSettings( uuid, userSettings );
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Boolean> hasSetting( UUID uuid, UserSettingType type )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            List<UserSetting> userSettings = new ArrayList<>();
            MongoCollection<Document> settingsColl = db().getCollection( "bu_user_settings" );

            return settingsColl.find( Filters.and(
                            Filters.eq( "uuid", uuid.toString() ),
                            Filters.eq( "setting_type", type.toString() )
                    ) )
                    .first() != null;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> registerSetting( UUID uuid, UserSettingType type, Object value )
    {
        return CompletableFuture.runAsync( () ->
        {
            MongoCollection<Document> settingsColl = db().getCollection( "bu_user_settings" );
            LinkedHashMap<String, Object> data = Maps.newLinkedHashMap();

            data.put( "uuid", uuid.toString() );
            data.put( "setting_type", type.toString() );
            data.put( "setting_value", value );

            db().getCollection( "bu_user_settings" ).insertOne( new Document( data ) );
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> updateSetting( UUID uuid, UserSettingType type, Object value )
    {
        return CompletableFuture.runAsync( () ->
        {
            MongoCollection<Document> settingsColl = db().getCollection( "bu_user_settings" );

            db().getCollection( "bu_user_settings" ).updateOne(
                    Filters.and(
                            Filters.eq( "uuid", uuid.toString() ),
                            Filters.eq( "setting_type", type.toString() )
                    ),
                    Updates.set( "setting_value", value )
            );
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> removeSetting( UUID uuid, UserSettingType type )
    {
        return CompletableFuture.runAsync( () ->
        {
            MongoCollection<Document> settingsColl = db().getCollection( "bu_user_settings" );

            db().getCollection( "bu_user_settings" ).deleteOne(
                    Filters.and(
                            Filters.eq( "uuid", uuid.toString() ),
                            Filters.eq( "setting_type", type.toString() )
                    )
            );
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    private MongoDatabase db()
    {
        return ( (MongoDBStorageManager) BuX.getInstance().getAbstractStorageManager() ).getDatabase();
    }

    private UserStorage getUserStorageFromDocument( final Document document )
    {
        final UserStorage storage = new UserStorage();

        storage.setUuid( UUID.fromString( document.getString( "uuid" ) ) );
        storage.setUserName( document.getString( "username" ) );
        storage.setIp( document.getString( "ip" ) );
        storage.setLanguage(
                BuX.getApi().getLanguageManager().getLangOrDefault( document.getString( "language" ) )
        );
        storage.setFirstLogin( document.getDate( "lastlogin" ) );
        storage.setLastLogout( document.getDate( "lastlogout" ) );
        storage.setJoinedHost( document.getString( "joined_host" ) );
        storage.setIgnoredUsers( loadIgnoredUsers( storage.getUuid() ) );

        return storage;
    }

    private List<String> loadIgnoredUsers( final UUID uuid )
    {
        final List<String> ignoredUsers = Lists.newArrayList();
        final MongoCollection<Document> userColl = db().getCollection( "bu_users" );
        final MongoCollection<Document> ignoredUsersColl = db().getCollection( "bu_ignoredusers" );

        ignoredUsersColl.find(
                Filters.eq( "user", uuid.toString() )
        ).forEach( (Consumer<? super Document>) doc ->
        {
            final UUID ignoredUuid = UUID.fromString( doc.getString( "ignored" ) );
            final Document ignoredDoc = userColl.find( Filters.eq( "uuid", ignoredUuid.toString() ) ).first();

            if ( ignoredDoc != null )
            {
                ignoredUsers.add( ignoredDoc.getString( "username" ) );
            }
        } );
        return ignoredUsers;
    }
}
