package com.gizmo.brennon.core.storage.data.sql.dao;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.language.Language;
import com.gizmo.brennon.core.api.storage.StorageType;
import com.gizmo.brennon.core.api.storage.dao.Dao;
import com.gizmo.brennon.core.api.storage.dao.UserDao;
import com.gizmo.brennon.core.api.user.UserSetting;
import com.gizmo.brennon.core.api.user.UserSettingType;
import com.gizmo.brennon.core.api.user.UserSettings;
import com.gizmo.brennon.core.api.user.UserStorage;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SqlUserDao implements UserDao
{

    @Override
    public CompletableFuture<Void> createUser( final UUID uuid,
                                               final String username,
                                               final String ip,
                                               final Language language,
                                               final String joinedHost )
    {
        final Date date = new Date( System.currentTimeMillis() );

        return createUser( uuid, username, ip, language, date, date, joinedHost );
    }

    @Override
    public CompletableFuture<Void> createUser( final UUID uuid,
                                               final String username,
                                               final String ip,
                                               final Language language,
                                               final Date login,
                                               final Date logout,
                                               final String joinedHost )
    {
        return CompletableFuture.runAsync( () ->
        {
            final StorageType type = BuX.getInstance().getAbstractStorageManager().getType();
            final String statement = type == StorageType.SQLITE || type == StorageType.POSTGRESQL
                ? "INSERT INTO bu_users (uuid, username, ip, language, firstlogin, lastlogout, joined_host) VALUES (?, ?, ?, ?, " + Dao.getInsertDateParameter() + ", " + Dao.getInsertDateParameter() + ", ?) ON CONFLICT(uuid) DO UPDATE SET username = ?;"
                : "INSERT INTO bu_users (uuid, username, ip, language, firstlogin, lastlogout, joined_host) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE username = ?;";

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement( statement ) )
            {
                pstmt.setString( 1, uuid.toString() );
                pstmt.setString( 2, username );
                pstmt.setString( 3, ip );
                pstmt.setString( 4, language.getName() );
                pstmt.setString( 5, Dao.formatDateToString( login ) );
                pstmt.setString( 6, Dao.formatDateToString( logout ) );
                pstmt.setString( 7, joinedHost );
                pstmt.setString( 8, username );

                pstmt.executeUpdate();
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> updateUser( UUID uuid, String name, String ip, Language language, Date logout )
    {
        return CompletableFuture.runAsync( () ->
        {
            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "UPDATE bu_users SET username = ?, ip = ?, language = ?"
                          + ( logout == null ? "" : ", lastlogout = " + Dao.getInsertDateParameter() )
                          + " WHERE uuid = ?;"
                  ) )
            {
                pstmt.setString( 1, name );
                pstmt.setString( 2, ip );
                pstmt.setString( 3, language.getName() );
                if ( logout != null )
                {
                    pstmt.setString( 4, Dao.formatDateToString( logout ) );
                }
                pstmt.setString( logout == null ? 4 : 5, uuid.toString() );

                pstmt.executeUpdate();
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Boolean> exists( final String name )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            boolean present = false;

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "SELECT id FROM bu_users WHERE username = ?;"
                  ) )
            {
                pstmt.setString( 1, name );

                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    present = rs.next();
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }

            return present;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Boolean> ipExists( String ip )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            boolean present = false;

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "SELECT id FROM bu_users WHERE ip = ?;"
                  ) )
            {
                pstmt.setString( 1, ip );

                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    present = rs.next();
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }

            return present;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Optional<UserStorage>> getUserData( final UUID uuid )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            UserStorage storage = null;

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement( "SELECT * FROM bu_users WHERE uuid = ?;" ) )
            {
                pstmt.setString( 1, uuid.toString() );

                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    if ( rs.next() )
                    {
                        storage = loadUserStorageFromResultSet( connection, rs );
                    }
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
            return Optional.ofNullable( storage );
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Optional<UserStorage>> getUserData( final String name )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            UserStorage storage = null;

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement( "SELECT * FROM bu_users WHERE username = ? OR ip = ?;" ) )
            {
                pstmt.setString( 1, name );
                pstmt.setString( 2, name );

                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    if ( rs.next() )
                    {
                        storage = loadUserStorageFromResultSet( connection, rs );
                    }
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
            return Optional.ofNullable( storage );
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<List<String>> getUsersOnIP( String ip )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            final List<String> users = Lists.newArrayList();

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "SELECT username FROM bu_users WHERE ip = ?;"
                  ) )
            {
                pstmt.setString( 1, ip );

                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    while ( rs.next() )
                    {
                        users.add( rs.getString( "username" ) );
                    }
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
            return users;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<List<UUID>> getUuidsOnIP( String ip )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            final List<UUID> uuids = Lists.newArrayList();

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "SELECT uuid FROM bu_users WHERE ip = ?;"
                  ) )
            {
                pstmt.setString( 1, ip );

                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    while ( rs.next() )
                    {
                        uuids.add( UUID.fromString( rs.getString( "uuid" ) ) );
                    }
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
            return uuids;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> setName( UUID uuid, String name )
    {
        return CompletableFuture.runAsync( () ->
        {
            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "UPDATE bu_users SET username = ? WHERE uuid = ?;"
                  ) )
            {
                pstmt.setString( 1, name );
                pstmt.setString( 2, uuid.toString() );

                pstmt.executeUpdate();
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> setLanguage( UUID uuid, Language language )
    {
        return CompletableFuture.runAsync( () ->
        {
            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "UPDATE bu_users SET language = ? WHERE uuid = ?;"
                  ) )
            {
                pstmt.setString( 1, language.getName() );
                pstmt.setString( 2, uuid.toString() );

                pstmt.executeUpdate();
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> setJoinedHost( UUID uuid, String joinedHost )
    {
        return CompletableFuture.runAsync( () ->
        {
            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "UPDATE bu_users SET joined_host = ? WHERE uuid = ?;"
                  ) )
            {
                pstmt.setString( 1, joinedHost );
                pstmt.setString( 2, uuid.toString() );

                pstmt.executeUpdate();
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Map<String, Integer>> getJoinedHostList()
    {
        return CompletableFuture.supplyAsync( () ->
        {
            final Map<String, Integer> map = Maps.newHashMap();

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "SELECT joined_host, COUNT(*) amount FROM bu_users GROUP BY joined_host;"
                  ) )
            {
                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    while ( rs.next() )
                    {
                        final String joinedHost = rs.getString( "joined_host" );

                        if ( joinedHost != null )
                        {
                            map.put( joinedHost, rs.getInt( "amount" ) );
                        }
                    }
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
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

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "SELECT joined_host, COUNT(*) amount FROM bu_users WHERE joined_host LIKE ? GROUP BY joined_host;"
                  ) )
            {
                pstmt.setString( 1, "%" + searchTag + "%" );
                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    while ( rs.next() )
                    {
                        final String joinedHost = rs.getString( "joined_host" );

                        if ( joinedHost != null )
                        {
                            map.put( joinedHost, rs.getInt( "amount" ) );
                        }
                    }
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }

            return map;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> ignoreUser( UUID user, UUID ignore )
    {
        return CompletableFuture.runAsync( () ->
        {
            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "INSERT INTO bu_ignoredusers(user, ignored) VALUES (?, ?);"
                  ) )
            {
                pstmt.setString( 1, user.toString() );
                pstmt.setString( 2, ignore.toString() );

                pstmt.executeUpdate();
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> unignoreUser( UUID user, UUID unignore )
    {
        return CompletableFuture.runAsync( () ->
        {
            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement(
                      "DELETE FROM bu_ignoredusers WHERE user = ? AND ignored = ?;"
                  ) )
            {
                pstmt.setString( 1, user.toString() );
                pstmt.setString( 2, unignore.toString() );

                pstmt.executeUpdate();
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<UUID> getUuidFromName( final String targetName )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            UUID uuid = null;

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement( "SELECT uuid FROM bu_users WHERE username = ?;" ) )
            {
                pstmt.setString( 1, targetName );

                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    if ( rs.next() )
                    {
                        uuid = UUID.fromString( rs.getString( "uuid" ) );
                    }
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
            return uuid;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<UserSettings> getSettings( final UUID uuid )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            List<UserSetting> userSettings = new ArrayList<>();

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement( "SELECT * FROM bu_user_settings WHERE uuid = ?;" ) )
            {
                pstmt.setString( 1, uuid.toString() );

                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    while ( rs.next() )
                    {
                        userSettings.add( new UserSetting(
                            UserSettingType.valueOf( rs.getString( "setting_type" ) ),
                            rs.getObject( "setting_value" )
                        ) );
                    }
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
            return new UserSettings( uuid, userSettings );
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Boolean> hasSetting( UUID uuid, UserSettingType type )
    {
        return CompletableFuture.supplyAsync( () ->
        {
            boolean exists = false;

            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement( "SELECT id FROM bu_user_settings WHERE uuid = ?;" ) )
            {
                pstmt.setString( 1, uuid.toString() );

                try ( ResultSet rs = pstmt.executeQuery() )
                {
                    if ( rs.next() )
                    {
                        exists = true;
                    }
                }
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
            return exists;
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> registerSetting( UUID uuid, UserSettingType type, Object value )
    {
        return CompletableFuture.runAsync( () ->
        {
            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement( "INSERT INTO bu_user_settings(uuid, setting_type, setting_value) VALUES (?, ?, ?);" ) )
            {
                pstmt.setString( 1, uuid.toString() );
                pstmt.setString( 2, type.toString() );
                pstmt.setObject( 3, value );
                pstmt.executeUpdate();
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> updateSetting( UUID uuid, UserSettingType type, Object value )
    {
        return CompletableFuture.runAsync( () ->
        {
            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement( "UPDATE bu_user_settings SET setting_value = ? WHERE uuid = ? and setting_type = ?;" ) )
            {
                pstmt.setObject( 1, value );
                pstmt.setString( 2, uuid.toString() );
                pstmt.setString( 3, type.toString() );
                pstmt.executeUpdate();
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    @Override
    public CompletableFuture<Void> removeSetting( UUID uuid, UserSettingType type )
    {
        return CompletableFuture.runAsync( () ->
        {
            try ( Connection connection = BuX.getApi().getStorageManager().getConnection();
                  PreparedStatement pstmt = connection.prepareStatement( "DELETE FROM bu_user_settings WHERE uuid = ? and setting_type = ?;" ) )
            {
                pstmt.setString( 1, uuid.toString() );
                pstmt.setString( 2, type.toString() );
                pstmt.executeUpdate();
            }
            catch ( SQLException e )
            {
                BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            }
        }, BuX.getInstance().getScheduler().getExecutorService() );
    }

    private UserStorage loadUserStorageFromResultSet( final Connection connection,
                                                      final ResultSet rs ) throws SQLException
    {
        final UserStorage storage = new UserStorage();
        storage.setUuid( UUID.fromString( rs.getString( "uuid" ) ) );
        storage.setUserName( rs.getString( "username" ) );
        storage.setIp( rs.getString( "ip" ) );
        storage.setLanguage( BuX.getApi().getLanguageManager().getLangOrDefault( rs.getString( "language" ) ) );
        storage.setFirstLogin( Dao.formatStringToDate( rs.getString( "firstlogin" ) ) );
        storage.setLastLogout( Dao.formatStringToDate( rs.getString( "lastlogout" ) ) );
        storage.setJoinedHost( rs.getString( "joined_host" ) );
        storage.setIgnoredUsers( loadIgnoredUsers( connection, storage.getUuid() ) );
        return storage;
    }

    private List<String> loadIgnoredUsers( final Connection connection, final UUID user ) throws SQLException
    {
        final List<String> ignoredUsers = Lists.newArrayList();
        try ( PreparedStatement pstmt = connection.prepareStatement(
            "SELECT username FROM bu_ignoredusers iu LEFT JOIN bu_users u ON iu.ignored = u.uuid WHERE user = ?;"
        ) )
        {
            pstmt.setString( 1, user.toString() );

            try ( ResultSet rs = pstmt.executeQuery() )
            {
                while ( rs.next() )
                {
                    ignoredUsers.add( rs.getString( "username" ) );
                }
            }
        }
        return ignoredUsers;
    }
}
