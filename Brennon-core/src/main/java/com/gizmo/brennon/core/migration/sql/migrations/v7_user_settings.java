package com.gizmo.brennon.core.migration.sql.migrations;

import com.gizmo.brennon.core.migration.FileMigration;

public class v7_user_settings extends FileMigration
{

    public v7_user_settings()
    {
        super( "/migrations/v7_user_settings.sql" );
    }

    @Override
    public boolean shouldRun()
    {
        return true;
    }
}
