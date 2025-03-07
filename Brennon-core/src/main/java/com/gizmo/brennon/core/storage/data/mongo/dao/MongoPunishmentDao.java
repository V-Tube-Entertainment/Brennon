package com.gizmo.brennon.core.storage.data.mongo.dao;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.punishments.PunishmentType;
import com.gizmo.brennon.core.api.storage.dao.PunishmentDao;
import com.gizmo.brennon.core.api.storage.dao.punishments.BansDao;
import com.gizmo.brennon.core.api.storage.dao.punishments.KickAndWarnDao;
import com.gizmo.brennon.core.api.storage.dao.punishments.MutesDao;
import com.gizmo.brennon.core.api.storage.dao.punishments.TracksDao;
import com.gizmo.brennon.core.storage.data.mongo.dao.punishment.MongoBansDao;
import com.gizmo.brennon.core.storage.data.mongo.dao.punishment.MongoKickAndWarnDao;
import com.gizmo.brennon.core.storage.data.mongo.dao.punishment.MongoMutesDao;
import com.gizmo.brennon.core.storage.data.mongo.dao.punishment.MongoTracksDao;
import com.gizmo.brennon.core.storage.mongodb.MongoDBStorageManager;
import com.google.common.collect.Maps;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MongoPunishmentDao implements PunishmentDao
{

    private final BansDao bansDao;
    private final MutesDao mutesDao;
    private final KickAndWarnDao kickAndWarnDao;
    private final TracksDao tracksDao;

    public MongoPunishmentDao()
    {
        this.bansDao = new MongoBansDao();
        this.mutesDao = new MongoMutesDao();
        this.kickAndWarnDao = new MongoKickAndWarnDao();
        this.tracksDao = new MongoTracksDao();
    }

    @Override
    public BansDao getBansDao()
    {
        return bansDao;
    }

    @Override
    public MutesDao getMutesDao()
    {
        return mutesDao;
    }

    @Override
    public KickAndWarnDao getKickAndWarnDao()
    {
        return kickAndWarnDao;
    }

    @Override
    public TracksDao getTracksDao()
    {
        return tracksDao;
    }

    @Override
    public CompletableFuture<Long> getPunishmentsSince( PunishmentType type, UUID uuid, Date date )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            final MongoCollection<Document> collection = db().getCollection( type.getTable() );

            if ( type.isActivatable() )
            {
                return collection.countDocuments( Filters.and(
                    Filters.eq( "uuid", uuid.toString() ),
                    Filters.gte( "date", date ),
                    Filters.eq( "type", type.toString() ),
                    Filters.eq( "punishmentaction_status", false )
                ) );
            }
            else
            {
                return collection.countDocuments( Filters.and(
                    Filters.eq( "uuid", uuid ),
                    Filters.gte( "date", date ),
                    Filters.eq( "punishmentaction_status", false )
                ) );
            }
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Long> getIPPunishmentsSince( PunishmentType type, String ip, Date date )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            final MongoCollection<Document> collection = db().getCollection( type.getTable() );

            return collection.countDocuments( Filters.and(
                Filters.eq( "ip", ip ),
                Filters.gte( "date", date ),
                Filters.eq( "type", type.toString() ),
                Filters.eq( "punishmentaction_status", false )
            ) );
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> updateActionStatus( int limit, PunishmentType type, UUID uuid, Date date )
    {
        return CompletableFuture.runAsync( () ->
        {
            final MongoCollection<Document> collection = db().getCollection( type.getTable() );

            if ( type.isActivatable() )
            {
                collection.find(
                        Filters.and(
                            Filters.eq( "uuid", uuid.toString() ),
                            Filters.gte( "date", date ),
                            Filters.eq( "type", type.toString() ),
                            Filters.eq( "punishmentaction_status", false )
                        ) )
                    .sort( Sorts.ascending( "date" ) )
                    .limit( limit )
                    .forEach( (Consumer<? super Document>) doc ->
                    {
                        doc.put( "punishmentaction_status", true );

                        save( collection, doc );
                    } );
            }
            else
            {
                collection.find(
                        Filters.and(
                            Filters.eq( "uuid", uuid.toString() ),
                            Filters.gte( "date", date ),
                            Filters.eq( "punishmentaction_status", false )
                        ) )
                    .sort( Sorts.ascending( "date" ) )
                    .limit( limit )
                    .forEach( (Consumer<? super Document>) doc ->
                    {
                        doc.put( "punishmentaction_status", true );

                        save( collection, doc );
                    } );
            }
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> updateIPActionStatus( int limit, PunishmentType type, String ip, Date date )
    {
        return CompletableFuture.runAsync( () ->
        {
            final MongoCollection<Document> collection = db().getCollection( type.getTable() );

            collection.find(
                    Filters.and(
                        Filters.eq( "ip", ip ),
                        Filters.gte( "date", date ),
                        Filters.eq( "type", type.toString() ),
                        Filters.eq( "punishmentaction_status", false )
                    ) )
                .sort( Sorts.ascending( "date" ) )
                .limit( limit )
                .forEach( (Consumer<? super Document>) doc ->
                {
                    doc.put( "punishmentaction_status", true );

                    save( collection, doc );
                } );
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> savePunishmentAction( UUID uuid, String username, String ip, String uid )
    {
        return CompletableFuture.runAsync( () ->
        {
            final LinkedHashMap<String, Object> data = Maps.newLinkedHashMap();

            data.put( "uuid", uuid.toString() );
            data.put( "user", username );
            data.put( "ip", ip );
            data.put( "actionid", uid );
            data.put( "date", new Date() );

            db().getCollection( "bu_punishmentactions" ).insertOne( new Document( data ) );
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    // Recreating save api ...
    private void save( final MongoCollection<Document> collection, final Document document )
    {
        final Object id = document.get( "_id" );

        if ( id == null )
        {
            collection.insertOne( document );
        }
        else
        {
            collection.replaceOne( Filters.eq( "_id", id ), document, new ReplaceOptions().upsert( true ) );
        }
    }

    private MongoDatabase db()
    {
        return ( (MongoDBStorageManager) BuX.getInstance().getAbstractStorageManager() ).getDatabase();
    }
}
