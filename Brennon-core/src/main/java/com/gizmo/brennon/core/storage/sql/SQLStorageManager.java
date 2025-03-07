package com.gizmo.brennon.core.storage.sql;

import com.gizmo.brennon.core.api.storage.AbstractStorageManager;
import com.gizmo.brennon.core.api.storage.StorageType;
import com.gizmo.brennon.core.api.storage.dao.Dao;

public abstract class SQLStorageManager extends AbstractStorageManager
{
    public SQLStorageManager( final StorageType type, final Dao dao )
    {
        super( type, dao );
    }
}
