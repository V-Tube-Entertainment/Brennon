package com.gizmo.brennon.core.storage.hikari;

import com.gizmo.brennon.core.api.storage.StorageType;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.zaxxer.hikari.HikariConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class MariaDBStorageManager extends HikariStorageManager
{

    public MariaDBStorageManager()
    {
        super( StorageType.MARIADB, ConfigFiles.CONFIG.getConfig(), getProperties() );
    }

    private static HikariConfig getProperties()
    {
        return new HikariConfig();
    }

    @Override
    protected String getDataSourceClass()
    {
        return Utils.classFound( "org.mariadb.jdbc.MariaDbDataSource" ) ? "org.mariadb.jdbc.MariaDbDataSource"
            : "org.mariadb.jdbc.MySQLDataSource";
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return dataSource.getConnection();
    }
}