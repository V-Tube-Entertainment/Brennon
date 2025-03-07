package com.gizmo.brennon.core.api.storage;

import com.gizmo.brennon.core.storage.file.H2StorageManager;
import com.gizmo.brennon.core.storage.file.SQLiteStorageManager;
import com.gizmo.brennon.core.storage.hikari.MariaDBStorageManager;
import com.gizmo.brennon.core.storage.hikari.MySQLStorageManager;
import com.gizmo.brennon.core.storage.hikari.PostgreSQLStorageManager;
import com.gizmo.brennon.core.storage.mongodb.MongoDBStorageManager;
import lombok.Getter;

import java.util.function.Supplier;

public enum StorageType
{

    MYSQL( MySQLStorageManager::new, "MySQL" ),
    POSTGRESQL( PostgreSQLStorageManager::new, "PostgreSQL" ),
    MARIADB( MariaDBStorageManager::new, "MariaDB" ),
    SQLITE( SQLiteStorageManager::new, "SQLite" ),
    H2( H2StorageManager::new, "H2" ),
    MONGODB( MongoDBStorageManager::new, "MongoDB" );

    @Getter
    private final Supplier<? extends AbstractStorageManager> storageManagerSupplier;
    @Getter
    private final String name;

    StorageType( final Supplier<? extends AbstractStorageManager> storageManagerSupplier, final String name )
    {
        this.storageManagerSupplier = storageManagerSupplier;
        this.name = name;
    }
}
