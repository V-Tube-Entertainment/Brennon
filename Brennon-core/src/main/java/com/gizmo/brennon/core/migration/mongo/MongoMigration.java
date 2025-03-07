package com.gizmo.brennon.core.migration.mongo;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.migration.Migration;
import com.gizmo.brennon.core.storage.mongodb.MongoDBStorageManager;
import com.mongodb.client.MongoDatabase;

public interface MongoMigration extends Migration
{

    default MongoDatabase db()
    {
        return ( (MongoDBStorageManager) BuX.getInstance().getAbstractStorageManager() ).getDatabase();
    }
}
