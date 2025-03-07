package com.gizmo.brennon.core.migration.sql.migrations;

import com.gizmo.brennon.core.migration.FileMigration;

public class v6_api_tokens extends FileMigration
{

    public v6_api_tokens()
    {
        super( "/migrations/v6_api_tokens.sql" );
    }

    @Override
    public boolean shouldRun()
    {
        return true;
    }
}
