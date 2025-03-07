package com.gizmo.brennon.core.storage.data.sql.dao.punishment;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.punishments.PunishmentInfo;
import com.gizmo.brennon.core.api.punishments.PunishmentType;
import com.gizmo.brennon.core.api.storage.dao.Dao;
import com.gizmo.brennon.core.api.storage.dao.PunishmentDao;
import com.gizmo.brennon.core.api.storage.dao.punishments.KickAndWarnDao;
import com.google.common.collect.Lists;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SqlKickAndWarnDao implements KickAndWarnDao
{

    @Override
    public CompletableFuture<PunishmentInfo> insertWarn( UUID uuid, String user, String ip, String reason, String server, String executedby )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "INSERT INTO " + PunishmentType.WARN.getTable() + " (uuid, user, ip, reason, server, executed_by, date)" +
                          " VALUES (?, ?, ?, ?, ?, ?, " + Dao.getInsertDateParameter() + ");"
                  ) )
            {
                pstmt.setString( 1, uuid.toString() );
                pstmt.setString( 2, user );
                pstmt.setString( 3, ip );
                pstmt.setString( 4, reason );
                pstmt.setString( 5, server );
                pstmt.setString( 6, executedby );
                pstmt.setString( 7, Dao.formatDateToString( new Date() ) );

                pstmt.executeUpdate();
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured:", e );
            }
            return PunishmentDao.buildPunishmentInfo( PunishmentType.WARN, uuid, user, ip, reason, server, executedby, new Date(), -1, true, null );
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<PunishmentInfo> insertKick( UUID uuid, String user, String ip, String reason, String server, String executedby )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "INSERT INTO " + PunishmentType.KICK.getTable() + " (uuid, user, ip, reason, server, executed_by, date)" +
                          " VALUES (?, ?, ?, ?, ?, ?, " + Dao.getInsertDateParameter() + ");"
                  ) )
            {
                pstmt.setString( 1, uuid.toString() );
                pstmt.setString( 2, user );
                pstmt.setString( 3, ip );
                pstmt.setString( 4, reason );
                pstmt.setString( 5, server );
                pstmt.setString( 6, executedby );
                pstmt.setString( 7, Dao.formatDateToString( new Date() ) );

                pstmt.executeUpdate();
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured:", e );
            }
            return PunishmentDao.buildPunishmentInfo( PunishmentType.KICK, uuid, user, ip, reason, server, executedby, new Date(), -1, true, null );
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<List<PunishmentInfo>> getKicks( UUID uuid )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            final List<PunishmentInfo> punishments = Lists.newArrayList();

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "SELECT * FROM " + PunishmentType.KICK.getTable() + " WHERE uuid = ?;"
                  ) )
            {
                pstmt.setString( 1, uuid.toString() );

                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    while ( rs.next() )
                    {
                        final int id = rs.getInt( "id" );
                        final String ip = rs.getString( "ip" );
                        final String user = rs.getString( "user" );
                        final String reason = rs.getString( "reason" );
                        final String server = rs.getString( "server" );
                        final String executedby = rs.getString( "executed_by" );
                        final Date date = Dao.formatStringToDate( rs.getString( "date" ) );

                        punishments.add( PunishmentDao.buildPunishmentInfo(
                            id, PunishmentType.KICK, uuid, user, ip, reason, server, executedby,
                            date, -1, true, null
                        ) );
                    }
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured:", e );
            }

            return punishments;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<List<PunishmentInfo>> getWarns( UUID uuid )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            final List<PunishmentInfo> punishments = Lists.newArrayList();

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "SELECT * FROM " + PunishmentType.WARN.getTable() + " WHERE uuid = ?;"
                  ) )
            {
                pstmt.setString( 1, uuid.toString() );

                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    while ( rs.next() )
                    {
                        final int id = rs.getInt( "id" );
                        final String ip = rs.getString( "ip" );
                        final String user = rs.getString( "user" );
                        final String reason = rs.getString( "reason" );
                        final String server = rs.getString( "server" );
                        final String executedby = rs.getString( "executed_by" );
                        final Date date = Dao.formatStringToDate( rs.getString( "date" ) );

                        punishments.add( PunishmentDao.buildPunishmentInfo(
                            id, PunishmentType.WARN, uuid, user, ip, reason, server, executedby,
                            date, -1, true, null
                        ) );
                    }
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured:", e );
            }

            return punishments;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<List<PunishmentInfo>> getKicksExecutedBy( String name )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            final List<PunishmentInfo> punishments = Lists.newArrayList();

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "SELECT * FROM " + PunishmentType.KICK.getTable() + " WHERE executed_by = ?;"
                  ) )
            {
                pstmt.setString( 1, name );

                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    while ( rs.next() )
                    {
                        final int id = rs.getInt( "id" );
                        final String ip = rs.getString( "ip" );
                        final UUID uuid = UUID.fromString( rs.getString( "uuid" ) );
                        final String user = rs.getString( "user" );
                        final String reason = rs.getString( "reason" );
                        final String server = rs.getString( "server" );
                        final String executedby = rs.getString( "executed_by" );
                        final Date date = Dao.formatStringToDate( rs.getString( "date" ) );

                        punishments.add( PunishmentDao.buildPunishmentInfo(
                            id, PunishmentType.KICK, uuid, user, ip, reason, server, executedby,
                            date, -1, true, null
                        ) );
                    }
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured:", e );
            }

            return punishments;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<List<PunishmentInfo>> getWarnsExecutedBy( String name )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            final List<PunishmentInfo> punishments = Lists.newArrayList();

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "SELECT * FROM " + PunishmentType.WARN.getTable() + " WHERE executed_by = ?;"
                  ) )
            {
                pstmt.setString( 1, name );

                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    while ( rs.next() )
                    {
                        final int id = rs.getInt( "id" );
                        final String ip = rs.getString( "ip" );
                        final UUID uuid = UUID.fromString( rs.getString( "uuid" ) );
                        final String user = rs.getString( "user" );
                        final String reason = rs.getString( "reason" );
                        final String server = rs.getString( "server" );
                        final String executedby = rs.getString( "executed_by" );
                        final Date date = Dao.formatStringToDate( rs.getString( "date" ) );

                        punishments.add( PunishmentDao.buildPunishmentInfo(
                            id, PunishmentType.WARN, uuid, user, ip, reason, server, executedby,
                            date, -1, true, null
                        ) );
                    }
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured:", e );
            }

            return punishments;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<PunishmentInfo> getKickById( String id )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            PunishmentInfo info = null;

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "SELECT * FROM " + PunishmentType.KICK.getTable() + " WHERE id = ? LIMIT 1;"
                  ) )
            {
                pstmt.setInt( 1, Integer.parseInt( id ) );

                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    if ( rs.next() )
                    {
                        final UUID uuid = UUID.fromString( rs.getString( "uuid" ) );
                        final String user = rs.getString( "user" );
                        final String ip = rs.getString( "ip" );
                        final String reason = rs.getString( "reason" );
                        final String server = rs.getString( "server" );
                        final String executedby = rs.getString( "executed_by" );
                        final Date date = Dao.formatStringToDate( rs.getString( "date" ) );

                        info = PunishmentDao.buildPunishmentInfo(
                            id, PunishmentType.KICK, uuid, user, ip, reason, server, executedby,
                            date, -1, true, null
                        );
                    }
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured:", e );
            }

            return info;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<PunishmentInfo> getWarnById( String id )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            PunishmentInfo info = null;

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "SELECT * FROM " + PunishmentType.WARN.getTable() + " WHERE id = ? LIMIT 1;"
                  ) )
            {
                pstmt.setInt( 1, Integer.parseInt( id ) );

                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    if ( rs.next() )
                    {
                        final UUID uuid = UUID.fromString( rs.getString( "uuid" ) );
                        final String user = rs.getString( "user" );
                        final String ip = rs.getString( "ip" );
                        final String reason = rs.getString( "reason" );
                        final String server = rs.getString( "server" );
                        final String executedby = rs.getString( "executed_by" );
                        final Date date = Dao.formatStringToDate( rs.getString( "date" ) );

                        info = PunishmentDao.buildPunishmentInfo(
                            id, PunishmentType.WARN, uuid, user, ip, reason, server, executedby,
                            date, -1, true, null
                        );
                    }
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured:", e );
            }

            return info;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }
}
