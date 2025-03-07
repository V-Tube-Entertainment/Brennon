package com.gizmo.brennon.core.storage.data.sql;

import com.gizmo.brennon.core.api.storage.dao.*;
import com.gizmo.brennon.core.storage.data.sql.dao.*;
import lombok.Getter;

@Getter
public class SQLDao implements Dao
{

    private final UserDao userDao;
    private final PunishmentDao punishmentDao;
    private final FriendsDao friendsDao;
    private final ReportsDao reportsDao;
    private final OfflineMessageDao offlineMessageDao;
    private final ApiTokenDao apiTokenDao;

    public SQLDao()
    {
        this.userDao = new SqlUserDao();
        this.punishmentDao = new SqlPunishmentDao();
        this.friendsDao = new SqlFriendsDao();
        this.reportsDao = new SqlReportsDao();
        this.offlineMessageDao = new SqlOfflineMessageDao();
        this.apiTokenDao = new SqlApiTokenDao();
    }
}
