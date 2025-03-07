package com.gizmo.brennon.core.migration;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.storage.StorageType;
import com.gizmo.brennon.core.migration.config.ConfigMigrationManager;
import com.gizmo.brennon.core.migration.mongo.MongoMigrationManager;
import com.gizmo.brennon.core.migration.sql.SqlMigrationManager;

public class MigrationManagerFactory
{

    private MigrationManagerFactory()
    {
    }

    public static MigrationManager createMigrationManager()
    {
        final StorageType type = BuX.getInstance().getAbstractStorageManager().getType();

        if ( type == StorageType.MONGODB )
        {
            return new MongoMigrationManager();
        }
        return new SqlMigrationManager();
    }

    public static MigrationManager createConfigMigrationManager()
    {
        return new ConfigMigrationManager();
    }
}