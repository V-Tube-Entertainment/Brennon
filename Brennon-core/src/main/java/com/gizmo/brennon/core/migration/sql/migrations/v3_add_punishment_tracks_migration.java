package com.gizmo.brennon.core.migration.sql.migrations;

import com.gizmo.brennon.core.migration.FileMigration;

public class v3_add_punishment_tracks_migration extends FileMigration
{

    public v3_add_punishment_tracks_migration()
    {
        super( "/migrations/v3_add_punishment_tracks.sql" );
    }

    @Override
    public boolean shouldRun()
    {
        return true;
    }
}
