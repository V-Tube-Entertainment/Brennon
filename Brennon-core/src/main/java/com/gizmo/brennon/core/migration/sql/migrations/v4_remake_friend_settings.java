package com.gizmo.brennon.core.migration.sql.migrations;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.friends.FriendSetting;
import com.gizmo.brennon.core.migration.FileMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class v4_remake_friend_settings extends FileMigration
{

    public v4_remake_friend_settings()
    {
        super( "/migrations/v4_remake_friend_settings.sql" );
    }

    @Override
    public void migrate() throws SQLException
    {
        try ( Connection connection = BuX.getInstance().getAbstractStorageManager().getConnection() )
        {
            super.migrate();

            try ( PreparedStatement preparedStatement = connection.prepareStatement(
                "select * from bu_friendsettings_old"
            ) )
            {
                try ( ResultSet rs = preparedStatement.executeQuery() )
                {
                    while ( rs.next() )
                    {
                        final String user = rs.getString( "user" );
                        final boolean requests = rs.getBoolean( "requests" );
                        final boolean messages = rs.getBoolean( "messages" );

                        try ( PreparedStatement pstmt = connection.prepareStatement( "insert into bu_friendsettings values (?, ?, ?)" ) )
                        {
                            pstmt.setString( 1, user );
                            pstmt.setString( 2, FriendSetting.REQUESTS.toString() );
                            pstmt.setObject( 3, requests );

                            pstmt.executeUpdate();
                        }

                        try ( PreparedStatement pstmt = connection.prepareStatement( "insert into bu_friendsettings values (?, ?, ?)" ) )
                        {
                            pstmt.setString( 1, user );
                            pstmt.setString( 2, FriendSetting.MESSAGES.toString() );
                            pstmt.setObject( 3, messages );

                            pstmt.executeUpdate();
                        }
                    }
                }
            }

            try ( PreparedStatement preparedStatement = connection.prepareStatement(
                "drop table bu_friendsettings_old"
            ) )
            {
                preparedStatement.execute();
            }
        }
    }

    @Override
    public boolean shouldRun()
    {
        return true;
    }
}
