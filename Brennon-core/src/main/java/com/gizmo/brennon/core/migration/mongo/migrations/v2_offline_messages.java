package com.gizmo.brennon.core.migration.mongo.migrations;

import com.gizmo.brennon.core.migration.mongo.MongoMigration;

public class v2_offline_messages implements MongoMigration
{
    @Override
    public boolean shouldRun() throws Exception
    {
        return true;
    }

    @Override
    public void migrate() throws Exception
    {
        db().getCollection( "bu_messagequeue" ).drop();
    }
}
