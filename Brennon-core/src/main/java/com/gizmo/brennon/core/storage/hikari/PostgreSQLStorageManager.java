package com.gizmo.brennon.core.storage.hikari;

import com.gizmo.brennon.core.api.storage.StorageType;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;

import java.sql.Connection;
import java.sql.SQLException;

public class PostgreSQLStorageManager extends HikariStorageManager
{

    public PostgreSQLStorageManager()
    {
        super( StorageType.POSTGRESQL, ConfigFiles.CONFIG.getConfig(), null );
    }

    @Override
    protected String getDataSourceClass()
    {
        return "org.postgresql.ds.PGSimpleDataSource";
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return dataSource.getConnection();
    }
}