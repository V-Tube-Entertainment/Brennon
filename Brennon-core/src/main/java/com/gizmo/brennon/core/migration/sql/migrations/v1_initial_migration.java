package com.gizmo.brennon.core.migration.sql.migrations;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.migration.FileMigration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class v1_initial_migration extends FileMigration
{

    public v1_initial_migration()
    {
        super( "/migrations/v1_initial_migration.sql" );
    }

    @Override
    public boolean shouldRun() throws SQLException
    {
        boolean shouldRun = true;

        try ( Connection connection = BuX.getInstance().getAbstractStorageManager().getConnection() )
        {
            final DatabaseMetaData metaData = connection.getMetaData();
            try ( ResultSet rs = metaData.getTables( null, null, "bu_users", null ) )
            {
                if ( rs.next() )
                {
                    shouldRun = false;
                }
            }
        }
        return shouldRun;
    }
}
